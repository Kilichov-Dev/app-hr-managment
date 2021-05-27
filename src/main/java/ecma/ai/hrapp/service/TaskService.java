package ecma.ai.hrapp.service;

import ecma.ai.hrapp.component.Checker;
import ecma.ai.hrapp.component.MailSender;
import ecma.ai.hrapp.entity.Role;
import ecma.ai.hrapp.entity.Task;
import ecma.ai.hrapp.entity.User;
import ecma.ai.hrapp.entity.enums.RoleName;
import ecma.ai.hrapp.entity.enums.TaskStatus;
import ecma.ai.hrapp.payload.ApiResponse;
import ecma.ai.hrapp.payload.TaskDto;
import ecma.ai.hrapp.repository.TaskRepository;
import ecma.ai.hrapp.repository.UserRepository;
import ecma.ai.hrapp.security.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class TaskService {
    @Autowired
    TaskRepository taskRepository;

    @Autowired
    Checker checker;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MailSender mailSender;

    @Autowired
    JwtProvider jwtProvider;

    public ApiResponse add(TaskDto taskDto) throws MessagingException {
        String email = taskDto.getUserEmail();
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (!optionalUser.isPresent()) {
            return new ApiResponse("User not found!", false);
        }

        User user = optionalUser.get();

        //Rollarni tekshirish

        Set<Role> roles = user.getRoles();
        ApiResponse apiResponse = null;

        for (Role role : roles) {
            apiResponse = checker.checkForAny(role.getName().name());
            if (!apiResponse.isSuccess()) {
                return new ApiResponse("Sizda bunaqa role yo'q!", false);
            }
        }

        List<Task> takerTasks = taskRepository.findAllByTaskTaker(user);
        for (Task takerTask : takerTasks) {
            if (!takerTask.getStatus().name().equals(TaskStatus.COMPLETED.name()))
                return new ApiResponse("Xodimning vazifasi tugallanamagan", false);
        }

        Task task = new Task();
        task.setTaskTaker(user);
        assert apiResponse != null;
        task.setTaskGiver((User) apiResponse.getObject());
        task.setDeadline(taskDto.getDeadline());
        task.setDescription(taskDto.getDescription());
        task.setName(taskDto.getName());
        assert taskDto.getStatus() != null;
        task.setStatus(taskDto.getStatus());
        Task saved = taskRepository.save(task);

        boolean addTask = mailSender.mailTextAddTask(user.getEmail(), saved.getName(), saved.getId());
        if (!addTask)
            return new ApiResponse("Task qo'shildi, lekin junatilmadi!", true);
        return new ApiResponse("Task qo'shildi, va jo'natildi!", true);

    }

    public ApiResponse edit(UUID id, TaskDto taskDto) throws MessagingException {
        ApiResponse apiResponse = getById(id);
        if (!apiResponse.isSuccess()) {
            return apiResponse;
        }

        Task oldTask = (Task) apiResponse.getObject();

        String email = taskDto.getUserEmail();
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (!optionalUser.isPresent()) {
            return new ApiResponse("User not found!", false);
        }

        User user = optionalUser.get();

        Set<Role> roles = user.getRoles();
        ApiResponse response = null;
        for (Role role : roles) {
            checker.checkForAny(role.getName().name());
            if (response.isSuccess())
                return new ApiResponse("Sizda bunday role yo'q!", false);
        }

        List<Task> takerTasks = taskRepository.findAllByTaskTakerAndIdNot(user, id);
        for (Task takerTask : takerTasks) {
            if (!takerTask.getStatus().name().equals(TaskStatus.COMPLETED.name()))
                return new ApiResponse("Employe taskni tugatmagan!", false);
        }

        oldTask.setTaskTaker(user);


        assert response != null;
        oldTask.setTaskGiver((User) response.getObject());

        assert taskDto.getDeadline() != null;
        oldTask.setDeadline(taskDto.getDeadline());

        assert taskDto.getName() != null;
        oldTask.setName(taskDto.getName());

        assert taskDto.getStatus() != null;
        oldTask.setStatus(taskDto.getStatus());

        assert taskDto.getDescription() != null;
        oldTask.setDescription(taskDto.getDescription());

        Task saved = taskRepository.save(oldTask);

        boolean editTask = mailSender.mailTextEditTask(user.getEmail(), saved.getName(), saved.getId());
        if (!editTask)
            return new ApiResponse("Task o'zrgartirildi, lekin junatilmadi!", true);
        return new ApiResponse("Task o'rgartirildi va junatildi!", true);
    }


    public ApiResponse editStatus(UUID id, TaskDto taskDto) throws MessagingException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> optionalUser = userRepository.findById(user.getId());
        if (!optionalUser.isPresent()) {
            return new ApiResponse("User not found!", false);
        }

        Optional<Task> optionalTask = taskRepository.findById(id);
        if (!optionalTask.isPresent()) {
            return new ApiResponse("Task not found!", false);
        }

        Task task = optionalTask.get();
        if (task.getTaskTaker().getEmail().equals(user.getEmail())) {
            return new ApiResponse("Task sizniki emas!", false);
        }

        task.setStatus(taskDto.getStatus());
        Task save = taskRepository.save(task);

        if (save.getStatus().name().equals(TaskStatus.COMPLETED.name())) {
            boolean completed = mailSender.mailTextTaskCompleted(save.getTaskGiver().getEmail(), save.getTaskTaker().getEmail(), save.getName());
            if (completed)
                return new ApiResponse("Task tugallandi va email yuborildi!", true);
            return new ApiResponse("Task tugallandi lekin yuborilmadi!", false);
        }
        return new ApiResponse("Task edit!", true);
    }

    public ApiResponse getById(UUID id) {

        Optional<Task> byId = taskRepository.findById(id);
        if (byId.isPresent()) {
            return new ApiResponse("Task not found!", false);
        }

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userOptional = userRepository.findById(user.getId());
        if (!userOptional.isPresent()) {
            return new ApiResponse("Error!", false);
        }

        for (Role role : userOptional.get().getRoles()) {
            if (role.getName().name().equals(RoleName.ROLE_DIRECTOR.name())) {
                return new ApiResponse("Task id bilan ", true, byId.get());
            }
        }

        UUID idTaker = byId.get().getTaskTaker().getId();
        UUID idGiver = byId.get().getTaskGiver().getId();
        UUID idToken = userOptional.get().getId();
        if (idToken != idTaker && idToken != idGiver) {
            return new ApiResponse("Task sizga tegishli emas!", false);
        }
        return new ApiResponse("Task id bilan", true, byId.get());
    }

    public ApiResponse getAllTo() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userOptional = userRepository.findById(user.getId());
        if (!userOptional.isPresent()) {
            return new ApiResponse("User not found!", false);
        }
        List<Task> taskList = taskRepository.findAllByTaskGiver(userOptional.get());
        return new ApiResponse("TaskTaker tasklari ro'yxati", true, taskList);

    }

    public ApiResponse getAllFrom() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userOptional = userRepository.findById(user.getId());
        if (!userOptional.isPresent()) {
            return new ApiResponse("user not found!", false);
        }
        List<Task> taskList = taskRepository.findAllByTaskTaker(userOptional.get());
        return new ApiResponse("TaskGiver task list", true, taskList);
    }

    public ApiResponse deleteById(UUID id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userOptional = userRepository.findById(user.getId());
        if (userOptional.isPresent()) {
            return new ApiResponse("User not found!", false);
        }
        Optional<Task> byId = taskRepository.findById(id);
        if (byId.isPresent() && byId.get().getTaskGiver().getEmail().equals(userOptional.get().getEmail())) {
            taskRepository.deleteById(id);
            return new ApiResponse("Task deleted!", true);
        }
        return new ApiResponse("Task delete qilinmadi!", false);
    }

    public ApiResponse getAllUserDate(Timestamp startTime, Timestamp endTime, User user) {
        Set<Role> roles = user.getRoles();
        String role = null;

        for (Role role1 : roles) {
            role = role1.getName().name();
            break;
        }

        boolean check = checker.check(role);
        if (!check) {
            return new ApiResponse("Sizda ruhsat mavjud emas!", false);
        }

        List<Task> allByTaskGiverAndCreatedAtBetweenAndStatus = taskRepository.findAllByTaskGiverAndCreatedAtBetweenAndStatus(user, startTime, endTime, TaskStatus.COMPLETED);
        return new ApiResponse("Vaqt bo'yicha userning tasklar ro'yxati", true, allByTaskGiverAndCreatedAtBetweenAndStatus);

    }


}
