package com.nubixplus.lib.domain.dtos.users;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nubixplus.lib.domain.dtos.auth.RoleDTO;
import com.nubixplus.lib.domain.entities.user.UserOrganization;
import com.nubixplus.lib.domain.types.MembershipStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Optional;

/**
 * Usuario visto DESDE una organizacion: agrega el estado de la membresia y los
 * roles que tiene en esa compania (no en todas).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailsDTO extends UserDTO {

    private Long membershipId;
    private MembershipStatus membershipStatus;
    private String jobTitle;
    private List<RoleDTO> roles;

    protected UserDetailsDTO(UserOrganization membership) {
        super(membership.getUser());
        this.membershipId = membership.getId();
        this.membershipStatus = membership.getStatus();
        this.jobTitle = membership.getJobTitle();
        this.roles = RoleDTO.of(membership.getRoles());
    }

    public static UserDetailsDTO of(UserOrganization membership) {
        return Optional.ofNullable(membership).map(UserDetailsDTO::new).orElse(null);
    }
}
