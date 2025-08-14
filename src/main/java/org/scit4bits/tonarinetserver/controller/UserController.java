package org.scit4bits.tonarinetserver.controller;

import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.dto.UserDTO;
import org.scit4bits.tonarinetserver.repository.UserRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<SimpleResponse> createUser(@RequestBody UserDTO userJson) {
        log.debug("userJson: {}", userJson);
        userRepository.save(userJson.toEntity());
        return ResponseEntity.ok(new SimpleResponse("User created successfully"));
    }
    
}
