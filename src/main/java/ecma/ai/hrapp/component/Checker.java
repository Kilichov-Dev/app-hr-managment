package ecma.ai.hrapp.component;

import ecma.ai.hrapp.entity.Role;
import ecma.ai.hrapp.entity.User;
import ecma.ai.hrapp.entity.enums.RoleName;
import ecma.ai.hrapp.payload.ApiResponse;
import ecma.ai.hrapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class Checker {

    @Autowired
    UserRepository userRepository;


    public boolean check(String role) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<User> optionalUser = userRepository.findById(user.getId());

        if (optionalUser.isPresent()) {
            Set<Role> roles = optionalUser.get().getRoles();
            String position = optionalUser.get().getPosition();
            if (role.equals(RoleName.ROLE_DIRECTOR.name())) return false;
            for (Role adminRole : roles) {
                if (role.equals(RoleName.ROLE_MANAGER.name()) &&
                        adminRole.getName().name().equals(RoleName.ROLE_DIRECTOR.name())) {
                    return true;
                }
            }
        }
        return false;
    }

    public ApiResponse checkForAny(String role) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> optionalUser = userRepository.findById(user.getId());
        if (optionalUser.isPresent()) {
            Set<Role> roles = optionalUser.get().getRoles();
            if (role.equals(RoleName.ROLE_DIRECTOR.name())) {
                return new ApiResponse("Error", false);
            }
            for (Role role1 : roles) {
                if (role.equals(RoleName.ROLE_MANAGER.name())
                        && role1.getName().name().equals(RoleName.ROLE_DIRECTOR.name())) {
                    return new ApiResponse("Success", true);
                }

                if (role.equals(RoleName.ROLE_STAFF.name()) &&
                        (role1.getName().name().equals(RoleName.ROLE_MANAGER.name())
                                || role1.getName().name().equals(RoleName.ROLE_DIRECTOR.name()))) {
                    return new ApiResponse("Success", true);

                }

            }
        }
        return new ApiResponse("Error!", false);

    }
}
