package com.kkalyan.starter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class LuceneIndexer {

    QueryParser qp;
    IndexSearcher searcher;

    public LuceneIndexer() {
        try {
          
            indexWords();
        } catch (Exception ex) {
            Logger.getLogger(LuceneIndexer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void indexWords() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("srt.bids"));
            StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
            Directory index = new RAMDirectory();
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, analyzer);
            IndexWriter w = new IndexWriter(index, config);

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("") || line.startsWith("#")) {
                    continue;
                }
                String[] fields = line.split(",");
                if (fields.length < 3) {
                    continue;
                }
                String li = fields[0];
                String bid = fields[1];
                String keyword = fields[2];
                Document doc = new Document();
                doc.add(new StringField("line", li + "", Field.Store.YES));
                doc.add(new TextField("keyword", keyword, Field.Store.YES));
                doc.add(new TextField("bid", bid, Field.Store.YES));
                w.addDocument(doc);
            }
            br.close();
            w.close();
            IndexReader reader = DirectoryReader.open(index);
            searcher = new IndexSearcher(reader);
            qp = new QueryParser(Version.LUCENE_48, "keyword", analyzer);
        } catch (IOException ex) {
            Logger.getLogger(LuceneIndexer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public HashMap<String, Double> search() {
        ArrayList<String> userqueries = getUserQueries();

        int maxResults = 10;
        HashMap<String, Double> results = new HashMap<String, Double>();
        for (String query : userqueries) {
            try {
                Query q = qp.parse(query);
                TopScoreDocCollector collector = TopScoreDocCollector.create(maxResults, true);
                searcher.search(q, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;
                for (int i = 0; i < hits.length; ++i) {
                    int docId = hits[i].doc;
                    float score = hits[i].score;
                    if (score < 1) {
                       // continue;
                    }
                    Document d = searcher.doc(docId);
                    results.put(d.get("line"), new Double(d.get("bid")));
                    System.out.println(hits[i].score+ "; line with keywords: " + d.get("keyword") + " matched for user search: " + query);
                }
            } catch (Exception ex) {
                Logger.getLogger(LuceneIndexer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return results;

    }

    public ArrayList<String> getUserQueries() {
        BufferedReader br = null;
        ArrayList<String> words = new ArrayList<String>();

        try {
            br = new BufferedReader(new FileReader("user.profile"));
            String word;
            while ((word = br.readLine()) != null) {
                words.add(word);
            }
            return words;
        } catch (Exception ex) {
            Logger.getLogger(LuceneIndexer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(LuceneIndexer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return words;
    }

    public static void main(String[] args) {
        System.out.println(new LuceneIndexer().search());
    }

}
