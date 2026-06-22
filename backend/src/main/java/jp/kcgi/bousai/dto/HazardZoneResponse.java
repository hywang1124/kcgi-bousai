package jp.kcgi.bousai.dto;

import jp.kcgi.bousai.domain.HazardType;
import jp.kcgi.bousai.domain.HazardZone;
import jp.kcgi.bousai.domain.Severity;

/**
 * 危険区域のレスポンス DTO。
 *
 * @param geojson GeoJSON ジオメトリ文字列（フロントで JSON.parse して描画）
 */
public record HazardZoneResponse(
        Long id,
        HazardType type,
        Severity severity,
        String nameJa,
        String nameEn,
        String nameZh,
        String description,
        String geojson
) {
    public static HazardZoneResponse from(HazardZone zone) {
        return new HazardZoneResponse(
                zone.getId(),
                zone.getType(),
                zone.getSeverity(),
                zone.getNameJa(),
                zone.getNameEn(),
                zone.getNameZh(),
                zone.getDescription(),
                zone.getGeojson());
    }
}
