package ecma.ai.hrapp.service;

import ecma.ai.hrapp.component.Checker;
import ecma.ai.hrapp.component.MailSender;
import ecma.ai.hrapp.component.PasswordGenerator;
import ecma.ai.hrapp.entity.Role;
import ecma.ai.hrapp.entity.User;
import ecma.ai.hrapp.entity.enums.RoleName;
import ecma.ai.hrapp.payload.ApiResponse;
import ecma.ai.hrapp.payload.UserDto;
import ecma.ai.hrapp.repository.RoleRepository;
import ecma.ai.hrapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    Checker checker;
    @Autowired
    PasswordGenerator passwordGenerator;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    MailSender mailSender;


    public ApiResponse add(UserDto userDto) throws MessagingException {
        Optional<Role> optionalRole = roleRepository.findById(userDto.getRoleId());
        if (!optionalRole.isPresent()) return new ApiResponse("Role id not found!", false);

        boolean check = checker.check(optionalRole.get().getName().name());//

        if (!check) {
            return new ApiResponse("Dostup netu!", false);
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            return new ApiResponse("Already exists!", false);
        }
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPosition(userDto.getPosition());

        Set<Role> roleSet = new HashSet<>();
        roleSet.add(optionalRole.get());
        user.setRoles(roleSet);

        String password = passwordGenerator.genRandomPassword(8);
        user.setPassword(passwordEncoder.encode(password));

        String code = UUID.randomUUID().toString();
        user.setVerifyCode(code);

        userRepository.save(user);

        //mail xabar yuborish kk
        boolean addStaff = mailSender.mailTextAddStaff(userDto.getEmail(), code, password);

        if (addStaff) {
            return new ApiResponse("User qo'shildi! va emailga xabar ketdi!", true);
        } else {
            return new ApiResponse("Xatolik yuz berdi", false);
        }
    }

    public ApiResponse edit(UserDto userDto) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userOptional = userRepository.findById(user.getId());
        if (!userOptional.isPresent()) {
            return new ApiResponse("Email nor found!", false);
        }

        Optional<Role> optionalRole = roleRepository.findById(userDto.getRoleId());
        if (!optionalRole.isPresent()) {
            return new ApiResponse("Role id not found!", false);
        }

        boolean existsEmail = userRepository.existsByEmailAndIdNot(userDto.getEmail(), userOptional.get().getId());

        if (existsEmail) {
            return new ApiResponse("Email already exists!", false);
        }

        user.setEmail(userDto.getEmail());
        user.setUsername(userDto.getUsername());
        Set<Role> roles = userOptional.get().getRoles();
        roles.add(optionalRole.get());
        user.setRoles(roles);
        user.setPosition(userDto.getPosition());
        userRepository.save(user);

        boolean b = mailSender.mailTextEdit(userDto.getEmail());
        if (b)
            return new ApiResponse("Successfully editing!", true);

        return new ApiResponse("Error!", false);
    }

    public ApiResponse getOne() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<User> userOptional = userRepository.findById(user.getId());
        return userOptional.map(newuser -> new ApiResponse("Tokenni olish!", true, user)).orElseGet(() -> new ApiResponse("Invalid token !", false, null));
    }

    public ApiResponse getByEmail(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (!optionalUser.isPresent()) {
            return new ApiResponse("Email not found!", false);
        }

        Set<Role> roles = optionalUser.get().getRoles();
        String role = RoleName.ROLE_STAFF.name();
        for (Role role1 : roles) {
            role = role1.getName().name();
            break;
        }

        boolean check = checker.check(role);
        if (!check) {
            return new ApiResponse("You have no such right!", false);
        }
        return new ApiResponse("Get by email!", true, optionalUser.get());

    }

    public ApiResponse verifyEmail(String email, String code) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (!optionalUser.isPresent()) {
            return new ApiResponse("Invalid request", false);
        }

        User user = optionalUser.get();
        if (user.getEmail().equals(email) && user.getVerifyCode().equals(code)) {
            user.setEnabled(true);
            user.setVerifyCode(null);
            userRepository.save(user);
            return new ApiResponse("Akkount tasdiqlandi!", true);
        }
        return new ApiResponse("Invalid request!", false);
    }

    public ApiResponse getEmailforCustom(String email){
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent())
            return new ApiResponse("Email not found!", false);

        Set<Role> roles = userOptional.get().getRoles();
        String role = RoleName.ROLE_STAFF.name();
        for (Role roleName : roles) {
            role = roleName.getName().name();
            break;
        }

        boolean check = checker.check(role);
        if (!check)
            return new ApiResponse("You have no such right!", false);

        return new ApiResponse("Get by email!",true,userOptional.get());
    }
}
