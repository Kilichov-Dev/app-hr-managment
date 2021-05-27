package ecma.ai.hrapp.controller;

import ecma.ai.hrapp.entity.PaidSalary;
import ecma.ai.hrapp.payload.ApiResponse;
import ecma.ai.hrapp.payload.PaidSalaryDto;
import ecma.ai.hrapp.service.PaidSalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/paidSalary")
public class PaidSalaryController {

    @Autowired
    PaidSalaryService paidSalaryService;

    @PostMapping
    public HttpEntity<?> add(@Valid @RequestBody PaidSalaryDto paidSalaryDto) {
        ApiResponse apiResponse = paidSalaryService.add(paidSalaryDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    @PutMapping
    public HttpEntity<?> edit(@RequestBody PaidSalaryDto paidSalaryDto) {
        ApiResponse apiResponse = paidSalaryService.add(paidSalaryDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    @DeleteMapping
    public HttpEntity<?> delete(@RequestParam String email, @RequestParam String month) {
        ApiResponse apiResponse = paidSalaryService.delete(email, month);
        return ResponseEntity.status(apiResponse.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    @PutMapping("/stat")
    public HttpEntity<?> customize(@RequestParam String email, @RequestParam String month, @RequestParam boolean stat) {
        ApiResponse apiResponse = paidSalaryService.custom(email, month, stat);
        return ResponseEntity.status(apiResponse.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(apiResponse);
    }
}
