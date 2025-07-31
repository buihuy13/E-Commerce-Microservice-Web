package com.Huy.notification_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Huy.notification_service.model.ConfirmationBody;
import com.Huy.notification_service.service.EmailService;

@RestController
@RequestMapping("/notification")
public class NotificationController {
	
	private final EmailService emailService;

	public NotificationController(EmailService emailService) {
		this.emailService = emailService;
	}

	@PostMapping
	public ResponseEntity<String> sendEmail(@RequestBody com.Huy.notification_service.model.RequestBody requestBody) {
		emailService.sendEmail(requestBody);
		return new ResponseEntity<>("Gửi mail thành công", HttpStatus.OK);
	}

	@PostMapping("/confirmation")
	public ResponseEntity<String> sendConfirmationEmail(@RequestBody ConfirmationBody requestBody) {
		emailService.sendConfirmationEmail(requestBody);
		return new ResponseEntity<>("Gửi mail thành công", HttpStatus.OK);
	}
}