package jp.kcgi.bousai.controller;

import jakarta.validation.Valid;
import jp.kcgi.bousai.dto.UpdateRoleRequest;
import jp.kcgi.bousai.dto.UserResponse;
import jp.kcgi.bousai.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ユーザ・役割の管理 API（ADMIN 必須。アクセス制御は {@code SecurityConfig}）。
 */
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    /** ユーザ一覧。 */
    @GetMapping
    public List<UserResponse> list() {
        return userService.findAll();
    }

    /** ユーザの役割を変更する。 */
    @PutMapping("/{id}/role")
    public UserResponse updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        return userService.updateRole(id, request.role());
    }
}
