package jp.kcgi.bousai.service;

import jp.kcgi.bousai.domain.Shelter;
import jp.kcgi.bousai.dto.ShelterRequest;
import jp.kcgi.bousai.dto.ShelterResponse;
import jp.kcgi.bousai.repository.ShelterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 避難所のビジネスロジック。エンティティは本層の外に出さず、DTO に変換して返す。
 */
@Service
public class ShelterService {

    private final ShelterRepository shelterRepository;

    public ShelterService(ShelterRepository shelterRepository) {
        this.shelterRepository = shelterRepository;
    }

    /**
     * 全避難所を取得する。
     *
     * @return 避難所レスポンス DTO のリスト
     */
    @Transactional(readOnly = true)
    public List<ShelterResponse> findAll() {
        return shelterRepository.findAll().stream()
                .map(ShelterResponse::from)
                .toList();
    }

    /** 避難所を新規作成する。 */
    @Transactional
    public ShelterResponse create(ShelterRequest request) {
        Shelter shelter = new Shelter(
                request.nameJa(), request.nameEn(), request.nameZh(), request.address(),
                request.lat(), request.lng(), request.capacity(), joinFacilities(request));
        return ShelterResponse.from(shelterRepository.save(shelter));
    }

    /** 避難所を更新する。存在しなければ 404。 */
    @Transactional
    public ShelterResponse update(Long id, ShelterRequest request) {
        Shelter shelter = shelterRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "避難所が見つかりません"));
        shelter.update(
                request.nameJa(), request.nameEn(), request.nameZh(), request.address(),
                request.lat(), request.lng(), request.capacity(), joinFacilities(request));
        return ShelterResponse.from(shelter);
    }

    /** 避難所を削除する。存在しなければ 404。 */
    @Transactional
    public void delete(Long id) {
        if (!shelterRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "避難所が見つかりません");
        }
        shelterRepository.deleteById(id);
    }

    /** 設備リストをカンマ区切り文字列へ。空なら null。 */
    private static String joinFacilities(ShelterRequest request) {
        List<String> facilities = request.facilities();
        if (facilities == null || facilities.isEmpty()) {
            return null;
        }
        return String.join(",", facilities);
    }
}
