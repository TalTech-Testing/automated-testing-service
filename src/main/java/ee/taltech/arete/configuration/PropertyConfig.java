package ee.taltech.arete.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Properties;

/**
 * Custom properties for Spring Boot configuration.
 */
@EnableConfigurationProperties
@Configuration
@EnableScheduling
public class PropertyConfig {

	@Bean
	public JavaMailSender getJavaMailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		if (System.getenv().containsKey("GITLAB_PASSWORD")) { // if debug
			mailSender.setHost("smtp.gmail.com");
			mailSender.setPort(587);

			mailSender.setUsername("automated.testing.service@gmail.com"); // just some dev email
			mailSender.setPassword("s5KyDLo^ji*XBw2K&Tl3yBG2wwN2QVIQ&dPcfv**K204j9GWNez");

			Properties props = mailSender.getJavaMailProperties();
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.debug", "false");
		} else {
			mailSender.setHost("localhost");
		}

		return mailSender;
	}

}
