package com.hz.smsgate.base.constants;

import com.hz.smsgate.business.pojo.SenderIdVo;

/**
 * @Auther: huangzhuo
 * @Date: 2019/11/4 21:00
 * @Description:
 */
public class SmppServerConstants {
    /**
     * CM msgid的缓存key
     */
    public static final String CM_MSGID_CACHE = "cmMsgIdCache";


    /**
     * CM 短信下行  OPT 验证码类型
     */
    public static final String CM_SUBMIT_SM_OPT = "cmOptSubmitSm";


    /**
     * CM 短信下行   验证码类型
     */
    public static final String CM_SUBMIT_SM_TZ = "cmTzSubmitSm";


    /**
     * CM 短信下行  营销类型
     */
    public static final String CM_SUBMIT_SM_YX = "cmYxSubmitSm";


    /**
     * CM 状态报告缓存key
     */
    public static final String CM_DELIVER_SM = "cmDeliverSm";


    /**
     * WEB msgid的缓存key
     */
    public static final String WEB_MSGID_CACHE = "webMsgIdCache";


    /**
     * WEB 短短信下行 opt
     */
    public static final String WEB_SUBMIT_SM_OPT = "webSubmitSmOpt";
    /**
     * WEB 短短信下行 tz
     */
    public static final String WEB_SUBMIT_SM_TZ = "webSubmitSmTz";
    /**
     * WEB 短短信下行 yx
     */
    public static final String WEB_SUBMIT_SM_YX = "webSubmitSmYx";

//    /**
//     * WEB 长短信下行缓存key
//     */
//    public static final String WEB_LONG_SUBMIT_SM = "webLongSubmitSm";

    /**
     * WEB 长短信下行缓存  opt
     */
    public static final String WEB_LONG_SUBMIT_SM_OPT = "webOptLongSm";


    /**
     * WEB 长短信下行缓存  opt
     */
    public static final String WEB_LONG_CM_SUBMIT_SM_OPT = "webOptLongCmSm";


    /**
     * WEB 长短信下行缓存  通知
     */
    public static final String WEB_LONG_SUBMIT_SM_TZ = "webTzLongSm";

    /**
     * WEB 长短信下行缓存  营销
     */
    public static final String WEB_LONG_SUBMIT_SM_YX = "webYxLongSm";

    /**
     * WEB 长长短信下行缓存  营销
     */
    public static final String WEB_LONG_LONG_SUBMIT_SM_YX = "webYxLongLongSm";


    /**
     * WEB 长短信合并  opt 验证码类型
     */
    public static final String WEB_LONG_CM_SUBMIT_SM_SEND_OPT = "webCmOptLongSmSend";

    /**
     * WEB 长短信合并  opt 验证码类型
     */
    public static final String WEB_LONG_SUBMIT_SM_SEND_OPT = "webOptLongSmSend";

    /**
     * WEB 长短信合并   通知类型
     */
    public static final String WEB_LONG_SUBMIT_SM_SEND_TZ = "webTzLongSmSend";

    /**
     * WEB 长短信合并   营销类型
     */
    public static final String WEB_LONG_SUBMIT_SM_SEND_YX = "webYxLongSmSend";


//    /**
//     * WEB 长短信合并
//     */
//    public static final String WEB_LONG_SUBMIT_SM_SEND = "webLongSubmitSmSend";
//
//    /**
//     * WEB 长短信真实发送
//     */
//    public static final String WEB_REL_LONG_SUBMIT_SM_SEND = "webRealLongSubmitSmSend";


    /**
     * WEB 长短信真实发送  OPT
     */
    public static final String WEB_REL_CM_LONG_SUBMIT_SM_SEND_OPT = "webRealCmOptLongSmSend";

    /**
     * WEB 长短信真实发送  OPT
     */
    public static final String WEB_REL_LONG_SUBMIT_SM_SEND_OPT = "webRealOptLongSmSend";

    /**
     * WEB 长短信真实发送  通知类型
     */
    public static final String WEB_REL_LONG_SUBMIT_SM_SEND_TZ = "webRealTzLongSmSend";


    /**
     * WEB 长短信真实发送  营销类型
     */
    public static final String WEB_REL_LONG_SUBMIT_SM_SEND_YX = "webRealYxLongSmSend";





    /**
     * 同步请求到网关
     */
    public static final String SYNC_SUBMIT = "syncSubmit";


    /**
     * WEB 状态报告缓存key
     */
    public static final String WEB_DELIVER_SM = "webDeliverSm";


    /**
     * 重新绑定客户端
     */
    public static final String WEB_BIND_AGAIN = "webBindAgain";


    /**
     * 短信下行  详情记录
     */
    public static final String BACK_SUBMIT_SM = "backSubmitSm";

    /**
     * msgid的缓存key
     */
    public static final String BACK_MSGID_CACHE = "backMsgIdCache";

    /**
     * msgid的缓存key
     */
    public static final String REAL_CHANNEL_CACHE = "realChannelCache";



}
