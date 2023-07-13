package com.study.board.controller;

import com.study.board.common.exception.BaseException;
import com.study.board.common.response.BaseResponse;
import com.study.board.model.jwt.PostJwtRes;
import com.study.board.model.user.GetUserRes;
import com.study.board.model.user.LoginReq;
import com.study.board.model.user.LogoutReq;
import com.study.board.model.user.SignUpUserReq;
import com.study.board.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    private final UserService userService;

    /**
     * 회원가입 기능
     * <p>
     * * BindingResult : 검증오류가 발생할 시 오류 내용을 보관하는 객체.
     *
     * @param signUpUserReq (name, age, email, password)
     * @param result        ()
     * @return
     */
    @PostMapping("/signup")
    public BaseResponse<String> createUser(@RequestBody @Valid SignUpUserReq signUpUserReq, BindingResult result) {
        if (result.hasErrors()) {
            String errorMessage = result.getFieldError().getDefaultMessage();// 에러의 메시지를 빼올 수 있음. (getFieldError() : 해당 필드의 에러 내용을 모두 빼옴. getDefaultMessage() : 에러 중 메시지를 빼옴.)
            return new BaseResponse<>(false, HttpStatus.NOT_ACCEPTABLE.value(), errorMessage); //error 직접 생성.
        }

        try {
            userService.createUser(signUpUserReq);
            return new BaseResponse<>("회원가입에 성공하였습니다.");
        } catch (BaseException e) { //만약 UserService의 createUser()에서 에러가 말생하면 catch 구문 실행
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 회원 전부 조회하기 기능
     *
     * @return
     */
    @GetMapping("/")
    public BaseResponse<List<GetUserRes>> getUser() {
        try {
            List<GetUserRes> getUserRes = userService.getUsers();
            return new BaseResponse<>(getUserRes);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 단일 유저 조회
     * case 1. UserService의 getUserById 메서드를 통해 GetUserRes 받아와서 반환.
     * case 2. 만약 UserService의 getUserById에서 예러 발생 시 null값 반환.
     *
     * @RequestParam으로 userId 받아와서 조회
     */
    @GetMapping(value = "/", params = "userId")
    public BaseResponse<GetUserRes> getUserById(@RequestParam Long userId) {
        log.info("GET USER BY ID CONTROLLER");
        try {
            GetUserRes userRes = userService.getUserById(userId);
            return new BaseResponse<>(userRes);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 세션을 활용한 로그인 기능.
     * @param loginReq
     * @param request
     * @return
     */
    @PostMapping("/session-login")
    public BaseResponse<String> sessionLogin(@RequestBody LoginReq loginReq, HttpServletRequest request, HttpServletResponse response) {
        try {
            userService.sessionLogin(loginReq, request, response);
            return new BaseResponse<>("로그인에 성공하였습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 세션 활용 로그아웃 기능
     * @param request
     * @return
     */
    @PostMapping("/session-logout")
    public BaseResponse<String> sessionLogout(@RequestBody LogoutReq logoutReq,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        try {
            userService.sessionLogout(logoutReq, request, response);
            return new BaseResponse<>("성공적으로 로그아웃되었습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    /**
     * 쿠키 만들어 보기.
     * @param response
     * @return
     */
    @PostMapping("/set-cookie")
    public String setCookies(HttpServletResponse response) {
        Cookie cookie = new Cookie("MinJeongCookie", "cookie-cookie");
        cookie.setPath("/");
        response.addCookie(cookie);
        return "ok";
    }

    @PostMapping("/login")
    public BaseResponse<PostJwtRes> login(@RequestBody @Valid LoginReq loginReq, BindingResult result) {
        if (result.hasErrors()) {
            String errorMessage = result.getFieldError().getDefaultMessage();// 에러의 메시지를 빼올 수 있음. (getFieldError() : 해당 필드의 에러 내용을 모두 빼옴. getDefaultMessage() : 에러 중 메시지를 빼옴.)
            return new BaseResponse<>(false, HttpStatus.NOT_ACCEPTABLE.value(), errorMessage); //error 직접 생성.
        }

        try {
            PostJwtRes postJwtRes = userService.login(loginReq);
            return new BaseResponse<>(postJwtRes);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }
}
