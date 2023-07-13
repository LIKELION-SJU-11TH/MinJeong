package com.study.board.model.user;

import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
public class SignUpUserReq {
    @NotBlank(message = "이름을 입력해주세요.")
    private String name;

    private int age;

    @Email
    private String email;

    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}", message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자로 구성해주세요.")
    private String password;

    public SignUpUserReq() {
    }

    public SignUpUserReq(String name, int age, String email, String password) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.password = password;
    }
}
