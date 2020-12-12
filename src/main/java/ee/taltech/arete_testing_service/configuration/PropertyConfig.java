package ee.taltech.arete_testing_service.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.management.OperatingSystemMXBean;
import ee.taltech.arete_testing_service.domain.Submission;
import ee.taltech.arete_testing_service.service.git.SshTransportConfigCallback;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.management.ManagementFactory;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.CopyOnWriteArrayList;

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
		mailSender.setHost("localhost");

		return mailSender;
	}

	@Bean
	@Scope("prototype")
	public Logger produceLogger(InjectionPoint injectionPoint) {
		Class<?> classOnWired = injectionPoint.getMember().getDeclaringClass();
		return LoggerFactory.getLogger(classOnWired);
	}

	@Bean
	public ObjectMapper mapper() {
		return new ObjectMapper();
	}

	@Bean
	public TransportConfigCallback transportConfigCallback() {
		return new SshTransportConfigCallback();
	}

	@Bean
	public OperatingSystemMXBean operatingSystemMXBean() {
		return ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
	}
}
