package com.ittaga.ttaggg;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JsoupExample {

    @Test
    void fmkorea() throws IOException, InterruptedException {
        Document doc = Jsoup.connect("https://www.fmkorea.com/")
            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
            .referrer("http://www.google.com")
            .get();

        Elements strToAnalyze = doc.select("div div div ul li div h3 a");
        Multiset multiset = TreeMultiset.create();

        for (Element element : strToAnalyze) {
            String href = element.attr("href");
            Thread.sleep(3000L);
            Document doc2 = Jsoup.connect("https://www.fmkorea.com/" + href).get();
            String p = doc2.select("#bd_capture div article div p").text();

            if (hasKorean(p)) {
                System.out.println(p);
                Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);

                KomoranResult analyzeResultList = komoran.analyze(p);

                List<String> tokenList = analyzeResultList.getNouns();

                List<String> abc = tokenList.stream()
                    .filter(b -> b.length() >= 2)
                    .collect(Collectors.toList());
                multiset.addAll(abc);
            }
        }
        System.out.println(multiset);

    }

    public static boolean hasKorean(CharSequence charSequence) {
        boolean hasKorean = false;
        for (char c : charSequence.toString().toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HANGUL_JAMO
                || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.HANGUL_SYLLABLES) {
                hasKorean = true;
                break;
            }
        }
        return hasKorean;
    }
}
