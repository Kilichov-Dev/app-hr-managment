package ecma.ai.hrapp.service;

import ecma.ai.hrapp.component.Checker;
import ecma.ai.hrapp.entity.Role;
import ecma.ai.hrapp.entity.Turniket;
import ecma.ai.hrapp.entity.TurniketHistory;
import ecma.ai.hrapp.entity.enums.RoleName;
import ecma.ai.hrapp.payload.ApiResponse;
import ecma.ai.hrapp.payload.TurniketHistoryDto;
import ecma.ai.hrapp.repository.TuniketHistoryRepository;
import ecma.ai.hrapp.repository.TurniketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TuniketHistoryService {
    @Autowired
    TuniketHistoryRepository tuniketHistoryRepository;

    @Autowired
    TurniketRepository turniketRepository;

    @Autowired
    Checker checker;

    public ApiResponse add(TurniketHistoryDto turniketHistoryDto) {
        Optional<Turniket> optionalTurniket = turniketRepository.findByNumber(turniketHistoryDto.getNumber());

        if (!optionalTurniket.isPresent()) {
            return new ApiResponse("Turniket not found!", false);
        }

        Set<Role> roles = optionalTurniket.get().getOwner().getRoles();
        String role = RoleName.ROLE_STAFF.name();
        for (Role role1 : roles) {
            role = role1.getName().name();
            break;
        }

        boolean check = checker.check(role);
        if (!check)
            return new ApiResponse("Sizda bunday huquq yo'q!", false);

        TurniketHistory turniketHistory = new TurniketHistory();
        turniketHistory.setTurniket(optionalTurniket.get());
        turniketHistory.setType(turniketHistoryDto.getType());
        tuniketHistoryRepository.save(turniketHistory);
        return new ApiResponse("Success Welcome!", true);
    }

    public ApiResponse getAllDate(String number, Timestamp startTime, Timestamp endTime){
        Optional<Turniket> optionalTurniket = turniketRepository.findByNumber(number);
        if (!optionalTurniket.isPresent())
            return new ApiResponse("Turniket not found!", false);

        Set<Role> roles = optionalTurniket.get().getOwner().getRoles();
        String role = RoleName.ROLE_STAFF.name();
        for (Role role1 : roles) {
            role = role1.getName().name();
            break;
        }

        boolean check = checker.check(role);
        if (!check)
            return new ApiResponse("Sizda bunday huquq yo'q!", false);

        List<TurniketHistory> historyList =tuniketHistoryRepository.findAllByTurniketAndTimeIsBetween(optionalTurniket.get(), startTime, endTime);
        return new ApiResponse("Vaqt bo'yicha list",true, historyList);
    }

    public ApiResponse getAll(String number){
        Optional<Turniket> optionalTurniket = turniketRepository.findByNumber(number);
        if (!optionalTurniket.isPresent())
            return new ApiResponse("Turniket not found!", false);

        Set<Role> roles = optionalTurniket.get().getOwner().getRoles();
        String role = RoleName.ROLE_STAFF.name();
        for (Role role1 : roles) {
            role = role1.getName().name();
            break;
        }

        boolean check = checker.check(role);
        if (!check)
            return new ApiResponse("Sizda bunday huquq yo'q!", false);

        List<TurniketHistory> allByTurniket = tuniketHistoryRepository.findAllByTurniket(optionalTurniket.get());
        return new ApiResponse("Turniketlar tarixi", true, allByTurniket);
    }
}
