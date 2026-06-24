package jp.kcgi.bousai.ai;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.ApplicationArguments;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RagCorpusLoader} が実際に公式サイトの語料を埋め込み、関連する文書を
 * 検索できることを確認するテスト（Claude API キーは不要、ベクトル検索のみ検証）。
 */
class RagCorpusLoaderTest {

    private static final File PERSIST_FILE = new File("data/vector-store-test.json");

    private static EmbeddingModel embeddingModel;
    private static SimpleVectorStore vectorStore;

    @BeforeAll
    static void setUp() throws Exception {
        if (PERSIST_FILE.exists()) {
            PERSIST_FILE.delete();
        }
        TransformersEmbeddingModel multilingual = new TransformersEmbeddingModel();
        multilingual.setModelResource("https://huggingface.co/Xenova/paraphrase-multilingual-MiniLM-L12-v2/resolve/main/onnx/model.onnx");
        multilingual.setTokenizerResource("https://huggingface.co/Xenova/paraphrase-multilingual-MiniLM-L12-v2/resolve/main/tokenizer.json");
        multilingual.afterPropertiesSet();
        embeddingModel = multilingual;
        vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        new RagCorpusLoader(vectorStore, PERSIST_FILE).run(null);
    }

    @AfterAll
    static void tearDown() {
        if (PERSIST_FILE.exists()) {
            PERSIST_FILE.delete();
        }
    }

    @Test
    void retrievesRiverFloodDocumentForFloodQuestion() {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder()
                .query("洪水や土砂災害のときに確認できる情報は？")
                .topK(1)
                .similarityThreshold(0.5)
                .build());

        assertFalse(results.isEmpty(), "should retrieve at least one document above threshold");
        boolean foundRiverDoc = results.stream()
                .anyMatch(doc -> doc.getText().contains("国土交通省"));
        assertTrue(foundRiverDoc, "expected MLIT river-disaster document as top result");
    }

    @Test
    void retrievesTokyoDocumentForEvacuationQuestion() {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder()
                .query("東京都の地震対策や避難情報について教えて")
                .topK(2)
                .similarityThreshold(0.5)
                .build());

        assertFalse(results.isEmpty(), "should retrieve at least one document above threshold");
        boolean foundTokyoDoc = results.stream()
                .anyMatch(doc -> doc.getText().contains("東京都"));
        assertTrue(foundTokyoDoc, "expected Tokyo metropolitan document among top results");
    }
}
