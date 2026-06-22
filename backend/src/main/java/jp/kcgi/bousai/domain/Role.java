package jp.kcgi.bousai.domain;

/**
 * ユーザ役割。権限は {@code ROLE_<name>} として扱う。
 *
 * <ul>
 *   <li>{@link #ADMIN} … 全機能（ユーザ・役割管理を含む）</li>
 *   <li>{@link #EDITOR} … コンテンツ編集</li>
 *   <li>{@link #USER} … 一般（既定の登録ロール）</li>
 * </ul>
 */
public enum Role {
    ADMIN,
    EDITOR,
    USER
}
