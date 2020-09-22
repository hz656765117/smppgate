package com.hz.smsgate.base.utils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;


/**
 * MailService实现类
 *
 * @author huangzhuo
 * @date 2020/5/11 14:27
 */
@Component
public class MailUtil {

	@Autowired
	private JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String from;

	public void sendSimpleMail(String to, String subject, String content) throws MailException {
		SimpleMailMessage message = new SimpleMailMessage();
		// 邮件发送者
		message.setFrom(from);
		// 邮件接受者
		message.setTo(to);
		// 主题
		message.setSubject(subject);
		// 内容
		message.setText(content);

		mailSender.send(message);
	}
}
