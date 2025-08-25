package capstone.capstone_shop.dto;

import capstone.capstone_shop.domain.UserRole;

import java.io.Serializable;

public record LoginUserDto (
    Long id, String name,String idUser, UserRole userRole
) implements Serializable {}
