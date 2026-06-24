package jp.kcgi.bousai.ai;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SpringAiChatAssistant} が組み立てる RAG Advisor の振る舞いを、実際の
 * 防災資料（{@code rag-corpus}）とベクトルストアを使って検証する。
 *
 * <p>2つの不変条件を確認する：</p>
 * <ul>
 *   <li>関連資料が見つかった場合：資料の内容がプロンプトに注入される。</li>
 *   <li>関連資料が見つからない場合：ユーザーの質問文がそのまま LLM に渡される
 *       （Spring AI の既定挙動である「質問文を英語の固定文に置き換える」動作を
 *       防ぐための {@code allowEmptyContext(true)} 設定の検証）。</li>
 * </ul>
 */
class SpringAiChatAssistantRagTest {

    private static final File PERSIST_FILE = new File("data/vector-store-rag-advisor-test.json");

    private static SimpleVectorStore vectorStore;
    private static BaseAdvisor ragAdvisor;

    @BeforeAll
    static void setUp() throws Exception {
        if (PERSIST_FILE.exists()) {
            PERSIST_FILE.delete();
        }
        TransformersEmbeddingModel multilingual = new TransformersEmbeddingModel();
        multilingual.setModelResource(
                "https://huggingface.co/Xenova/paraphrase-multilingual-MiniLM-L12-v2/resolve/main/onnx/model.onnx");
        multilingual.setTokenizerResource(
                "https://huggingface.co/Xenova/paraphrase-multilingual-MiniLM-L12-v2/resolve/main/tokenizer.json");
        multilingual.afterPropertiesSet();
        EmbeddingModel embeddingModel = multilingual;

        vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        new RagCorpusLoader(vectorStore, PERSIST_FILE).run(null);

        ragAdvisor = (BaseAdvisor) SpringAiChatAssistant.buildRagAdvisor(vectorStore);
    }

    @AfterAll
    static void tearDown() {
        if (PERSIST_FILE.exists()) {
            PERSIST_FILE.delete();
        }
    }

    private static ChatClientRequest requestWithQuestion(String question) {
        return ChatClientRequest.builder()
                .prompt(new Prompt(new UserMessage(question)))
                .build();
    }

    private static final AdvisorChain NOOP_CHAIN = new AdvisorChain() {
    };

    @Test
    void injectsRetrievedContextWhenRelevantDocumentExists() {
        ChatClientRequest result = ragAdvisor.before(
                requestWithQuestion("洪水や土砂災害のときに確認できる情報は？"), NOOP_CHAIN);

        String augmentedQuery = result.prompt().getUserMessage().getText();
        assertTrue(augmentedQuery.contains("国土交通省"),
                "augmented prompt should contain retrieved MLIT document content");
    }

    @Test
    void passesThroughOriginalQuestionWhenNoRelevantDocumentExists() {
        String question = "ペットの防災グッズは何を準備すればいいですか？";

        ChatClientRequest result = ragAdvisor.before(requestWithQuestion(question), NOOP_CHAIN);

        String augmentedQuery = result.prompt().getUserMessage().getText();
        assertEquals(question, augmentedQuery,
                "with no matching document, the original question must pass through unchanged "
                        + "(not be replaced by Spring AI's default English canned message)");
    }
}
