package com.study.board.service;

import com.study.board.common.exception.BaseException;
import com.study.board.common.response.BaseResponseStatus;
import com.study.board.entity.User;
import com.study.board.model.jwt.PostJwtRes;
import com.study.board.model.user.GetUserRes;
import com.study.board.model.user.LoginReq;
import com.study.board.model.user.LogoutReq;
import com.study.board.model.user.SignUpUserReq;
import com.study.board.repository.SessionRepository;
import com.study.board.repository.UserRepository;
import com.study.board.util.JwtUtils;
import com.study.board.util.SHA256;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final SessionRepository sessionRepository = SessionRepository.getInstance();


    /**
     * 유저 생성
     * @param signUpUserReq
     */
    public void createUser(SignUpUserReq signUpUserReq) throws BaseException {
        String plainPw = signUpUserReq.getPassword(); // 유저가 입력한 비밀번호 (ex. ldc1104)
        String encryptPw = SHA256.encrypt(plainPw); // 암호화된 비밀번호 (ex. d34eda6...)

        // 논리적 Validation 부분
        Optional<User> optionalUser = userRepository.findByEmail(signUpUserReq.getEmail()); // email로 DB에서 유저 검색. 유저가 없으면 optionalUser는 null
        if (optionalUser.isPresent()) { // 만약 유저가 null이 아니라 있으면 에러를 발생시킴.
            throw new BaseException(BaseResponseStatus.EXIST_EMAIL);
        }
        // 논리적 Validation 끝

        User user = User.builder()
                .age(signUpUserReq.getAge())
                .name(signUpUserReq.getName())
                .email(signUpUserReq.getEmail())
                .password(encryptPw)
                .build();

        try {
            userRepository.save(user);
        } catch (Exception e) { //저장에 실패 시 예외를 발생시킴.
            throw new BaseException(BaseResponseStatus.DATABASE_INSERT_ERROR);
        }
    }

    /**
     * 유저 전체 조회
     */
    public List<GetUserRes> getUsers() {
        List<User> userList = userRepository.findAll(); // DB에서 모든 User를 가져와 리스트에 넣기.


        List<GetUserRes> userRes = new ArrayList<>(); // 반환해줄 배열을 생성, 배열을 구성할 객체는 GetUserRes.
        for (User user : userList) {
            GetUserRes getUserEntity = new GetUserRes(user); // GetUserRes.class의 생성자를 통하여 User를 GetUserRes로 변환할 수 있게 구현. (GetUserRes.class에서 확인해보세요.)
            userRes.add(getUserEntity); //배열에 객체 추가
        }

        /*
        간단한 방법.
        List<GetUserRes> userRes = userList.stream().map(GetUserRes::new).collect(Collectors.toList());
        */

        return userRes;
    }


    /**
     * 인덱스로 유저 조회
     * 한번 구현해 보세요
     * <p>
     * 1. user_id로 user 조회 (UserRepository의 findById 이용)
     * 2. User를 GetUserRes로 변환
     * 3. GetUserRes 반환
     * <p>
     * findById로 User 조회 시 Optional<User>로 반환 -> User를 빼오는 방법 찾아보세요.
     */
    public GetUserRes getUserById(Long userId) throws BaseException {
        Optional<User> optionalUser = userRepository.findById(userId); //아이디로 유저 조회, 만약 해당 아이디의 유저가 없으면 optionalUser는 null.

        if (optionalUser.isEmpty()) { // 만약 유저 아이디에 따른 유저가 없다면 예외 발생
            throw new BaseException(BaseResponseStatus.NON_EXIST_USER);
        }

        User user = optionalUser.get(); //optionalUser가 null이 아니라면 get()메서드를 통해 User를 가져올 수 있음.
        GetUserRes getUserRes = new GetUserRes(user); // GetUserRes.class의 생성자를 통하여 User를 GetUserRes로 변환할 수 있게 구현. (GetUserRes.class에서 확인해보세요.)

        return getUserRes;
    }

    /**
     * 세션을 활용한 로그인 기능
     * 1. email 로 가입된 회원 찾기, 회원 없으면 예외 발생 (NON_EXIST_USER)
     * 2. DB에 저장되어있는 pw, 클라이언트가 입력한 pw를 암호화한 값 비교
     * 2. 일치 시 세션 생성, 쿠키에 담에서 클라이언트에게 전송. (HttpSession 이용 시 자동으로 생성 후 쿠키에 담아줌)
     * 3. 불일치 시 예외 발생 시킴 (NOT_MATCHED_PASSWORD)
     * @param loginReq (email, password)
     * @param request
     * @throws BaseException
     */
    public void sessionLogin(LoginReq loginReq,
                             HttpServletRequest request,
                             HttpServletResponse response) throws BaseException {
        Optional<User> optionalUser = userRepository.findByEmail(loginReq.getEmail());
        log.info("loginReq MAIL : {}", loginReq.getEmail());

        // 해당 email로 가입한 유저가 없을 시 예외 발생.
        if (optionalUser.isEmpty()) {
            throw new BaseException(BaseResponseStatus.NON_EXIST_USER);
        }

        User user = optionalUser.get();

        String plainPw = loginReq.getPassword();
        String encryptPw = SHA256.encrypt(plainPw);

        // 비밀번호 일치하지 않을 시 예외 발생.
        if (!Objects.equals(user.getPassword(), encryptPw)) {
            throw new BaseException(BaseResponseStatus.NOT_MATCH_PASSWORD);
        }

        // 비밀번호 일치 시 Session생성, SessionID 유저에게 전송.
        HttpSession session = request.getSession();
        session.setAttribute("userEmail", loginReq.getEmail());
        session.setMaxInactiveInterval(30*60);

        // 세션 저장.
        try {
            sessionRepository.save(session);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.DATABASE_INSERT_ERROR);
        }
    }

    /**
     * 세션 활용 로그아웃 기능
     * 1. 세션이 없으면 예외 발생
     * 2. 세션이 있으면 age 0으로 설정.
     * @param request
     * @throws BaseException
     */
    public void sessionLogout(LogoutReq logoutReq,
                              HttpServletRequest request,
                              HttpServletResponse response) throws BaseException {
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new BaseException(BaseResponseStatus.NO_SESSION_ID);
        }

        try { // 데이터 베이스에서 세션 ID삭제 실패 시 예외 발생.
            sessionRepository.deleteSessionBySessionId(session.getId());
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.DATABASE_DELETE_ERROR);
        }
        session.removeAttribute("userEmail");
        session.invalidate();
        // 로그아웃 마지막은 홈 화면으로 Redirect.
        try {
            response.sendRedirect("http://localhost:8080/");
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.REDIRECT_ERROR);
        }
    }

    public PostJwtRes login(LoginReq loginReq) {
//        User user1 = userRepository
//                      .findByEmail(loginReq.getEmail())
//                      .orElseThrow(() -> new BaseException(BaseResponseStatus.NON_EXIST_USER));

        Optional<User> opUser = userRepository.findByEmail(loginReq.getEmail());
        User user = opUser.orElseThrow(() -> new BaseException(BaseResponseStatus.NON_EXIST_USER));
        String encryptPwd;
        try {
            log.info("password : {}", loginReq.getPassword());
            encryptPwd = SHA256.encrypt(loginReq.getPassword());
        } catch (Exception exception) {
            throw new BaseException(BaseResponseStatus.PASSWORD_ENCRYPTION_ERROR);
        }
        String role = user.getRole().toString();

        if (Objects.equals(encryptPwd, user.getPassword())) {
            Map<String, String> jwtTokens = jwtUtils.generateToken(user.getId(), role);
            return new PostJwtRes(user.getId(), jwtTokens.get("accessToken"), jwtTokens.get("refreshToken"));
        } else {
            throw new BaseException(BaseResponseStatus.NON_EXIST_ARTICLE);
        }

    }
}
