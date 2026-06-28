package jp.kcgi.bousai.config;

import jp.kcgi.bousai.ai.ChatAssistant;
import jp.kcgi.bousai.ai.MockChatAssistant;
import jp.kcgi.bousai.ai.SpringAiChatAssistant;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * {@link ChatAssistant} の実装を切り替える設定。
 *
 * <p>環境変数 {@code OPENAI_API_KEY}（= {@code spring.ai.openai.api-key}）が設定されている場合は
 * Spring AI（OpenAI）実装、未設定の場合はモック実装を使う。</p>
 */
@Configuration
public class AiAssistantConfig {

    /** OpenAI API キーが設定されているかどうかの条件。 */
    static class OpenAiApiKeyPresent implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String key = context.getEnvironment().getProperty("spring.ai.openai.api-key", "");
            return !key.isBlank();
        }
    }

    @Bean
    @Conditional(OpenAiApiKeyPresent.class)
    public ChatAssistant springAiChatAssistant(OpenAiChatModel chatModel) {
        return new SpringAiChatAssistant(chatModel);
    }

    @Bean
    @ConditionalOnMissingBean(ChatAssistant.class)
    public ChatAssistant mockChatAssistant() {
        return new MockChatAssistant();
    }
}
