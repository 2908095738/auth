package com.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private String clinicName;

    private String token;

    private Long failureTokenTime;
}
