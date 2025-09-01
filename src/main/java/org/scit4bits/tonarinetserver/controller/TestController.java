package org.scit4bits.tonarinetserver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.scit4bits.tonarinetserver.dto.UserDTO;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.repository.UserRepository;
import org.scit4bits.tonarinetserver.service.EmailService;
import org.scit4bits.tonarinetserver.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestController {

    private final EmailService emailService;
    private final UserRepository userRepository;
    @GetMapping("/sendemail")
    public String sendEmail(@RequestParam("message") String message) {
        log.info("Received message: {}", message);
        emailService.sendEmail("hjs0410hc@gmail.com", "본인에게서 온 메일입니다. 제대로 전달되었습니까?", message);
        return "Email sent successfully";
    }

    @GetMapping("/someuser")
    @Transactional
    public ResponseEntity<UserDTO> getMethodName(@RequestParam("userId") Integer userId) {
        User user = userRepository.findById(userId).get();
        log.debug("User organizations size: {}", user.getOrganizations().size()); // Initialize the collection
        UserDTO dto = UserDTO.fromEntity(user);
        return ResponseEntity.ok(dto);
    }
}
