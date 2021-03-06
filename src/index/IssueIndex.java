package index;

import manager.AndroidIssueManager;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

/**
 * File-Based Index Extension for Google Android Issues files (using Lucene).
 */
public final class IssueIndex {
    private static final String INDEX_NAME = ".aosp_issue_index";
    private static final String PATH_FIELD_TAG = "path";
    private static final String MODIFIED_FIELD_TAG = "modified";
    private static final String CONTENT_FIELD_TAG = "contents";

    private IssueIndex() {
    }

    /**
     * Return if the index file exists.
     *
     * @return
     */
    public static boolean exists() {
        return Files.exists(Paths.get(getIndexDirectoryPath()));
    }

    private static String getIndexDirectoryPath() {
        File pluginDirectory = AndroidIssueManager.getPluginDirectory();
        File indexFile = new File(pluginDirectory, INDEX_NAME);
        return indexFile.getAbsolutePath();
    }

    public static void indexIssueDirectory() throws IllegalAccessException, IOException {
        String indexDirectoryPath = getIndexDirectoryPath();
        boolean createIndex = !exists();
        final Path indexDir = Paths.get(indexDirectoryPath);
        final Path issueDir = Paths.get(AndroidIssueManager.getIssueDirectory().getAbsolutePath());

        if (!Files.isReadable(issueDir)) {
            throw new IllegalAccessException("Issues directory is not readable");
        }

        long start = System.currentTimeMillis();
        try {
            System.out.println("Indexing to directory '" + indexDirectoryPath + "'...");

            FSDirectory dir = FSDirectory.open(indexDir);
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);

            indexWriterConfig.setOpenMode(createIndex ? IndexWriterConfig.OpenMode.CREATE
                    : IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

            IndexWriter writer = new IndexWriter(dir, indexWriterConfig);
            indexIssues(writer, issueDir);
            writer.close();
        } catch (IOException io) {
            io.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("Indexing took " + (end - start) + " msecs.");
    }

    /**
     * Delete the existing index
     * @throws IllegalAccessException
     * @throws IOException
     */
    public static void deleteIndex() throws IllegalAccessException, IOException {
        String indexDirectoryPath = getIndexDirectoryPath();
        boolean exists = exists();
        final Path indexDir = Paths.get(indexDirectoryPath);

        if (!exists) {
            System.out.println("No index to delete, returning...");
            return;
        }
        try {
            System.out.println("Deleting index in directory '" + indexDirectoryPath + "'...");
            FSDirectory dir = FSDirectory.open(indexDir);
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(new StandardAnalyzer());
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(dir, indexWriterConfig);
            writer.deleteAll();
            writer.commit();
            writer.close();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    /**
     * Indexes the given file using the given writer, or if a directory is given,
     * recurse over files and directories under the given directory.
     * <p>
     * This method indexes one document per input file. For good throughput, put multiple documents into your
     * input file(s).
     *
     * @param writer
     * @param path
     * @throws IOException
     */
    private static void indexIssues(final IndexWriter writer, final Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        indexFile(writer, file, attrs.lastModifiedTime().toMillis());
                    } catch (IOException ignoreD) {
                        //don't index files that can't be read.
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            indexFile(writer, path, Files.getLastModifiedTime(path).toMillis());
        }
    }

    /**
     * Index a file, given a path.
     *
     * @param writer
     * @param file
     * @param lastModified
     * @throws IOException
     */
    private static void indexFile(final IndexWriter writer,
                                  final Path file,
                                  final long lastModified) throws IOException {
        Document doc = new Document();
        Field pathField = new StringField(PATH_FIELD_TAG,
                file.toString(), Field.Store.YES);
        doc.add(pathField);
        doc.add(new LongPoint(MODIFIED_FIELD_TAG, lastModified));
        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        doc.add(new TextField(CONTENT_FIELD_TAG, content, Field.Store.YES));
        if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
            System.out.println("Adding " + file);
            writer.addDocument(doc);
        } else {
            System.out.println("Updating " + file);
            writer.updateDocument(new Term("path", file.toString()), doc);
        }
    }

    private static final int MIN_NUMBER_OF_HITS = 10;

    /**
     * Execute a search on the index, given a query string.
     * @param queryString
     * @return list of issue ids as results.
     * @throws ParseException
     * @throws IOException
     */
    public static ArrayList<String> searchForTerm(@NotNull final String queryString)
            throws ParseException, IOException {
        String indexPath = getIndexDirectoryPath();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();

        QueryParser parser = new QueryParser(CONTENT_FIELD_TAG, analyzer);
        Query query = parser.parse(queryString);
        System.out.println("Searching for: " + query.toString(CONTENT_FIELD_TAG));

        TopScoreDocCollector collector = TopScoreDocCollector.create(MIN_NUMBER_OF_HITS);
        searcher.search(query, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        // 4. display term positions, and term indexes
        ArrayList<String> issueIds = new ArrayList<>();
        System.out.println("Found " + hits.length + " hits.");
        for (int i = 0; i < hits.length; i++) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get(PATH_FIELD_TAG));
            String path = d.get(PATH_FIELD_TAG);
            String issueId = AndroidIssueManager.getIssueIdFromPath(path);
            issueIds.add(issueId);
        }

        // searcher can only be closed when there
        // is no need to access the documents any more.
        reader.close();
        return issueIds;
    }
}
