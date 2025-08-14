package org.scit4bits.tonarinetserver.controller;

import org.scit4bits.tonarinetserver.dto.SimpleRequest;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simple")
public class SimpleController {

    @GetMapping("/hello")
    public ResponseEntity<SimpleResponse> hello() {
        SimpleResponse response = new SimpleResponse("Hello, World!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/echo")
    public ResponseEntity<SimpleResponse> echo(@RequestBody SimpleRequest request) {
        String replyMessage = "Received: " + request.getMessage();
        if(request.getMessage() == null){
            return ResponseEntity.badRequest().body(new SimpleResponse("Message cannot be null"));
        }
        SimpleResponse response = new SimpleResponse(replyMessage);
        return ResponseEntity.ok(response);
    }

}