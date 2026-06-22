package jp.kcgi.bousai.repository;

import jp.kcgi.bousai.domain.ChatLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * AI 問答ログのリポジトリ。
 */
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
}
