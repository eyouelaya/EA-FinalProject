package edu.miu.cs.cs544.controller;


import edu.miu.cs.cs544.domain.CustomError;
import edu.miu.cs.cs544.domain.User;
import edu.miu.cs.cs544.domain.dto.PasswordDTO;
import edu.miu.cs.cs544.domain.dto.UserDTO;
import edu.miu.cs.cs544.domain.VerificationToken;
import edu.miu.cs.cs544.event.RegistrationCompleteEvent;
import edu.miu.cs.cs544.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController

public class UserRegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO, HttpServletRequest request) {

        try {
            User user = userService.registerUser(userDTO);
            eventPublisher.publishEvent(new RegistrationCompleteEvent(user, applicationUrl(request)));
        }
        catch (CustomError e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Successfully Registered, check your email to verify your account", HttpStatus.OK);
    }

    @GetMapping("/registrationConfirm")
    public ResponseEntity<?> confirmRegistration(@RequestParam("token") String token) {
        String result = userService.validateVerificationToken(token);
        if (result.equals("valid")) {
            return  new ResponseEntity<>("User verified successfully", HttpStatus.OK);
        }
        return  new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/resendRegistrationToken")
    public ResponseEntity<?> resendRegistrationToken(@RequestParam("token") String existingToken, HttpServletRequest request) {
        VerificationToken newToken = userService.generateNewVerificationToken(existingToken);
        User user = newToken.getUser();
        resendVerificationToken(user, newToken, applicationUrl(request));
        return  new ResponseEntity<>("Token Re-sent Successfully", HttpStatus.OK);
    }

    @PostMapping("/resetPassword")
public String resetPassword(@RequestBody PasswordDTO passwordDTO, HttpServletRequest request) {
        User user = userService.findUserByEmail(passwordDTO.getEmail());
        String url = applicationUrl(request);
        if (user != null) {
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user, token);
            url = passwordResetTokenMail(user, token, applicationUrl(request));
        }
        return url;
    }

    private String passwordResetTokenMail(User user, String token, String appUrl) {
        String url = appUrl + "/savePassword?id=" + user.getId() + "&token=" + token;
        //send password reset Email
        log.info("Click the link to reset your password: " + url);
        return url;
    }


    @PostMapping("/api/savePassword")
    public ResponseEntity<?> savePassword(@RequestParam("token") String token, @RequestBody PasswordDTO passwordDTO) {
        String result = userService.ValidatePasswordResetToken(token);

        if(!result.equals("valid")){
            return  new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST);
        }
        Optional<User> user = userService.getUserByPasswordToken(token);
        if(user.isPresent()){
            userService.changeUserPassword(user.get(), passwordDTO.getNewPassword());
            return  new ResponseEntity<>("Password Changed Successfully", HttpStatus.OK);
        }
        else {
            return  new ResponseEntity<>("Invalid Token", HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("/api/changePassword")
    public String ChangePassword(@RequestBody PasswordDTO passwordDTO) {
        User user = userService.findUserByEmail(passwordDTO.getEmail());
        if (!userService.checkIfValidOldPassword(user, passwordDTO.getOldPassword())) {
            return "Invalid Old Password";
        }
        //Save new password
     userService.changeUserPassword(user, passwordDTO.getNewPassword());
        return "password changed successfully";
    }
    private void resendVerificationToken(User user, VerificationToken newToken, String appUrl) {
        String url = appUrl + "/registrationConfirm?token=" + newToken.getToken();
        //send verification Email
        log.info("Click the link to verify your account: " + url);
    }

    private String applicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}
