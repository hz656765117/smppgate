package com.hz.smsgate.business.controller;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.hz.smsgate.base.smpp.pdu.SubmitSm;
import com.hz.smsgate.base.smpp.pdu.SubmitSmResp;
import com.hz.smsgate.base.smpp.pojo.Address;
import com.hz.smsgate.base.smpp.pojo.SmppSession;
import com.hz.smsgate.business.listener.ClientInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);


	@RequestMapping("test1")
	public String getSomething1() throws  Exception{
		LOGGER.debug("aaaaaaaaaaadebugdebugdebugtest1111123434");
		LOGGER.info("aaaaaaaaaaaaaaaaaaainfoinfoinfotest1111123434");
		LOGGER.error("aaaaaaaaaaaaaaaaerrorerrorerrortest1111123434");
		LOGGER.warn("aaaaaaaaaaaaaawarnwarnwarntest1warnwarn111123434");
		SmppSession session0 = ClientInit.session0;
		if (session0 == null) {
			session0 = ClientInit.clientBootstrap.bind(ClientInit.config0, ClientInit.sessionHandler);
		}

		String text160 = "\u20AC Lorem [ipsum] dolor sit amet, consectetur adipiscing elit. Proin feugiat, leo id commodo tincidunt, nibh diam ornare est, vitae accumsan risus lacus sed sem metus.";
		byte[] textBytes = CharsetUtil.encode(text160, CharsetUtil.CHARSET_GSM);
		SubmitSm submit0 = new SubmitSm();

		submit0.setSourceAddress(new Address((byte) 0x03, (byte) 0x00, "40404"));
		submit0.setDestAddress(new Address((byte) 0x01, (byte) 0x01, "44555519205"));
		try{
			submit0.setShortMessage(textBytes);
			SubmitSmResp submitResp = session0.submit(submit0, 10000);
		}catch (Exception e){

		}




		return "test111";
	}


	@RequestMapping("/login")
	public String welcome() {

		return "index";

	}


}
