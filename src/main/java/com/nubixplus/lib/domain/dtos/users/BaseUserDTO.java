package com.nubixplus.lib.domain.dtos.users;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.dtos.BaseDTO;
import com.nubixplus.lib.domain.entities.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Objects;
import java.util.Optional;

/** Identidad del usuario, sin datos sensibles. */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseUserDTO extends BaseDTO {

    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String avatarUrl;

    public BaseUserDTO(User user) {
        super(user);
        if (Objects.nonNull(user)) {
            this.email = user.getEmail();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.fullName = user.getFullName();
            this.avatarUrl = user.getAvatarUrl();
        }
    }

    public static BaseUserDTO of(User user) {
        return Optional.ofNullable(user).map(BaseUserDTO::new).orElse(null);
    }
}
