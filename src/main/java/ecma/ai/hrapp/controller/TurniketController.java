package ecma.ai.hrapp.controller;

import ecma.ai.hrapp.entity.User;
import ecma.ai.hrapp.payload.ApiResponse;
import ecma.ai.hrapp.payload.TurniketDto;
import ecma.ai.hrapp.service.TurniketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/turniket")
public class TurniketController {
    @Autowired
    TurniketService turniketService;

    @GetMapping("/all")
    public HttpEntity<?> getAll() {
        ApiResponse all = turniketService.getAll();
        return ResponseEntity.status(all.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(all);

    }

    @GetMapping
    public HttpEntity<?> getByNumber(@RequestParam String number) {
        ApiResponse apiResponse = turniketService.getNumber(number);
        return ResponseEntity.status(apiResponse.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(apiResponse);
    }


    @PostMapping
    public HttpEntity<?> add(@Valid @RequestBody TurniketDto turniketDto) throws MessagingException {
        ApiResponse apiResponse = turniketService.add(turniketDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    @PutMapping
    public HttpEntity<?> edit(@RequestBody TurniketDto turniketDto, @RequestParam String number) throws MessagingException {
        ApiResponse apiResponse = turniketService.edit(number, turniketDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    @DeleteMapping
    public HttpEntity<?> delete(@RequestParam String number) {
        ApiResponse apiResponse = turniketService.delete(number);
        return ResponseEntity.status(apiResponse.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(apiResponse);
    }


}
