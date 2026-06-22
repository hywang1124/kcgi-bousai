package jp.kcgi.bousai.controller;

import jakarta.validation.Valid;
import jp.kcgi.bousai.dto.ShelterRequest;
import jp.kcgi.bousai.dto.ShelterResponse;
import jp.kcgi.bousai.service.ShelterService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 避難所 REST API。
 *
 * <p>一覧（GET）は公開。作成・更新・削除は ADMIN 限定（アクセス制御は {@code SecurityConfig}）。</p>
 */
@RestController
@RequestMapping("/api/v1/shelters")
public class ShelterController {

    private final ShelterService shelterService;

    public ShelterController(ShelterService shelterService) {
        this.shelterService = shelterService;
    }

    /** 避難所一覧を取得する（公開）。 */
    @GetMapping
    public List<ShelterResponse> list() {
        return shelterService.findAll();
    }

    /** 避難所を新規作成する（ADMIN）。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShelterResponse create(@Valid @RequestBody ShelterRequest request) {
        return shelterService.create(request);
    }

    /** 避難所を更新する（ADMIN）。 */
    @PutMapping("/{id}")
    public ShelterResponse update(@PathVariable Long id, @Valid @RequestBody ShelterRequest request) {
        return shelterService.update(id, request);
    }

    /** 避難所を削除する（ADMIN）。 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        shelterService.delete(id);
    }
}
