package com.study.board.model.user;

import com.study.board.entity.User;
import lombok.Getter;

@Getter
public class GetUserRes {
    private String email;
    private String name;
    private int age;

    public GetUserRes(User user){
        this.email = user.getEmail();
        this.name = user.getName();
        this.age = user.getAge();
    }

    public GetUserRes() { }
}
