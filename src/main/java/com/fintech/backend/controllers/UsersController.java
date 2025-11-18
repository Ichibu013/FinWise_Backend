package com.fintech.backend.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fintech.backend.dto.ChangePasswordDto;
import com.fintech.backend.dto.LoginDto;
import com.fintech.backend.dto.SignupDto;
import com.fintech.backend.service.UsersService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
public class UsersController extends FormattedResponseMapping {
    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<HashMap<String, Object>> getUserDetails(@PathVariable Long id) {
        return getResponseFormat(HttpStatus.OK, "User Details Found", usersService.getUserDetails(id));
    }

    @PostMapping
    public ResponseEntity<HashMap<String, Object>> login(@RequestBody LoginDto loginDto) {
        return getResponseFormat(HttpStatus.ACCEPTED, "Login Successful", usersService.login(loginDto));
    }

    @PutMapping
    public ResponseEntity<HashMap<String, Object>> signup(@RequestBody SignupDto signupDto) {
        return getResponseFormat(HttpStatus.CREATED, "Signup Successful", usersService.createUser(signupDto));
    }

    @PatchMapping("/change-password/{userId}")
    public ResponseEntity<HashMap<String, Object>> changePassword(@PathVariable Long userId, @RequestBody ChangePasswordDto changePasswordDto) {
        return getResponseFormat(HttpStatus.ACCEPTED, "Password Changed", usersService.changePassword(userId, changePasswordDto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<HashMap<String, Object>> updateUser(@PathVariable Long id, @RequestBody SignupDto signupDto) {
        return getResponseFormat(HttpStatus.OK, "User Updated Successfully", usersService.updateUser(id, signupDto));
    }

    @Operation(summary = "Upload user profile picture", description = "Upload and save user profile picture")
    @PostMapping(value = "/{id}/profile-picture", consumes = "multipart/form-data")
    public ResponseEntity<HashMap<String, Object>> saveProfilePicture(@PathVariable Long id, @RequestParam("image") MultipartFile image) {
        return getResponseFormat(HttpStatus.OK, "Profile Picture Saved Successfully", usersService.saveProfilePicture(image, id));
    }

    @GetMapping("/{id}/profile-picture")
    public ResponseEntity<HashMap<String, Object>> getProfilePicture(@PathVariable Long id) {
        return getResponseFormat(HttpStatus.OK, "Profile Picture Found Successfully", usersService.getProfilePicture(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HashMap<String, Object>> deleteUserById(@PathVariable Long id) {
        return getResponseFormat(HttpStatus.OK, "User Deleted Successfully", usersService.deleteUserById(id));
    }

    @GetMapping("/fingerprint-login/{userId}")
    public ResponseEntity<HashMap<String, Object>> getFingerPrintForUser(@PathVariable Long userId) {
        return getResponseFormat(HttpStatus.OK,"FingerPrint found", usersService.getFingerprintForUser(userId));
    }

    @PostMapping({"/fingerprint-login"})
    public ResponseEntity<HashMap<String, Object>> loginByFingerPrint(@RequestBody JsonNode request) {
        return getResponseFormat(HttpStatus.ACCEPTED, "Logged User By FingerPrint", usersService.loginByFingerPrint(request.get("key").asText()));
    }

    @PatchMapping({"/fingerprint-login/{userId}"})
    public ResponseEntity<HashMap<String, Object>> changePasswordByFingerPrint(@RequestBody JsonNode request, @PathVariable Long userId) {
        return getResponseFormat(HttpStatus.OK, "Fingerprint uploaded", usersService.uploadFingerPrintId(userId, request.get("key").asText()));
    }

    @DeleteMapping("/fingerprint-login/{userId}")
    public ResponseEntity<HashMap<String, Object>> deleteFingerPrint(@PathVariable Long userId) {
        return getResponseFormat(HttpStatus.OK, "Fingerprint Deleted Successfully", usersService.deleteFingerPrintId(userId));
    }
}
