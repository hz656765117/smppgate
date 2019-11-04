package com.hz.smsgate.base.utils;

import com.hz.smsgate.base.smpp.config.SmppServerConfiguration;

public class SmppUtils {

    /**
     * 获取服务端配置实例
     *
     * @param port 服务端监听端口
     * @return
     */
    public static SmppServerConfiguration getServerConfig(int port) {
        // create a server configuration
        SmppServerConfiguration configuration = new SmppServerConfiguration();
        configuration.setPort(port);
        configuration.setMaxConnectionSize(10);
        configuration.setNonBlockingSocketsEnabled(true);
        configuration.setDefaultRequestExpiryTimeout(30000);
        configuration.setDefaultWindowMonitorInterval(15000);
        configuration.setDefaultWindowSize(50);
        configuration.setDefaultWindowWaitTimeout(configuration.getDefaultRequestExpiryTimeout());
        configuration.setDefaultSessionCountersEnabled(true);
        configuration.setJmxEnabled(true);
        return configuration;
    }

}
