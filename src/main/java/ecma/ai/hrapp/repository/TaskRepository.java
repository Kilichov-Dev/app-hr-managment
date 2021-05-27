package ecma.ai.hrapp.repository;

import ecma.ai.hrapp.entity.Task;
import ecma.ai.hrapp.entity.Turniket;
import ecma.ai.hrapp.entity.User;
import ecma.ai.hrapp.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findAllByTaskTaker(User taskTaker);

    List<Task> findAllByTaskTakerAndIdNot(User taskTaker, UUID id);

    List<Task> findAllByTaskGiverAndCreatedAtBetweenAndStatus(User taskGiver, Timestamp startTime, Timestamp endTime, TaskStatus status);

    List<Task> findAllByTaskGiver(User taskGiver);
//    Optional<User> findByEmail(String email);
}
