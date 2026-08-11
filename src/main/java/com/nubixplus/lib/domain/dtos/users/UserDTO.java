package com.nubixplus.lib.domain.dtos.users;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.entities.user.User;
import com.nubixplus.lib.domain.types.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO extends BaseUserDTO {

    private String phone;
    private UserStatus status;
    private boolean emailVerified;
    private boolean mustChangePassword;
    private LocalDateTime lastLoginAt;

    protected UserDTO(User user) {
        super(user);
        this.phone = user.getPhone();
        this.status = user.getStatus();
        this.emailVerified = user.isEmailVerified();
        this.mustChangePassword = user.isMustChangePassword();
        this.lastLoginAt = user.getLastLoginAt();
    }

    public static UserDTO of(User user) {
        return Optional.ofNullable(user).map(UserDTO::new).orElse(null);
    }
}
