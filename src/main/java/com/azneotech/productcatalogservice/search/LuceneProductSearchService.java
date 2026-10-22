package com.azneotech.productcatalogservice.search;

import com.azneotech.productcatalogservice.models.Product;
import jakarta.annotation.PreDestroy;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory Lucene index. It is rebuilt from the database on every start
 * (see {@link ProductIndexBootstrap}), so nothing here needs to be durable.
 * <p>
 * Queries are built programmatically from the analysed tokens rather than via
 * a QueryParser: user input therefore has no syntax to escape and can never
 * fail to parse. Each token is matched as an exact term (boosted per field)
 * and, for tokens of {@value #MIN_PREFIX_LENGTH}+ chars, as a lower-weight
 * prefix. Tokens are OR-ed together, so documents matching more of them
 * simply rank higher.
 * <p>
 * Typos are corrected per token: a token that has no exact or prefix match anywhere in the index is
 * matched fuzzily instead (see {@link #fuzzyTokenQuery}). Tokens that do match are never fuzzed, so a
 * correctly spelled word doesn't pull in near-miss words ("shirt" stays clear of "short").
 */
@Service
@ConditionalOnProperty(name = "catalog.search.engine", havingValue = "lucene", matchIfMissing = true)
public class LuceneProductSearchService implements IProductSearchService, AutoCloseable {

    static final String FIELD_ID = "id";
    static final String FIELD_TITLE = "title";
    static final String FIELD_DESCRIPTION = "description";
    static final String FIELD_CATEGORY = "category";

    private static final Map<String, Float> FIELD_BOOSTS = new LinkedHashMap<>();
    private static final float PREFIX_BOOST_FACTOR = 0.5f;
    private static final int MIN_PREFIX_LENGTH = 3;
    private static final int MAX_QUERY_TOKENS = 10;
    private static final int MIN_FUZZY_LENGTH = 4;
    private static final int MAX_EDITS_SHORT_TOKEN = 1;
    private static final int MAX_EDITS_LONG_TOKEN = 2;
    private static final int LONG_TOKEN_LENGTH = 6;
    // The first letter must be right: typos rarely hit it, and it keeps the fuzzy term scan cheap.
    private static final int FUZZY_PREFIX_LENGTH = 1;
    private static final float FUZZY_BOOST_FACTOR = 0.5f;

    static {
        FIELD_BOOSTS.put(FIELD_TITLE, 3f);
        FIELD_BOOSTS.put(FIELD_CATEGORY, 2f);
        FIELD_BOOSTS.put(FIELD_DESCRIPTION, 1f);
    }

    private final Directory directory;
    private final Analyzer analyzer;
    private final IndexWriter writer;
    private final SearcherManager searcherManager;

    public LuceneProductSearchService() {
        this.directory = new ByteBuffersDirectory();
        this.analyzer = new EnglishAnalyzer();
        try {
            IndexWriterConfig config = new IndexWriterConfig(analyzer)
                    .setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            this.writer = new IndexWriter(directory, config);
            // A commit point must exist before a reader can be opened on the directory.
            this.writer.commit();
            this.searcherManager = new SearcherManager(writer, null);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not initialise Lucene index", e);
        }
    }

    @Override
    public synchronized void index(Product product) {
        try {
            writer.updateDocument(idTerm(product.getId()), toDocument(product));
            refresh();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not index product " + product.getId(), e);
        }
    }

    @Override
    public synchronized void remove(Long productId) {
        try {
            writer.deleteDocuments(idTerm(productId));
            refresh();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not remove product " + productId + " from index", e);
        }
    }

    @Override
    public synchronized void reindexAll(Collection<Product> products) {
        try {
            writer.deleteAll();
            for (Product product : products) {
                writer.addDocument(toDocument(product));
            }
            writer.commit();
            refresh();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not rebuild product index", e);
        }
    }

    @Override
    public List<Long> search(String query, int limit) {
        List<String> tokens = analyze(query);
        if (tokens.isEmpty()) {
            return List.of();
        }
        try {
            IndexSearcher searcher = searcherManager.acquire();
            try {
                Query luceneQuery = buildQuery(searcher, tokens);
                TopDocs topDocs = searcher.search(luceneQuery, Math.max(1, limit));
                StoredFields storedFields = searcher.storedFields();
                List<Long> ids = new ArrayList<>(topDocs.scoreDocs.length);
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    ids.add(Long.valueOf(storedFields.document(scoreDoc.doc).get(FIELD_ID)));
                }
                return ids;
            } finally {
                searcherManager.release(searcher);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Product search failed", e);
        }
    }

    @PreDestroy
    @Override
    public void close() {
        try {
            IOUtils.close(searcherManager, writer, analyzer, directory);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not close Lucene index", e);
        }
    }

    /** Makes the calling thread's own writes visible to the next {@link #search}. */
    private void refresh() throws IOException {
        searcherManager.maybeRefreshBlocking();
    }

    private static Term idTerm(Long productId) {
        return new Term(FIELD_ID, String.valueOf(productId));
    }

    private static Document toDocument(Product product) {
        Document document = new Document();
        // StringField: indexed verbatim (for delete/upsert by term) and stored (for retrieval).
        document.add(new StringField(FIELD_ID, String.valueOf(product.getId()), Field.Store.YES));
        document.add(new TextField(FIELD_TITLE, nullToEmpty(product.getTitle()), Field.Store.NO));
        document.add(new TextField(FIELD_DESCRIPTION, nullToEmpty(product.getDescription()), Field.Store.NO));
        if (product.getCategory() != null) {
            document.add(new TextField(FIELD_CATEGORY, nullToEmpty(product.getCategory().getName()), Field.Store.NO));
        }
        return document;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private Query buildQuery(IndexSearcher searcher, List<String> tokens) throws IOException {
        BooleanQuery.Builder root = new BooleanQuery.Builder();
        for (String token : tokens) {
            Query exactOrPrefix = exactOrPrefixTokenQuery(token);
            boolean looksMistyped = token.length() >= MIN_FUZZY_LENGTH && searcher.count(exactOrPrefix) == 0;
            root.add(looksMistyped ? fuzzyTokenQuery(token) : exactOrPrefix, BooleanClause.Occur.SHOULD);
        }
        return root.build();
    }

    private static Query exactOrPrefixTokenQuery(String token) {
        BooleanQuery.Builder tokenQuery = new BooleanQuery.Builder();
        for (Map.Entry<String, Float> fieldBoost : FIELD_BOOSTS.entrySet()) {
            Term term = new Term(fieldBoost.getKey(), token);
            tokenQuery.add(new BoostQuery(new TermQuery(term), fieldBoost.getValue()),
                    BooleanClause.Occur.SHOULD);
            if (token.length() >= MIN_PREFIX_LENGTH) {
                tokenQuery.add(new BoostQuery(new PrefixQuery(term), fieldBoost.getValue() * PREFIX_BOOST_FACTOR),
                        BooleanClause.Occur.SHOULD);
            }
        }
        return tokenQuery.build();
    }

    /**
     * Typo-tolerant match for a token the index has never seen: up to 1 edit for tokens of 4-5 chars,
     * 2 for longer ones. An edit is an insertion, deletion, substitution or swap of adjacent letters.
     */
    private static Query fuzzyTokenQuery(String token) {
        int maxEdits = token.length() >= LONG_TOKEN_LENGTH ? MAX_EDITS_LONG_TOKEN : MAX_EDITS_SHORT_TOKEN;
        BooleanQuery.Builder tokenQuery = new BooleanQuery.Builder();
        for (Map.Entry<String, Float> fieldBoost : FIELD_BOOSTS.entrySet()) {
            Term term = new Term(fieldBoost.getKey(), token);
            tokenQuery.add(new BoostQuery(new FuzzyQuery(term, maxEdits, FUZZY_PREFIX_LENGTH),
                    fieldBoost.getValue() * FUZZY_BOOST_FACTOR), BooleanClause.Occur.SHOULD);
        }
        return tokenQuery.build();
    }

    /** Runs the raw text through the same analyzer used at index time. */
    private List<String> analyze(String rawQuery) {
        List<String> tokens = new ArrayList<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return tokens;
        }
        try (TokenStream stream = analyzer.tokenStream(FIELD_TITLE, rawQuery)) {
            CharTermAttribute termAttribute = stream.addAttribute(CharTermAttribute.class);
            stream.reset();
            while (stream.incrementToken() && tokens.size() < MAX_QUERY_TOKENS) {
                tokens.add(termAttribute.toString());
            }
            stream.end();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not analyse query", e);
        }
        return tokens;
    }
}
