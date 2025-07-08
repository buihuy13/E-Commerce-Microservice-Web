package com.Huy.notification_service.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.Huy.notification_service.model.RequestBody;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {
    private final SpringTemplateEngine springTemplateEngine;
    private final JavaMailSender javaMailSender;

    public EmailService(SpringTemplateEngine springTemplateEngine, JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
        this.springTemplateEngine = springTemplateEngine;
    }

    @Value("${spring.mail.username}")
    private String username;

    //@KafkaListener(topics = "notificationTopic")
    public void sendEmail(RequestBody request) {
        log.info("Sending emails...");
        try {
            Context context = new Context();
            Map<String, Object> map = new HashMap<>();
            map.put("name", request.getEmail());
            map.put("success", request.isSuccess());
            context.setVariables(map);
            String process = springTemplateEngine.process("welcome", context);
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            String subject = StringUtils
                    .join(Arrays.asList("Greetings", request.getEmail(), "!!!"), ' ');
            helper.setSubject(subject);
            helper.setText(process, true);
            helper.setTo(request.getEmail());
            helper.setFrom(username);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException | MailException ex) {
            log.error("Xảy ra lỗi khi gửi mail, " + ex.getMessage(), ex);
            throw new RuntimeException("Lỗi khi gửi mail, " + ex.getMessage(), ex);
        }
    }
}
