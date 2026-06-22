package jp.kcgi.bousai.controller;

import jp.kcgi.bousai.dto.ShelterResponse;
import jp.kcgi.bousai.service.ShelterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 避難所 REST API。パラメータ検証と編排のみを担い、業務は Service に委ねる。
 */
@RestController
@RequestMapping("/api/v1/shelters")
public class ShelterController {

    private final ShelterService shelterService;

    public ShelterController(ShelterService shelterService) {
        this.shelterService = shelterService;
    }

    /**
     * 避難所一覧を取得する。
     */
    @GetMapping
    public List<ShelterResponse> list() {
        return shelterService.findAll();
    }
}
