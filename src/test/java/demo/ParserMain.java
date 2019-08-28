package demo;

/*
 * #%L
 * ch-smpp
 * %%
 * Copyright (C) 2009 - 2015 Cloudhopper by Twitter
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.hz.smsgate.base.smpp.pdu.Pdu;
import com.hz.smsgate.base.smpp.transcoder.DefaultPduTranscoder;
import com.hz.smsgate.base.smpp.transcoder.DefaultPduTranscoderContext;
import com.hz.smsgate.base.smpp.transcoder.PduTranscoder;
import com.hz.smsgate.base.smpp.transcoder.PduTranscoderContext;
import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pdu.BufferHelper;

/**
 *
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 */
public class ParserMain {
    private static final Logger logger = LoggerFactory.getLogger(ParserMain.class);

    static public void main(String[] args) throws Exception {
        PduTranscoderContext context = new DefaultPduTranscoderContext();
        PduTranscoder transcoder = new DefaultPduTranscoder(context);
        String str = "000000420000000400000000000000030001003633393238383032000101343439353133363139323035004000000000000000000774657374323232020B00020D05";
        str = "0000002a000000090000000000000001736d7070636c69656e74310070617373776f7264000033000000";
        str = "1B65204C6F72656D201B3C697073756D1B3E20646F6C6F722073697420616D65742C20636F6E73656374657475722061646970697363696E6720656C69742E2050726F696E20666575676961742C206C656F20696420636F6D6D6F646F2074696E636964756E742C206E696268206469616D206F726E617265206573742C20766974616520616363756D73616E207269737573206C61637573207365642073656D206D657475732E";
       str = "00000021800000090000000000000001636c6f7564686f70706572000210000134";
       str = "00000021800000090000000000000001636c6f7564686f70706572000210000134";
        ChannelBuffer buffer = BufferHelper.createBuffer(str);
        Pdu pdu = transcoder.decode(buffer);
        logger.debug("{}", pdu);
    }

}
