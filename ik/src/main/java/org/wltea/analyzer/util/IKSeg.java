package org.wltea.analyzer.util;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.StringReader;
import java.util.*;

/**
 *
 * @author DingHao
 * @since 2020/5/21 11:43
 */
public class IKSeg{

    private Analyzer analyzer;
    private boolean appendWithText;

    public IKSeg() {
        this(false,true);
    }

    public IKSeg(boolean smart,boolean appendWithText) {
        this.analyzer = new IKAnalyzer(smart);
        this.appendWithText= appendWithText;
    }

    public String parse(String text) {
        if(text == null || text.isEmpty()){
            return text;
        }
        Set<String> tmp = new HashSet<>();
        try(TokenStream tokenStream = analyzer.tokenStream("content",new StringReader(text))){
            tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
                tmp.add(charTermAttribute.toString());
            }
        }catch (Exception e){
            //ignore
        }
        Optional<String> reduce = tmp.stream().reduce((t1, t2) -> t1 + " " + t2);
        if(!reduce.isPresent()){
            return "";
        }
        String s = reduce.get();
        if(!tmp.contains(text) && appendWithText){
            s += " " + text;
        }
        return s;
    }

    public List<Word> parse2(String text) {
        List<Word> rs = new ArrayList<>();
        if(text == null || text.isEmpty()){
            return rs;
        }
        try(TokenStream tokenStream = analyzer.tokenStream("content",new StringReader(text))){
            OffsetAttribute offset = tokenStream.addAttribute(OffsetAttribute.class);
            CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
            TypeAttribute type = tokenStream.addAttribute(TypeAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                rs.add(new Word(offset.startOffset(),offset.endOffset() ,term.toString(),type.type()));
            }
            tokenStream.end();
        }catch (Exception e){
            //ignore
        }
        return rs;
    }

}
