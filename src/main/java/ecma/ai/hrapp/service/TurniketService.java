package ecma.ai.hrapp.service;

import ecma.ai.hrapp.component.Checker;
import ecma.ai.hrapp.component.MailSender;
import ecma.ai.hrapp.entity.Company;
import ecma.ai.hrapp.entity.Role;
import ecma.ai.hrapp.entity.Turniket;
import ecma.ai.hrapp.entity.User;
import ecma.ai.hrapp.entity.enums.RoleName;
import ecma.ai.hrapp.payload.ApiResponse;
import ecma.ai.hrapp.payload.TurniketDto;
import ecma.ai.hrapp.repository.CompanyRepository;
import ecma.ai.hrapp.repository.TurniketRepository;
import ecma.ai.hrapp.security.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.Optional;
import java.util.Set;

@Service
public class TurniketService {
    @Autowired
    TurniketRepository turniketRepository;

    @Autowired
    Checker checker;

    @Autowired
    UserService userService;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    MailSender mailSender;

    @Autowired
    JwtProvider jwtProvider;

    public ApiResponse add(TurniketDto turniketDto) throws MessagingException {
        ApiResponse response = userService.getByEmail(turniketDto.getOwnerEmail());
        if (!response.isSuccess())
            return response;


        User user = (User) response.getObject();

        Optional<Company> optionalCompany = companyRepository.findById(turniketDto.getCompanyId());
        if (!optionalCompany.isPresent()) {
            return new ApiResponse("Company not found!", false);
        }

        Turniket turniket = new Turniket();
        turniket.setCompany(optionalCompany.get());
        turniket.setOwner(user);
        assert !turniketDto.isEnabled();
        turniket.setEnabled(turniket.isEnabled());
        Turniket save = turniketRepository.save(turniket);
        mailSender.mailTextTurniketStatus(save.getOwner().getEmail(), save.isEnabled());
        return new ApiResponse("Turniket successfully created!", true);
    }

    public ApiResponse edit(String number, TurniketDto turniketDto) throws MessagingException {
        Optional<Turniket> optionalTurniket = turniketRepository.findByNumber(number);
        if (optionalTurniket.isPresent()) {
            return new ApiResponse("Turniket not found!", false);
        }
        Turniket turniket = optionalTurniket.get();
        turniket.setEnabled(turniketDto.isEnabled());
        Turniket save = turniketRepository.save(turniket);
        mailSender.mailTextTurniketStatus(save.getOwner().getEmail(), save.isEnabled());
        return new ApiResponse("Truniket success editing!", false);

    }

    public ApiResponse delete(String number) {
        Optional<Turniket> optionalTurniket = turniketRepository.findByNumber(number);
        if (!optionalTurniket.isPresent())
            return new ApiResponse("Turniket not found!", false);


        Set<Role> roles = optionalTurniket.get().getOwner().getRoles();
        String role = null;
        for (Role roleName : roles) {
            role = roleName.getName().name();
            break;
        }
        boolean check = checker.check(role);

        if (!check)
            return new ApiResponse("Sizda bunday role yo'q!", false);

        turniketRepository.delete(optionalTurniket.get());
        return new ApiResponse("Turniket deleted!", true);
    }

    public ApiResponse getNumber(String number) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ApiResponse byEmail = userService.getEmailforCustom(user.getEmail());

        if (!byEmail.isSuccess()) {
            return byEmail;
        }

        Optional<Turniket> byNumber = turniketRepository.findByNumber(number);

        if (!byNumber.isPresent()) {
            return new ApiResponse("TUrniket not found!", false);
        }

        Set<Role> roles = user.getRoles();
        String role = null;
        for (Role role1 : roles) {
            role = role1.getName().name();
            break;
        }

        boolean check = checker.check(role);
        if (byNumber.get().getOwner().getEmail().equals(user.getEmail()) || check) {
            return new ApiResponse("Success", true, byNumber.get());
        }
        return new ApiResponse("Sizda bunday role yo'q", false);

    }

    public ApiResponse getAll() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ApiResponse byEmail = userService.getEmailforCustom(user.getEmail());
        if (!byEmail.isSuccess())
            return byEmail;

        Set<Role> roles = user.getRoles();
        String role = RoleName.ROLE_STAFF.name();
        for (Role roleName : roles) {
            role = roleName.getName().name();
            break;
        }

        if (role.equals(RoleName.ROLE_DIRECTOR.name()))
            return new ApiResponse("Turniket List", true, turniketRepository.findAll());

        return new ApiResponse("Turniket List", true, turniketRepository.findAllByOwner(user));
    }

    public ApiResponse getByUser(User user) {
        Set<Role> roles = user.getRoles();
        String role = RoleName.ROLE_STAFF.name();
        for (Role roleName : roles) {
            role = roleName.getName().name();
            break;
        }
        boolean check = checker.check(role);
        if (!check)
            return new ApiResponse("Sizga mumkin emas!", false);

        Optional<Turniket> optionalTurniket = turniketRepository.findAllByOwner(user);
        return new ApiResponse("Userning turniket ro'yxati", true, optionalTurniket);
    }


}
