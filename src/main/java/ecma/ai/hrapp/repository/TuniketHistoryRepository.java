package ecma.ai.hrapp.repository;

import ecma.ai.hrapp.entity.Turniket;
import ecma.ai.hrapp.entity.TurniketHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface TuniketHistoryRepository extends JpaRepository<TurniketHistory, UUID> {
    List<TurniketHistory> findAllByTurniketAndTimeIsBetween(Turniket turniket, Timestamp startTime, Timestamp endTime);

    List<TurniketHistory> findAllByTurniket(Turniket turniket);
//    Optional<User> findByEmail(String email);
}
