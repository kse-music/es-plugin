package org.wltea.analyzer.util;

/**
 * describe about this class
 *
 * @author DingHao
 * @since 2020/5/21 11:25
 */
public class Word {
    private int start;
    private int end;
    private String word;
    private String type;

    public Word(int start, int end, String word, String type) {
        this.start = start;
        this.end = end;
        this.word = word;
        this.type = type;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
