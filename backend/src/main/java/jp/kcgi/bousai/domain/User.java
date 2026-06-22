package jp.kcgi.bousai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 管理者ユーザ。{@code users} テーブルに対応する。監査フィールドは {@link Auditable} を参照。
 */
@Entity
@Table(name = "users")
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ログイン ID（一意） */
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    /** BCrypt ハッシュ化済みパスワード */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** 役割（例: ADMIN）。権限は ROLE_<role> として扱う */
    @Column(name = "role", nullable = false)
    private String role;

    /** 有効フラグ */
    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    protected User() {
        // JPA 用
    }

    public User(String username, String passwordHash, String role, boolean enabled) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
