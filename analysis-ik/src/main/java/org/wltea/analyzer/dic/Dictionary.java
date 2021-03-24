package org.wltea.analyzer.dic;

import org.wltea.analyzer.cfg.Configuration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 词典管理类,单例模式
 */
public class Dictionary {

    /*
	 * 词典单子实例
	 */
    private static volatile Dictionary singleton;

    private final DictionaryLoader dictionaryLoader;

	private Dictionary(Configuration cfg) {
        this.dictionaryLoader = new DictionaryLoader(cfg);
        dictionaryLoader.loadDict();
	}


	/**
	 * 词典初始化 由于IK Analyzer的词典采用Dictionary类的静态方法进行词典初始化
	 * 只有当Dictionary类被实际调用时，才会开始载入词典， 这将延长首次分词操作的时间 该方法提供了一个在应用加载阶段就初始化字典的手段
	 * 
	 */
	public static void initial(Configuration cfg) {
		if (singleton == null) {
			synchronized (Dictionary.class) {
				if (singleton == null) {
					singleton = new Dictionary(cfg);
				}
			}
		}
		singleton.dictionaryLoader.loadCustomDict(cfg.getCustomMainDictIdentify());
	}

	/**
	 * 获取词典单子实例
	 * 
	 * @return Dictionary 单例对象
	 */
	public static Dictionary getSingleton() {
		if (singleton == null) {
			throw new IllegalStateException("ik dict has not been initialized yet, please call initial method first.");
		}
		return singleton;
	}


	/**
	 * 批量加载新词条
	 * 
	 * @param words  Collection<String>词条列表
	 */
	public void addWords(Collection<String> words,String identify) {
		if (words != null) {
			for (String word : words) {
				if (word != null) {
					// 批量加载词条到主内存词典中
                    dictionaryLoader.getDictSegments(identify).getMainDict().fillSegment(word.trim().toCharArray());
				}
			}
		}
	}

	/**
	 * 批量移除（屏蔽）词条
	 */
	public void disableWords(Collection<String> words,String identify) {
		if (words != null) {
			for (String word : words) {
				if (word != null) {
					// 批量屏蔽词条
					dictionaryLoader.getDictSegments(identify).getMainDict().disableSegment(word.trim().toCharArray());
				}
			}
		}
	}

	/**
	 * 检索匹配主词典
	 * 
	 * @return Hit 匹配结果描述
	 */
	public Hit matchInMainDict(char[] charArray) {
		return dictionaryLoader.globalDict.getMainDict().match(charArray);
	}

	/**
	 * 检索匹配主词典
	 * 
	 * @return Hit 匹配结果描述
	 */
	public Hit matchInMainDict(char[] charArray, int begin, int length) {
		return match(dictionaryLoader.globalDict.getMainDict(),charArray, begin, length);
	}

	public List<Hit> matchInMainDict(char[] charArray, int begin, int length, String identify) {
		if(dictionaryLoader.getCustomDict().containsKey(identify)){
			return Arrays.asList(matchInMainDict(charArray, begin, length)
					,match(dictionaryLoader.getDictSegments(identify).getMainDict(),charArray, begin, length));
		}
		return Collections.singletonList(matchInMainDict(charArray, begin, length));
	}

	/**
	 * 检索匹配量词词典
	 * 
	 * @return Hit 匹配结果描述
	 */
	public Hit matchInQuantifierDict(char[] charArray, int begin, int length) {
		return match(dictionaryLoader.globalDict.getQuantifierDict(),charArray, begin, length);
	}

	public List<Hit> matchInQuantifierDict(char[] charArray, int begin, int length, String identify) {
		if(dictionaryLoader.getCustomDict().containsKey(identify)){
			return Arrays.asList(matchInQuantifierDict(charArray, begin, length)
					,match(dictionaryLoader.getDictSegments(identify).getQuantifierDict(),charArray, begin, length));
		}
		return Collections.singletonList(matchInQuantifierDict(charArray, begin, length));

	}

	private Hit match(DictSegment dictSegment,char[] charArray, int begin, int length) {
		return dictSegment.match(charArray, begin, length);
	}

	/**
	 * 从已匹配的Hit中直接取出DictSegment，继续向下匹配
	 * 
	 * @return Hit
	 */
	public Hit matchWithHit(char[] charArray, int currentIndex, Hit matchedHit) {
		DictSegment ds = matchedHit.getMatchedDictSegment();
		return ds.match(charArray, currentIndex, 1, matchedHit);
	}

	/**
	 * 判断是否是停止词
	 * 
	 * @return boolean
	 */
	public boolean isStopWord(char[] charArray, int begin, int length,String identify) {
		if(dictionaryLoader.globalDict.getStopWords().match(charArray, begin, length).isMatch()){
			return true;
		}
		return dictionaryLoader.getCustomDict().containsKey(identify) && dictionaryLoader.getDictSegments(identify).getStopWords().match(charArray, begin, length).isMatch();
	}

	public void reLoadMainDict(String identify){
        dictionaryLoader.reLoadDict(identify);
    }

}
