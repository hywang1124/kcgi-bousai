package jp.kcgi.bousai.service;

import jp.kcgi.bousai.dto.ShelterResponse;
import jp.kcgi.bousai.repository.ShelterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
