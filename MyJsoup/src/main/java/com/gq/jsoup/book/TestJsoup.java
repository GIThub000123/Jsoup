package com.gq.jsoup.book;

import org.junit.Test;

import java.util.List;
import java.util.Map;

public class TestJsoup {

    @Test
    public void testIps(){
        List<Map<String, Integer>> l =  ParserHtml.getIps();
        System.out.println(l.size());
    }

    @Test
    public void testDownloadBookUrl(){
        ParserHtml.downloadBookUrl("算法");
    }

    @Test
    public void testGetBookInfo(){
        ParserHtml.getBookInfo(ParserHtml.downloadBookUrl("算法"));
    }
}
