package jp.kcgi.bousai.dto;

import jp.kcgi.bousai.domain.Shelter;

import java.util.List;

/**
 * 避難所のレスポンス DTO。エンティティを外部に晒さないための変換先。
 *
 * @param id         ID
 * @param nameJa     名称（日本語）
 * @param nameEn     名称（英語）
 * @param nameZh     名称（中国語）
 * @param address    住所
 * @param lat        緯度
 * @param lng        経度
 * @param capacity   収容人数
 * @param facilities 設備一覧
 */
public record ShelterResponse(
        Long id,
        String nameJa,
        String nameEn,
        String nameZh,
        String address,
        Double lat,
        Double lng,
        Integer capacity,
        List<String> facilities
) {

    /**
     * エンティティから DTO を生成する。
     * {@code facilities} はカンマ区切り文字列をリストへ分解する。
     */
    public static ShelterResponse from(Shelter shelter) {
        return new ShelterResponse(
                shelter.getId(),
                shelter.getNameJa(),
                shelter.getNameEn(),
                shelter.getNameZh(),
                shelter.getAddress(),
                shelter.getLat(),
                shelter.getLng(),
                shelter.getCapacity(),
                splitFacilities(shelter.getFacilities())
        );
    }

    private static List<String> splitFacilities(String facilities) {
        if (facilities == null || facilities.isBlank()) {
            return List.of();
        }
        return List.of(facilities.split("\\s*,\\s*"));
    }
}
