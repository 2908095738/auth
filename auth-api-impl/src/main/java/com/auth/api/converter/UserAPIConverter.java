package com.auth.api.converter;

import com.auth.api.dto.UserDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserAPIConverter {

    UserDTO toDTO(com.auth.user.dto.UserDTO userDTO);

    List<UserDTO> toDTO(List<com.auth.user.dto.UserDTO> userDTO);
}
