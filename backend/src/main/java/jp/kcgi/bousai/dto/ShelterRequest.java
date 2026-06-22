package jp.kcgi.bousai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 避難所の作成・更新リクエスト DTO。
 *
 * @param nameJa     名称（日本語・必須）
 * @param nameEn     名称（英語）
 * @param nameZh     名称（中国語）
 * @param address    住所
 * @param lat        緯度（必須）
 * @param lng        経度（必須）
 * @param capacity   収容人数（0 以上）
 * @param facilities 設備一覧
 */
public record ShelterRequest(
        @NotBlank @Size(max = 255) String nameJa,
        @Size(max = 255) String nameEn,
        @Size(max = 255) String nameZh,
        @Size(max = 512) String address,
        @NotNull Double lat,
        @NotNull Double lng,
        @PositiveOrZero Integer capacity,
        List<String> facilities
) {
}
