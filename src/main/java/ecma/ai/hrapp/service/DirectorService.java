package ecma.ai.hrapp.service;

import ecma.ai.hrapp.component.Checker;
import ecma.ai.hrapp.entity.Turniket;
import ecma.ai.hrapp.entity.User;
import ecma.ai.hrapp.payload.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class DirectorService {
    @Autowired
    TuniketHistoryService turniketHistoryService;

    @Autowired
    TurniketService turniketService;

    @Autowired
    UserService userService;

    @Autowired
    TaskService taskService;


    public ApiResponse getHistoryTasks(Timestamp startTime, Timestamp endTime, String email) {
        ApiResponse response = userService.getByEmail(email);
        if (!response.isSuccess()) {
            return response;
        }

        User user = (User) response.getObject();

        ApiResponse turniketServiceByUser = turniketService.getByUser(user);
        if (!turniketServiceByUser.isSuccess()) {
            return turniketServiceByUser;
        }

        Turniket turniket = (Turniket) turniketServiceByUser.getObject();
        ApiResponse historyList = turniketHistoryService.getAllDate(turniket.getNumber(), startTime, endTime);

        ApiResponse taskList = taskService.getAllUserDate(startTime, endTime, user);

        List<ApiResponse> apiResponseList = new ArrayList<>();
        apiResponseList.add(historyList);
        apiResponseList.add(taskList);
        return new ApiResponse("Success", true, apiResponseList);
    }
}
