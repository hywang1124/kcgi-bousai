package jp.kcgi.bousai.config;

import jp.kcgi.bousai.ai.ChatAssistant;
import jp.kcgi.bousai.ai.MockChatAssistant;
import jp.kcgi.bousai.ai.SpringAiChatAssistant;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * {@link ChatAssistant} の実装切り替え（CLAUDE.md §6）。
 *
 * <p>環境変数 {@code ANTHROPIC_API_KEY}（= {@code spring.ai.anthropic.api-key}）が
 * 設定されている場合は Spring AI（Claude + RAG）実装、未設定の場合はモック実装を使う。
 * RAG の語料（防災文書）は現時点では未投入で、{@code VectorStore} は空のまま配線する。</p>
 */
@Configuration
public class AiAssistantConfig {

    /** Anthropic API キーが設定されているかどうかの条件。 */
    static class AnthropicApiKeyPresent implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String key = context.getEnvironment().getProperty("spring.ai.anthropic.api-key", "");
            return !key.isBlank();
        }
    }

    @Bean
    @Conditional(AnthropicApiKeyPresent.class)
    public EmbeddingModel embeddingModel() throws Exception {
        TransformersEmbeddingModel embeddingModel = new TransformersEmbeddingModel();
        embeddingModel.afterPropertiesSet();
        return embeddingModel;
    }

    @Bean
    @Conditional(AnthropicApiKeyPresent.class)
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    @Conditional(AnthropicApiKeyPresent.class)
    public ChatAssistant springAiChatAssistant(AnthropicChatModel chatModel, VectorStore vectorStore) {
        return new SpringAiChatAssistant(chatModel, vectorStore);
    }

    @Bean
    @ConditionalOnMissingBean(ChatAssistant.class)
    public ChatAssistant mockChatAssistant() {
        return new MockChatAssistant();
    }
}
