package com.example.user.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class UserDto {
    private String email;
    private String pwd;
    private String name;
    private String userId;
    private Date createdAt;

    private String encryptedPwd;

    private List<ResponseOrder> ordersList;
}
