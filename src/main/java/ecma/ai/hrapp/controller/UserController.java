package ecma.ai.hrapp.controller;

import ecma.ai.hrapp.payload.ApiResponse;
import ecma.ai.hrapp.payload.UserDto;
import ecma.ai.hrapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.validation.Valid;
import javax.xml.ws.spi.http.HttpContext;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserService userService;


    //Yangi user qo'shish
    //MANAGER,DIREKTOR //PreAuthorize
    @PreAuthorize(value = "hasAnyRole('ROLE_MANAGER','ROLE_DIRECTOR')")
    @PostMapping("/add")
    public HttpEntity<?> add( @Valid @RequestBody UserDto userDto) throws MessagingException {

        ApiResponse response = userService.add(userDto);
        return ResponseEntity.status(response.isSuccess() ? 200 : 409).body(response);
    }

    @PreAuthorize(value = "hasAnyRole('ROLE_DIRECTOR','ROLE_MANAGER','ROLE_STAFF')")
    @PutMapping
    public HttpEntity<?> edit(@Valid @RequestBody UserDto userDto) {
        ApiResponse apiResponse = userService.edit(userDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? HttpStatus.ACCEPTED : HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    @PreAuthorize(value = "hasAnyRole('ROLE_MANAGER','ROLE_DIREKTOR')")
    @GetMapping("/token")
    public HttpEntity<?> getByToken() {
        ApiResponse apiResponse = userService.getOne();
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @PreAuthorize(value = "hasAnyRole('ROLE_MANAGER','ROLE_DIREKTOR')")
    @GetMapping("/email")
    public HttpEntity<?> getByEmail(@PathVariable String email) {
        ApiResponse apiResponse = userService.getByEmail(email);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @GetMapping("/verifyEmail")
    public HttpEntity<?> verifyEmail(@PathVariable String email, @PathVariable String code) {
        ApiResponse apiResponse = userService.verifyEmail(email, code);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }


}
