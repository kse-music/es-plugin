package org.wltea.analyzer.dic;


/**
 * 管理主词典、量词、停用词
 *
 * @author DingHao
 * @since 2021/3/23 14:52
 */
class DictSegments {

    private DictSegment mainDict;
    private DictSegment quantifierDict;
    private DictSegment stopWords;

    public void setMainDict(DictSegment mainDict) {
        this.mainDict = mainDict;
    }

    public void setQuantifierDict(DictSegment quantifierDict) {
        this.quantifierDict = quantifierDict;
    }

    public void setStopWords(DictSegment stopWords) {
        this.stopWords = stopWords;
    }

    public DictSegment getMainDict() {
        if(mainDict == null){
            mainDict = new DictSegment((char)0);
        }
        return mainDict;
    }

    public DictSegment getQuantifierDict() {
        if(quantifierDict == null){
            quantifierDict = new DictSegment((char)0);
        }
        return quantifierDict;
    }

    public DictSegment getStopWords() {
        if(stopWords == null){
            stopWords = new DictSegment((char)0);
        }
        return stopWords;
    }

}
