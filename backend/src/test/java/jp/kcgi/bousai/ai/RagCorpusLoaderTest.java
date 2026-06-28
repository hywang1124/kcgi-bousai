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
 * 検索できることを確認するテスト（OpenAI API キーは不要、ベクトル検索のみ検証）。
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
                .topK(4)
                .similarityThreshold(0.5)
                .build());

        assertFalse(results.isEmpty(), "should retrieve at least one document above threshold");
        boolean foundRiverDoc = results.stream()
                .anyMatch(doc -> doc.getText().contains("川の防災情報")
                        || doc.getText().contains("ハザードマップ")
                        || doc.getText().contains("キキクル")
                        || doc.getText().contains("土砂災害警戒情報"));
        assertTrue(foundRiverDoc, "expected flood or landslide official-info guidance among top results");
    }

    @Test
    void retrievesEvacuationDocumentForEvacuationQuestion() {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder()
                .query("避難指示が出たらどこへ避難すればいい？")
                .topK(2)
                .similarityThreshold(0.5)
                .build());

        assertFalse(results.isEmpty(), "should retrieve at least one document above threshold");
        boolean foundEvacuationDoc = results.stream()
                .anyMatch(doc -> doc.getText().contains("警戒レベル4")
                        || doc.getText().contains("避難指示")
                        || doc.getText().contains("指定緊急避難場所"));
        assertTrue(foundEvacuationDoc, "expected evacuation guidance among top results");
    }

    @Test
    void retrievesEarthquakeActionDocumentForImmediateActionQuestion() {
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder()
                .query("地震が来たらどうすればいい？")
                .topK(4)
                .similarityThreshold(0.5)
                .build());

        assertFalse(results.isEmpty(), "should retrieve earthquake action guidance above threshold");
        boolean foundEarthquakeActionDoc = results.stream()
                .anyMatch(doc -> doc.getText().contains("まず身の安全")
                        || doc.getText().contains("丈夫な机")
                        || doc.getText().contains("頭を保護"));
        assertTrue(foundEarthquakeActionDoc, "expected earthquake immediate-action guidance among top results");
    }
}
