package org.scit4bits.tonarinetserver.controller;

import java.util.List;

import org.scit4bits.tonarinetserver.dto.PagedResponse;
import org.scit4bits.tonarinetserver.dto.SimpleResponse;
import org.scit4bits.tonarinetserver.dto.UserDTO;
import org.scit4bits.tonarinetserver.entity.User;
import org.scit4bits.tonarinetserver.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/list")
    public ResponseEntity<List<UserDTO>> getUsers(@AuthenticationPrincipal User user) {
        if(user == null || user.getIsAdmin() == false) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/getMe")
    public ResponseEntity<UserDTO> getMe(@AuthenticationPrincipal User user) {
        // Spring automatically injects the authenticated User!
        // No casting needed since User implements UserDetails
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @GetMapping("/toggleAdmin")
    public ResponseEntity<SimpleResponse> toggleAdmin(@AuthenticationPrincipal User user, @RequestParam("userId") Integer userId) {
        if(user.getIsAdmin() == false){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new SimpleResponse("You are not authorized to perform this action"));
        }
        try{
        userService.toggleAdmin(userId);
        return ResponseEntity.ok(new SimpleResponse("Toggling admin succeeded"));

        }catch (Exception e){
            return ResponseEntity.status(400).body(new SimpleResponse("Toggling admin failed"));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<UserDTO>> getUserSearch(
        @RequestParam(name="searchBy", defaultValue = "all") String searchBy,
        @RequestParam(name="search", defaultValue = "") String search,
        @RequestParam(name="page", defaultValue = "0") Integer page,
        @RequestParam(name="pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(name="sortBy", defaultValue = "id") String sortBy,
        @RequestParam(name="sortDirection", defaultValue = "asc") String sortDirection,
        @AuthenticationPrincipal User user
    ){
        if(user == null || user.getIsAdmin() == false) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        PagedResponse<UserDTO> users = userService.searchUser(searchBy, search, page, pageSize, sortBy, sortDirection);
        return ResponseEntity.ok(users);
    }
    
}
