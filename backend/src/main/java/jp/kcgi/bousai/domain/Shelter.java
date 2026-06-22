package jp.kcgi.bousai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 避難所エンティティ。{@code shelters} テーブルに対応する。
 *
 * <p>スキーマは Flyway 管理（V1__init.sql）。本クラスは読み取り中心のため、
 * {@code createdAt} は DB 側の DEFAULT に委ね、挿入・更新対象から外している。</p>
 */
@Entity
@Table(name = "shelters")
public class Shelter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 名称（日本語・必須） */
    @Column(name = "name_ja", nullable = false)
    private String nameJa;

    /** 名称（英語） */
    @Column(name = "name_en")
    private String nameEn;

    /** 名称（中国語） */
    @Column(name = "name_zh")
    private String nameZh;

    /** 住所 */
    @Column(name = "address")
    private String address;

    /** 緯度（必須） */
    @Column(name = "lat", nullable = false)
    private Double lat;

    /** 経度（必須） */
    @Column(name = "lng", nullable = false)
    private Double lng;

    /** 収容人数 */
    @Column(name = "capacity")
    private Integer capacity;

    /** 設備（カンマ区切りの文字列） */
    @Column(name = "facilities")
    private String facilities;

    /** 作成日時（DB の DEFAULT CURRENT_TIMESTAMP に委ねる） */
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected Shelter() {
        // JPA 用
    }

    public Long getId() {
        return id;
    }

    public String getNameJa() {
        return nameJa;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getNameZh() {
        return nameZh;
    }

    public String getAddress() {
        return address;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public String getFacilities() {
        return facilities;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
