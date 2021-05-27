package ecma.ai.hrapp.controller;

import ecma.ai.hrapp.payload.ApiResponse;
import ecma.ai.hrapp.payload.TaskDto;
import ecma.ai.hrapp.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/task")
public class TaskController {
    @Autowired
    TaskService taskService;

    @PostMapping
    public HttpEntity<?> add(@Valid @RequestBody TaskDto taskDto) throws MessagingException {
        ApiResponse add = taskService.add(taskDto);
        return ResponseEntity.status(add.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(add);
    }

    @PutMapping("/{id}")
    public HttpEntity<?> edit(@RequestBody TaskDto taskDto, @PathVariable UUID id) throws MessagingException {
        ApiResponse apiResponse = taskService.edit(id, taskDto);
        return ResponseEntity.status(apiResponse.isSuccess()? HttpStatus.OK:HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    @PutMapping("/edit/{id}")
    public HttpEntity<?> editStatus(@RequestBody TaskDto taskDto, @PathVariable UUID id) throws MessagingException {
        ApiResponse apiResponse = taskService.editStatus(id, taskDto);
        return ResponseEntity.status(apiResponse.isSuccess()?HttpStatus.OK:HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    @GetMapping("/{id}")
    public HttpEntity<?> getById(@PathVariable UUID id){
        ApiResponse apiResponse = taskService.getById(id);
        return ResponseEntity.status(apiResponse.isSuccess()? HttpStatus.OK:HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    @GetMapping()
    public HttpEntity<?> getAllToFrom(@RequestParam String start){
        ApiResponse response = null;
        if (start.equals("to")){
            response = taskService.getAllTo();
        } else if (start.equals("from"))
            response = taskService.getAllFrom();

        assert response != null;
        return ResponseEntity.status(response.isSuccess()?HttpStatus.OK:HttpStatus.BAD_REQUEST).body(response);
    }


    @DeleteMapping("{id}")
    public HttpEntity<?> delete(@PathVariable UUID id){
        ApiResponse response = taskService.deleteById(id);
        return ResponseEntity.status(response.isSuccess()?HttpStatus.OK:HttpStatus.BAD_REQUEST).body(response);
    }


}
