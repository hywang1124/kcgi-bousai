package jp.kcgi.bousai.ai;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 起動時に防災資料（{@code classpath:rag-corpus/*.md}）を読み込み、ベクトルストアへ
 * 投入する（RAG の語料投入）。資料は内閣府・首相官邸・国土交通省・気象庁・東京都の
 * 公式サイトから取得した内容に基づく（各ファイル冒頭に出典 URL を記載）。
 *
 * <p>{@code data/vector-store.json} に永続化し、次回起動時はファイルから復元する
 * （再埋め込みのコストを避ける）。語料を更新した場合はこのファイルを削除すれば
 * 次回起動時に再投入される。</p>
 */
public class RagCorpusLoader implements ApplicationRunner {

    private static final String DEFAULT_PERSIST_PATH = "data/vector-store.json";

    private final VectorStore vectorStore;
    private final File persisted;

    public RagCorpusLoader(VectorStore vectorStore) {
        this(vectorStore, new File(DEFAULT_PERSIST_PATH));
    }

    public RagCorpusLoader(VectorStore vectorStore, File persisted) {
        this.vectorStore = vectorStore;
        this.persisted = persisted;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (vectorStore instanceof SimpleVectorStore simpleVectorStore && persisted.exists()) {
            simpleVectorStore.load(persisted);
            return;
        }

        List<Document> documents = loadCorpusDocuments();
        if (documents.isEmpty()) {
            return;
        }
        vectorStore.add(documents);

        if (vectorStore instanceof SimpleVectorStore simpleVectorStore) {
            File parent = persisted.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            simpleVectorStore.save(persisted);
        }
    }

    private List<Document> loadCorpusDocuments() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:rag-corpus/*.md");
        TokenTextSplitter splitter = new TokenTextSplitter();

        List<Document> documents = new ArrayList<>();
        for (Resource resource : resources) {
            String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            Document document = new Document(content, Map.of("source", resource.getFilename()));
            documents.addAll(splitter.split(document));
        }
        return documents;
    }
}
