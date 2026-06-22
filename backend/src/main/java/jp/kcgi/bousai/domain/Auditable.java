package jp.kcgi.bousai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * 全エンティティ共通の監査フィールド基底クラス。
 *
 * <p>4 つの監査項目（作成日時・作成者・更新日時・更新者）は Spring Data JPA Auditing
 * （{@link AuditingEntityListener}）により、永続化（INSERT/UPDATE）時に自動設定される。
 * 手動設定や個別の AOP は行わない。</p>
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {

    /** 作成日時（自動設定・不変） */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** 作成者（自動設定・不変） */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    /** 更新日時（自動設定） */
    @LastModifiedDate
    @Column(name = "last_modified_at", nullable = false)
    private Instant lastModifiedAt;

    /** 更新者（自動設定） */
    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getLastModifiedAt() {
        return lastModifiedAt;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }
}
