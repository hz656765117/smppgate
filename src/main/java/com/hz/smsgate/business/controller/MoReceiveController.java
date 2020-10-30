package com.hz.smsgate.business.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/moReceive")
public class MoReceiveController {



    @RequestMapping(value = "/receive.asp", method = {RequestMethod.POST, RequestMethod.GET})
    public String getData(String msisdn,String text,String time,String msgid,String longcode) {
        try {
            log.info("收到的信息为msisdn={}，text={}，time={},msgid={},longcode={}",msisdn,text,time,msgid,longcode);

            return "-1";
        } catch (Exception e) {
            log.error("接受上行信息异常", e);
            return "-1";
        }
    }




}
