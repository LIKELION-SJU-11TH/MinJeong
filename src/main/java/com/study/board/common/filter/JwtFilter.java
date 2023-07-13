package com.study.board.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.board.common.exception.BaseException;
import com.study.board.common.response.BaseResponse;
import com.study.board.repository.UserRepository;
import com.study.board.util.JwtUtils;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;


    /*
    uriList 변수에는 토큰이 필요하지 않은 URI 패턴들이 포함되어 있음
    /user/login, /user/signup: 인증을 받지 않아도 접근이 가능한 URI -> 로그인 및 회원가입과 같은 경로에는 인증 토큰이 필요하지 않음
    
    doFilterInternal 메서드: Http 요청을 필터링하고 처리하는 역할
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        List<String> uriList = Arrays.asList(
                "/user/login",
                "/user/signup"
        );

        // 1. Token이 필요 없는 경우
        // 2. Http Method가 OPTIONS인 경우
        // 1, 2와 같은 경우는 JWT 토큰 확인하지 않음
        // OPTIONS: 브라우저가 서버에게 지원하는 옵션들을 미리 요청하는 목적 (preflight)

        // 요청된 URI가 userList에 포함되어 있는 경우 or 요청의 HTTP 메서드가 OPTIONS인 경우
        // OPTIONS: 브라우저에서 서버로 사전 요청을 보내어 서버가 허용하는 옵셥을 미리 확인하는 것을 말함 == preflight
        if (uriList.contains(request.getRequestURI()) || request.getMethod().equalsIgnoreCase("OPTION")) {
            if (uriList.contains(request.getRequestURI())) {
                log.info("NO NEED TOKEN URI : {}", request.getRequestURI());
            }

            filterChain.doFilter(request, response);
            return;
        }

        // 토큰이 필요한 경우
        try {
            String token = jwtUtils.getJwt();       // 현재 요청에서 사용될 jwt 토큰을 가져옴

            Authentication authentication = jwtUtils.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);   // 인증 객체 설정
            filterChain.doFilter(request, response);
        } catch (BaseException e) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            PrintWriter printWriter = response.getWriter();     // 응답을 출력하기 위해

            // BaseException에서 가져온 상태 코드를 사용하여 객체 생성
            // 응답의 상태코드와 메시지를 포함함
            BaseResponse<Object> baseResponse = new BaseResponse<>(e.getStatus());

            // BaseResponse 객체를 JSON 형식의 문자열로 변환
            String jsonRes = new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(baseResponse);
            printWriter.print(jsonRes);     // 출력
            printWriter.flush();    // 출력 버퍼를 비움
            printWriter.close();    // 출력 스트림을 닫음
        }
    }


}
