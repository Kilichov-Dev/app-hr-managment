package ecma.ai.hrapp.controller;

import ecma.ai.hrapp.entity.User;
import ecma.ai.hrapp.payload.ApiResponse;
import ecma.ai.hrapp.service.DirectorService;
import ecma.ai.hrapp.service.PaidSalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;

@RestController
@RequestMapping("/api/director")
public class DirectorController {
    @Autowired
    DirectorService directorService;

    @Autowired
    PaidSalaryService paidSalaryService;

    @GetMapping
    public HttpEntity<?> getHistoryAndTasks(@RequestParam Timestamp startTime, @RequestParam Timestamp endTime, @RequestParam String number) {
        ApiResponse apiResponse = directorService.getHistoryTasks(startTime, endTime, number);
        return ResponseEntity.status(!apiResponse.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    @GetMapping("/salaryUser")
    public HttpEntity<?> getByUser(@RequestParam String email) {
        ApiResponse apiResponse = paidSalaryService.getByUser(email);
        return ResponseEntity.status(apiResponse.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    @GetMapping("/salaryMonth")
    public HttpEntity<?> getByMonth(@RequestParam String month, @RequestParam User user) {
        ApiResponse apiResponse = paidSalaryService.getByMonth(month, user);
        return ResponseEntity.status(apiResponse.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(apiResponse);
    }

}
