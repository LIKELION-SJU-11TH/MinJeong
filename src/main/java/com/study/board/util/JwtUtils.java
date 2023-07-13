package com.study.board.util;

import com.study.board.common.exception.BaseException;
import com.study.board.common.response.BaseResponseStatus;
import com.study.board.entity.User;
import com.study.board.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.*;

@Component
public class JwtUtils {
    private final UserRepository userRepository;

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30;    // 30분, ms
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;     // 1주일

    private final Key key;      // 비밀키, 한 번 할당된 이후에는 변경될 수 없음(final)


    public JwtUtils(@Value("${jwt.secret}") String jwtSecret, @Autowired UserRepository userRepository) {
        this.userRepository = userRepository;

        // jwtSecret 값을 디코딩하여 'keyBytes' 배열에 저장, BASE64 문자열을 디코딩
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);    // byte[]: (데이터 타입)바이트 배열
        this.key = Keys.hmacShaKeyFor(keyBytes);        // 키 생성
    }

    public Map<String, String> generateToken(Long userId, String role) {
        String accessToken = Jwts.builder()     // Jwt 생성하기 위한 빌더 객체
                .setHeaderParam("alg", "HS256")
                .setHeaderParam("type", "JWT")      // 토큰 타입
                .claim("uid", userId)       // claim: 토큰에 담길 정보
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME))    // 만료시간
                .signWith(key, SignatureAlgorithm.HS256)        // 시그니처 추가, 시그니처: 비밀키+알고리즘
                .compact();     // 최종적으로 JWT를 문자열 형태로 반환
        
        String refreshToken = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        Map<String, String> tokenInfo = new HashMap<>();
        tokenInfo.put("accessToken", accessToken);
        tokenInfo.put("refreshToken", refreshToken);

        return tokenInfo;
    }

    public String getJwtHeader() {
        HttpServletRequest request
                = ((ServletRequestAttributes) RequestContextHolder  // 현재 실행 중인 스레드에 대한 요청 관련 정보를 제공하는 클래스
                .currentRequestAttributes())    // 현재 요청에 대한 속성을 가져옴
                .getRequest();  // 현재 요청에 대한 HTTP 헤더, 파라미터 등의 정보에 접근할 수 있음

        return request.getHeader("X-ACCESS-TOKEN");
    }

    public String getJwt() throws BaseException {
        String accessToken = getJwtHeader();

        if (accessToken == null){
            throw new BaseException(BaseResponseStatus.NO_JWT);
        }
        return accessToken;
    }

    
    /*
    JWT: 클레임(토큰에 담길 정보) 정보를 포함하는 토큰의 개념
    JWS: 페이로드의 무결성을 검증하기 위해 서명된 JWT
    JWS는 JWT의 일부로, 토큰의 보안성을 강화하는데 사용됨
     */
    public Long getUserId(String accessToken) throws BaseException {
        try {
            Long userId = Jwts
                    .parserBuilder()
                    .setSigningKey(key)     // 시그니처 키 설정
                    .build()        // JwtParser 객체를 생성하기 위한 빌더 역할
                    .parseClaimsJws(accessToken)        // 시그니터의 유효성을 확인, 유효한 경우 Claims 객체를 반환
                    .getBody()      // 토큰의 payload 정보를 얻기 위해
                    .get("uid", Long.class);    // uid 값을 Long형으로 반환
            return userId;
        } catch (ExpiredJwtException expiredJwt) {
            throw new BaseException(BaseResponseStatus.EXPIRED_TOKEN);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.INVALID_TOKEN);
        }
    }

    // 사용자 인증 정보를 생성하는 메서드
    // Authentication: spring security에서 인증을 나타내는 인터페이스
    public Authentication getAuthentication(String accessToken) throws BaseException{
        User user = userRepository.findById(getUserId(accessToken)).orElseThrow(()->new BaseException(BaseResponseStatus.NON_EXIST_USER));

        // GrantedAuthority: 인증된 사용자가 갖는 권한 정보를 포현하는데 사용
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole().toString())); // 객체를 생성하고 권한 목록에 추가

        // 생성된 사용자ID, 빈 비밀번호, 권한 목록을 사용하여 객체를 생성해 반환함
        // 이 객체는 인증된 사용자 정보를 나타내는데 사용되며, spring security에서 인증 처리를 위해 사용될 수 있음
        return new UsernamePasswordAuthenticationToken(getUserId(accessToken), "", authorities);
    }

    // 현재 사용자의 JWT 토큰을 가져와서 해당 토큰에 포함된 사용자의 ID를 반환하는 역할
    public Long getUserIdV2() {
        String jwt = getJwt();
        return getUserId(jwt);
    }
}
