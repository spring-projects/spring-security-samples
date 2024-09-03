package org.example.magiclink;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service("myMailSender")
public class MailSender {

	private final JavaMailSender mailSender;

	public MailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void send(String to, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("noreply@example.com");
		message.setTo(to);
		message.setSubject(subject);
		message.setText(text);
		this.mailSender.send(message);
	}

}
