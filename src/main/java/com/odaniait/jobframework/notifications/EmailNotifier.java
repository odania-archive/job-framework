package com.odaniait.jobframework.notifications;

import com.odaniait.jobframework.models.Build;
import com.odaniait.jobframework.models.BuildJobResult;
import com.odaniait.jobframework.models.ResultStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmailNotifier implements Notifier {
	private static Logger logger = LoggerFactory.getLogger(EmailNotifier.class);

	@Value("${jobframework.from_mail}")
	private String fromMail;

	@Autowired
	private MailSender mailSender;

	@Override
	public boolean exec(Build build, Map<String, String> parameter, String notificationText) {
		String emails = parameter.get("emails");
		String output = "";

		for (String stepName : build.getResults().keySet()) {
			BuildJobResult buildJobResult = build.getResults().get(stepName);

			output += "Step: " + stepName + "\n\n";
			output += buildJobResult.getOutput();
		}

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromMail);
		message.setTo(emails);
		message.setSubject(notificationText);
		message.setText(output);

		try {
			mailSender.send(message);
		} catch (MailException e) {
			logger.error("Error sending email", e);
			return false;
		}

		return true;
	}
}
