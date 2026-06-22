package jp.kcgi.bousai.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * AI 問答ログ。{@code chat_logs} テーブルに対応する。
 * 後からの検証・改善のため、質問・回答・言語・参照元を記録する。
 */
@Entity
@Table(name = "chat_logs")
public class ChatLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 質問文 */
    @Column(name = "question", nullable = false)
    private String question;

    /** 回答文 */
    @Column(name = "answer", nullable = false)
    private String answer;

    /** 回答言語（ja / en / zh など） */
    @Column(name = "lang", nullable = false)
    private String lang;

    /** 参照元（カンマ区切り。RAG 導入後は検索ヒット文書のタイトル等） */
    @Column(name = "sources")
    private String sources;

    /** 作成日時（DB の DEFAULT に委ねる） */
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected ChatLog() {
        // JPA 用
    }

    public ChatLog(String question, String answer, String lang, String sources) {
        this.question = question;
        this.answer = answer;
        this.lang = lang;
        this.sources = sources;
    }

    public Long getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public String getLang() {
        return lang;
    }

    public String getSources() {
        return sources;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
