package com.kkalyan.starter;

import junit.framework.TestCase;

/**
 *
 * @author kkalyan
 */
public class LuceneIndexerTest extends TestCase {

    public LuceneIndexerTest(String testName) {
        super(testName);
    }

    public void testSearch() throws Exception {
        System.out.println(new LuceneIndexer().search());

    }

}
