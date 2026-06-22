package jp.kcgi.bousai.service;

import jp.kcgi.bousai.domain.Role;
import jp.kcgi.bousai.domain.User;
import jp.kcgi.bousai.dto.RegisterRequest;
import jp.kcgi.bousai.dto.UserResponse;
import jp.kcgi.bousai.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * ユーザと役割の管理ロジック。
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 公開セルフ登録。既定で {@link Role#USER}。ユーザ名重複時は 409。
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "このユーザ名は既に使用されています");
        }
        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),
                Role.USER,
                true);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    /**
     * 役割を変更する（管理者用）。対象が存在しなければ 404。
     */
    @Transactional
    public UserResponse updateRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザが見つかりません"));
        user.changeRole(role);
        return UserResponse.from(user);
    }
}
