package ecma.ai.hrapp.service;

import ecma.ai.hrapp.component.Checker;
import ecma.ai.hrapp.entity.PaidSalary;
import ecma.ai.hrapp.entity.Role;
import ecma.ai.hrapp.entity.User;
import ecma.ai.hrapp.entity.enums.Month;
import ecma.ai.hrapp.entity.enums.RoleName;
import ecma.ai.hrapp.payload.ApiResponse;
import ecma.ai.hrapp.payload.PaidSalaryDto;
import ecma.ai.hrapp.repository.PaidSalaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class PaidSalaryService {
    @Autowired
    PaidSalaryRepository paidSalaryRepository;

    @Autowired
    Checker checker;

    @Autowired
    UserService userService;

    public ApiResponse add(PaidSalaryDto paidSalaryDto) {
        ApiResponse response = userService.getByEmail(paidSalaryDto.getEmail());
        if (!response.isSuccess()) {
            return response;
        }

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Set<Role> roles = user.getRoles();
        String role = RoleName.ROLE_STAFF.name();

        for (Role role1 : roles) {
            role = role1.getName().name();

        }
        boolean check = checker.check(role);
        if (!check) {
            return new ApiResponse("Sizda bunday huquq yo'q!", false);
        }

        PaidSalary paidSalary = new PaidSalary();
        paidSalary.setAmount(paidSalaryDto.getAmount());
        paidSalary.setOwner(user);
        paidSalary.setPeriod(paidSalaryDto.getPeriod());
        paidSalaryRepository.save(paidSalary);
        return new ApiResponse("Hodimga oylik kiritildi!", true);

    }

    public ApiResponse edit(PaidSalaryDto paidSalaryDto) {
        ApiResponse response = userService.getByEmail(paidSalaryDto.getEmail());
        if (!response.isSuccess())
            return response;
        User user = (User) response.getObject();

        Set<Role> roles = user.getRoles();
        String role = RoleName.ROLE_STAFF.name();
        for (Role rolex : roles) {
            role = rolex.getName().name();
        }

        boolean check = checker.check(role);
        if (!check)
            return new ApiResponse("Sizda bunday huquq yo'q!", false);

        Optional<PaidSalary> optional = paidSalaryRepository.findByOwnerAndPeriod(user, paidSalaryDto.getPeriod());
        if (!optional.isPresent())
            return new ApiResponse("Oylik mavjud emas!", false);

        if (optional.get().isPaid())
            return new ApiResponse("Oylik to'langan, o'zgartirish mumkin emas!", false);


        PaidSalary paidSalary = optional.get();
        paidSalary.setAmount(paidSalaryDto.getAmount());
        paidSalary.setOwner(user);
        paidSalary.setPeriod(paidSalaryDto.getPeriod());
        paidSalaryRepository.save(paidSalary);
        return new ApiResponse("Xodimning oyligi o'zgartirildi!", true);

    }

    public ApiResponse delete(String email, String month) {
        ApiResponse response = userService.getByEmail(email);
        if (response.isSuccess()) {
            return response;
        }
        User user = (User) response.getObject();

        Set<Role> roles = user.getRoles();
        String role = RoleName.ROLE_STAFF.name();
        for (Role role1 : roles) {
            role = role1.getName().name();
        }
        boolean check = checker.check(role);

        if (!check) {
            return new ApiResponse("Sizda bunday huquq yo'q!", false);
        }
        Month period = null;
        for (Month value : Month.values()) {
            if (value.name().equals(month)) {
                period = value;
                break;
            }
        }
        if (period == null) {
            return new ApiResponse("Month topilmadi!", false);
        }

        Optional<PaidSalary> optionalPaidSalary = paidSalaryRepository.findByOwnerAndPeriod(user, period);
        if (!optionalPaidSalary.isPresent()) {
            return new ApiResponse("Oylik maosh topilmadi!", false);
        }

        if (optionalPaidSalary.get().isPaid()) {
            return new ApiResponse("Oylik tulangan, o'zgartirish mumkin emas!", false);

        }
        paidSalaryRepository.delete(optionalPaidSalary.get());
        return new ApiResponse("Salary o'chirildi", true);
    }

    public ApiResponse custom(String email, String month, boolean qwer) {
        ApiResponse response = userService.getByEmail(email);
        if (!response.isSuccess()) {
            return response;
        }
        User user = (User) response.getObject();

        Set<Role> roles = user.getRoles();
        String role = RoleName.ROLE_STAFF.name();

        for (Role role1 : roles) {
            role = role1.getName().name();

        }

        boolean check = checker.check(role);
        if (!check) {
            return new ApiResponse("sizda bunday huquq yo'q!", false);

        }

        Month month1 = null;
        for (Month value : Month.values()) {
            if (month1.name().equals(month)) {
                month1 = value;
                break;
            }
        }
        if (month1 == null) {
            return new ApiResponse("Month topilmadi!", false);
        }

        Optional<PaidSalary> optionalPaidSalary = paidSalaryRepository.findByOwnerAndPeriod(user, month1);
        if (!optionalPaidSalary.isPresent()) {
            return new ApiResponse("Salary toplimadi!", false);
        }

        PaidSalary paidSalary = optionalPaidSalary.get();
        if (paidSalary.isPaid()) {
            return new ApiResponse("Oylik to'langan, o'zgartirish mumkin emas!", false);
        }

        paidSalary.setPaid(qwer);
        return new ApiResponse("Oylik to'langanlik holati o'zgartirildi!", true);
    }

    public ApiResponse getByUser(String email) {
        ApiResponse response = userService.getByEmail(email);
        if (!response.isSuccess())
            return response;
        User user = (User) response.getObject();

        Set<Role> roles = user.getRoles();
        String role = RoleName.ROLE_STAFF.name();
        for (Role rolex : roles) {
            role = rolex.getName().name();
        }

        boolean check = checker.check(role);
        if (!check)
            return new ApiResponse("Sizda huquq yo'q!", false);

        return new ApiResponse("List by Owner", true, paidSalaryRepository.findAllByOwner(user));
    }

    public ApiResponse getByMonth(String month, User user) {
        Set<Role> roles = user.getRoles();
        String rol = RoleName.ROLE_STAFF.name();
        for (Role role : roles) {
            rol = role.getName().name();
        }
        boolean check = checker.check(rol);
        if (!check)
            return new ApiResponse("Sizda huquq yo'q", false);

        Month period = null;

        for (Month value : Month.values()) {
            if (value.name().equals(month)) {
                period = value;
                break;
            }
        }
        if (period == null)
            return new ApiResponse("Month topilmadi", false);

        return new ApiResponse("Monthlar ro'yxati", true, paidSalaryRepository.findAllByPeriod(period));
    }


}
