package jp.kcgi.bousai.config;

import jp.kcgi.bousai.ai.ChatAssistant;
import jp.kcgi.bousai.ai.MockChatAssistant;
import jp.kcgi.bousai.ai.RagCorpusLoader;
import jp.kcgi.bousai.ai.SpringAiChatAssistant;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
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
 * {@link ChatAssistant} の実装切り替え（OPENAI.md §6）。
 *
 * <p>環境変数 {@code OPENAI_API_KEY}（= {@code spring.ai.openai.api-key}）が
 * 設定されている場合は Spring AI（OpenAI + RAG）実装、未設定の場合はモック実装を使う。
 * RAG の語料（防災文書、{@code classpath:rag-corpus/}）は {@link RagCorpusLoader} が
 * 起動時にベクトルストアへ投入する。</p>
 */
@Configuration
public class AiAssistantConfig {

    /**
     * 多言語対応の埋め込みモデル（ONNX）。既定の {@code all-MiniLM-L6-v2} は英語中心で
     * 日本語の類似度スコアが低く RAG の類似度閾値（0.5）を満たせないため、多言語モデルに
     * 差し替える。
     */
    private static final String MULTILINGUAL_MODEL_URI =
            "https://huggingface.co/Xenova/paraphrase-multilingual-MiniLM-L12-v2/resolve/main/onnx/model.onnx";
    private static final String MULTILINGUAL_TOKENIZER_URI =
            "https://huggingface.co/Xenova/paraphrase-multilingual-MiniLM-L12-v2/resolve/main/tokenizer.json";

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
    public EmbeddingModel embeddingModel() throws Exception {
        TransformersEmbeddingModel embeddingModel = new TransformersEmbeddingModel();
        embeddingModel.setModelResource(MULTILINGUAL_MODEL_URI);
        embeddingModel.setTokenizerResource(MULTILINGUAL_TOKENIZER_URI);
        embeddingModel.afterPropertiesSet();
        return embeddingModel;
    }

    @Bean
    @Conditional(OpenAiApiKeyPresent.class)
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    @Conditional(OpenAiApiKeyPresent.class)
    public ChatAssistant springAiChatAssistant(OpenAiChatModel chatModel, VectorStore vectorStore) {
        return new SpringAiChatAssistant(chatModel, vectorStore);
    }

    @Bean
    @Conditional(OpenAiApiKeyPresent.class)
    public RagCorpusLoader ragCorpusLoader(VectorStore vectorStore) {
        return new RagCorpusLoader(vectorStore);
    }

    @Bean
    @ConditionalOnMissingBean(ChatAssistant.class)
    public ChatAssistant mockChatAssistant() {
        return new MockChatAssistant();
    }
}
