package com.study.board.common.config;

import com.study.board.common.filter.JwtFilter;
import com.study.board.repository.UserRepository;
import com.study.board.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity      // 웹 보안
@Configuration
@Slf4j
@RequiredArgsConstructor    // 생성자 자동 생성
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        log.info("SECURITY CONFIG");
        http
                .csrf().disable()   // csrf 보안 비활성화
                .formLogin().disable()
                .authorizeRequests()    // 요청에 대해 보안검사 시작
                .antMatchers("/user/signup").permitAll()    // 해당 URI에 대해 모든 접근 허용
                .antMatchers("user/session-login").permitAll()
                .anyRequest().authenticated()       // 나머지 요청에 대해 보안검사
                .and()
                .addFilterBefore(new JwtFilter(jwtUtils, userRepository), UsernamePasswordAuthenticationFilter.class);
    }
}
