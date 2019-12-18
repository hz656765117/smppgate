package com.hz.smsgate.business.smpp.handler;

import com.hz.smsgate.base.constants.SmppServerConstants;
import com.hz.smsgate.base.constants.StaticValue;
import com.hz.smsgate.base.smpp.constants.SmppConstants;
import com.hz.smsgate.base.smpp.exception.RecoverablePduException;
import com.hz.smsgate.base.smpp.exception.UnrecoverablePduException;
import com.hz.smsgate.base.smpp.pdu.*;
import com.hz.smsgate.base.smpp.pojo.Address;
import com.hz.smsgate.base.smpp.pojo.PduAsyncResponse;
import com.hz.smsgate.base.smpp.pojo.SessionKey;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.base.smpp.utils.PduUtil;
import com.hz.smsgate.base.utils.PduUtils;
import com.hz.smsgate.base.utils.RedisUtil;
import com.hz.smsgate.base.utils.SmppUtils;
import com.hz.smsgate.business.listener.ClientInit;
import com.hz.smsgate.business.pojo.MsgVo;
import com.hz.smsgate.business.pojo.SmppUserVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;


/**
 * @Author huangzhuo
 * @Date 2019/10/17 14:18
 */

@Component
public class ServerSmppSessionRedisHandler extends DefaultSmppSessionHandler {

	private static final Logger logger = LoggerFactory.getLogger(ServerSmppSessionRedisHandler.class);

	@Autowired
	public RedisUtil redisUtil;

	public static ServerSmppSessionRedisHandler serverSmppSessionRedisHandler;

	@PostConstruct
	public void init() {
		serverSmppSessionRedisHandler = this;
		serverSmppSessionRedisHandler.redisUtil = this.redisUtil;
	}


	private WeakReference<SmppSession> sessionRef;

	public ServerSmppSessionRedisHandler(SmppSession session) {
		this.sessionRef = new WeakReference<SmppSession>(session);
	}

	public ServerSmppSessionRedisHandler() {
		super();
	}

	public ServerSmppSessionRedisHandler(Logger logger) {
		super(logger);
	}

	@Override
	public String lookupResultMessage(int commandStatus) {
		return super.lookupResultMessage(commandStatus);
	}

	@Override
	public String lookupTlvTagName(short tag) {
		return super.lookupTlvTagName(tag);
	}

	@Override
	public void fireChannelUnexpectedlyClosed() {
		super.fireChannelUnexpectedlyClosed();
	}

	@Override
	public PduResponse firePduRequestReceived(PduRequest pduRequest) {
		PduResponse response = pduRequest.createResponse();


		// mimic how long processing could take on a slower smsc
		try {
			if (pduRequest.isRequest()) {
				if (pduRequest.getCommandId() == SmppConstants.CMD_ID_SUBMIT_SM) {
					SubmitSmResp submitResp = (SubmitSmResp) response;
					SubmitSm submitSm = (SubmitSm) pduRequest;
					//通道替换
					submitSm = PduUtil.rewriteSmSourceAddress(submitSm);

					String msgid = SmppUtils.getMsgId();
					String systemId = "";
					SmppSession session = this.sessionRef.get();
					if (session != null) {
						systemId = session.getConfiguration().getSystemId();
						submitSm.setSystemId(systemId);
					}


					byte[] shortMessage = submitSm.getShortMessage();
					if (shortMessage[0] == 5 && shortMessage[1] == 0 && shortMessage[2] == 3) {

						submitResp.setMessageId(msgid);

						logger.info("这是拆分短信,systemid{},msgid{},后缀为{}", systemId, msgid, SmppUtils.getSuffixKeyBySm(submitSm));

						//临时流水id
						String tempMsgId = submitResp.getMessageId() + SmppUtils.getSuffixKeyBySm(submitSm);
						submitSm.setTempMsgId(tempMsgId);

						MsgVo msgVo = new MsgVo(tempMsgId, session.getConfiguration().getSystemId(), session.getConfiguration().getPassword(), submitSm.getSourceAddress().getAddress());
						try {
							serverSmppSessionRedisHandler.redisUtil.hmSet(SmppServerConstants.WEB_MSGID_CACHE, tempMsgId, msgVo);
							putSelfQueue(getRealSubmitSm(submitSm, session), 1);
						} catch (Exception e) {
							logger.error("-----------长短信下行接收，加入队列异常。------------- {}", e);
						}


						String msgId16 = new BigInteger(msgid, 10).toString(16);
						submitResp.setMessageId(msgId16);
						return submitResp;
					} else {
						submitSm.setTempMsgId(msgid);
						logger.info("这是短短信,systemid为{},msgid为:{}", systemId, msgid);
						MsgVo msgVo = new MsgVo(msgid, session.getConfiguration().getSystemId(), session.getConfiguration().getPassword(), submitSm.getSourceAddress().getAddress());

						try {
							serverSmppSessionRedisHandler.redisUtil.hmSet(SmppServerConstants.WEB_MSGID_CACHE, msgid, msgVo);

							putSelfQueue(getRealSubmitSm(submitSm, session), 0);
						} catch (Exception e) {
							logger.error("-----------短短信下行接收，加入队列异常。------------- {}", e);
						}
						String msgId16 = new BigInteger(msgid, 10).toString(16);
						submitResp.setMessageId(msgId16);
						submitResp.calculateAndSetCommandLength();
						return submitResp;
					}


				} else if (pduRequest.getCommandId() == SmppConstants.CMD_ID_DELIVER_SM) {
					return response;
				} else if (pduRequest.getCommandId() == SmppConstants.CMD_ID_ENQUIRE_LINK) {
					return response;
				} else {
					return response;
				}
			} else {
				if (pduRequest.getCommandId() == SmppConstants.CMD_ID_SUBMIT_SM_RESP) {
					return response;
				} else {
					return response;
				}

			}
		} catch (Exception e) {
			return response;
		}
	}

	/**
	 * 将网关提交过来的短信分别放到各自的队列中
	 *
	 * @param submitSm 下行短信对象
	 * @param type     短信类型  0 短短信  1 长短信
	 */
	public void putSelfQueue(SubmitSm submitSm, int type) {


		String senderId = submitSm.getSourceAddress().getAddress();
		SessionKey sessionKey = new SessionKey(submitSm.getSystemId(), senderId);

		//营销
		if (ClientInit.CHANNEL_YX_LIST.contains(sessionKey)) {
			if (type == 1) {
				serverSmppSessionRedisHandler.redisUtil.lPush(SmppServerConstants.WEB_LONG_SUBMIT_SM_YX, submitSm);
			} else {
				serverSmppSessionRedisHandler.redisUtil.lPush(SmppServerConstants.WEB_SUBMIT_SM_YX, submitSm);
			}
			//通知
		} else if (ClientInit.CHANNEL_TZ_LIST.contains(sessionKey)) {
			if (type == 1) {
				serverSmppSessionRedisHandler.redisUtil.lPush(SmppServerConstants.WEB_LONG_SUBMIT_SM_TZ, submitSm);
			} else {
				serverSmppSessionRedisHandler.redisUtil.lPush(SmppServerConstants.WEB_SUBMIT_SM_TZ, submitSm);
			}
			//opt  验证码
		} else if (ClientInit.CHANNEL_OPT_LIST.contains(sessionKey)) {
			if (type == 1) {
				serverSmppSessionRedisHandler.redisUtil.lPush(SmppServerConstants.WEB_LONG_SUBMIT_SM_OPT, submitSm);
			} else {
				serverSmppSessionRedisHandler.redisUtil.lPush(SmppServerConstants.WEB_SUBMIT_SM_OPT, submitSm);
			}
			//没有分类的 放到营销短信中去
		} else {
			if (type == 1) {
				serverSmppSessionRedisHandler.redisUtil.lPush(SmppServerConstants.WEB_LONG_SUBMIT_SM_YX, submitSm);
			} else {
				serverSmppSessionRedisHandler.redisUtil.lPush(SmppServerConstants.WEB_SUBMIT_SM_YX, submitSm);
			}
		}

	}


	/**
	 * 父账号，替换真实的systemId和senderId
	 *
	 * @param submitSm 下行对象
	 * @return 下行对象
	 */
	public SubmitSm getRealSubmitSm(SubmitSm submitSm, SmppSession session) {
		if (session == null) {
			return submitSm;
		}

		try {
			SmppUserVo smppUserFather = PduUtils.getSmppUserByUserPwd(session.getConfiguration().getSystemId(), session.getConfiguration().getPassword());
			//如果查不到账号，走原有的逻辑
			if (smppUserFather == null) {
				return submitSm;
			}

			Address sourceAddress = submitSm.getSourceAddress();

			List<SmppUserVo> list = smppUserFather.getList();
			if (list == null || list.size() <= 0) {
				submitSm.setSystemId(smppUserFather.getSystemid());
				sourceAddress.setAddress(smppUserFather.getSenderid());
				submitSm.setSourceAddress(sourceAddress);
				return submitSm;
			}

			String mbl = submitSm.getDestAddress().getAddress();
			//获取区号
			String areaCode = PduUtils.getAreaCode(mbl);

			String systemId = null;
			String senderId = null;

			List<SmppUserVo> areaList = new LinkedList<>();

			for (SmppUserVo smppUser : list) {
				if (areaCode.equals(smppUser.getAreaCode())) {
					areaList.add(smppUser);
				}
			}


			if (areaList == null || areaList.size() <= 0) {
				return submitSm;
			}

			if (areaList.size() == 1) {
				systemId = areaList.get(0).getSystemid();
				senderId = areaList.get(0).getSenderid();
			} else {
				//如果同一个国家配置了两个国家，则根据号段匹配发送
				String numSeg = PduUtils.getNumSeg(mbl);

				for (SmppUserVo smppUser : areaList) {
					if (StringUtils.isNotBlank(smppUser.getNumSegment()) && smppUser.getNumSegment().contains(numSeg)) {
						systemId = smppUser.getSystemid();
						senderId = smppUser.getSenderid();
						break;
					}
				}

				//如果号段都没匹配上，拿第一个账号发送
				if (StringUtils.isBlank(systemId) || StringUtils.isBlank(senderId)) {
					systemId = areaList.get(0).getSystemid();
					senderId = areaList.get(0).getSenderid();
					logger.error("手机号（{}），号段({})未配置到具体发送账号上,使用systemId（{}）和senderId（{}）发送", mbl, numSeg, systemId, senderId);
				}

			}

			if (StringUtils.isNotBlank(systemId) && StringUtils.isNotBlank(senderId)) {
				logger.info("systemId({}),senderId({})  获取真实systemId({})和senderId({})成功------------- ", submitSm.getSystemId(), sourceAddress.getAddress(), systemId, senderId);
				submitSm.setSystemId(systemId);
				sourceAddress.setAddress(senderId);
				submitSm.setSourceAddress(sourceAddress);

			} else {
				logger.error("systemId({}),senderId({})  获取真实systemId和senderId 失败------------- ", submitSm.getSystemId(), sourceAddress.getAddress());
			}
		} catch (Exception e) {
			logger.error("systemId({}),senderId({})  获取真实systemId和senderId 异常------------- ", submitSm.getSystemId(), submitSm.getSourceAddress().getAddress(), e);
		}

		return submitSm;
	}


	@Override
	public void fireExpectedPduResponseReceived(PduAsyncResponse pduAsyncResponse) {
		super.fireExpectedPduResponseReceived(pduAsyncResponse);
	}

	@Override
	public void fireUnexpectedPduResponseReceived(PduResponse pduResponse) {
		super.fireUnexpectedPduResponseReceived(pduResponse);
	}

	@Override
	public void fireUnrecoverablePduException(UnrecoverablePduException e) {
		super.fireUnrecoverablePduException(e);
	}

	@Override
	public void fireRecoverablePduException(RecoverablePduException e) {
		super.fireRecoverablePduException(e);
	}

	@Override
	public void fireUnknownThrowable(Throwable t) {
		super.fireUnknownThrowable(t);
	}

	@Override
	public void firePduRequestExpired(PduRequest pduRequest) {
		super.firePduRequestExpired(pduRequest);
	}

	@Override
	public boolean firePduReceived(Pdu pdu) {
		return super.firePduReceived(pdu);
	}

	@Override
	public boolean firePduDispatch(Pdu pdu) {
		return super.firePduDispatch(pdu);
	}
}
