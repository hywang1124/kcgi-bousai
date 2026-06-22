package jp.kcgi.bousai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 危険区域エンティティ。{@code hazard_zones} テーブルに対応する。
 *
 * <p>区域几何は GeoJSON ジオメトリ文字列（{@code geojson}）で保持する（PostGIS 非依存）。
 * 監査フィールドは {@link Auditable} を参照。</p>
 */
@Entity
@Table(name = "hazard_zones")
public class HazardZone extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 災害種別 */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private HazardType type;

    /** 危険度 */
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private Severity severity;

    @Column(name = "name_ja", nullable = false)
    private String nameJa;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "name_zh")
    private String nameZh;

    @Column(name = "description")
    private String description;

    /** GeoJSON ジオメトリ文字列（Polygon 等） */
    @Column(name = "geojson", nullable = false)
    private String geojson;

    protected HazardZone() {
        // JPA 用
    }

    public Long getId() {
        return id;
    }

    public HazardType getType() {
        return type;
    }

    public Severity getSeverity() {
        return severity;
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

    public String getDescription() {
        return description;
    }

    public String getGeojson() {
        return geojson;
    }
}
