package jp.kcgi.bousai.repository;

import jp.kcgi.bousai.domain.Shelter;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 避難所リポジトリ。Spring Data JPA により基本的な CRUD を提供する。
 */
public interface ShelterRepository extends JpaRepository<Shelter, Long> {
}
