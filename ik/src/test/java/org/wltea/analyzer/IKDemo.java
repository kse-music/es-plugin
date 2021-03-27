package org.wltea.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.search.Query;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.wltea.analyzer.query.IKQueryExpressionParser;
import org.wltea.analyzer.util.IKSeg;
import org.wltea.analyzer.util.Word;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

/**
 * 使用IKAnalyzer进行分词的演示
 * 2012-10-22
 *
 */
public class IKDemo {

    @Test
	public void ik() throws IOException {
    	String text = "这是一个中文分词的例子，你可以直接运行它！IKAnalyer can analysis english text too";
		IKSeg ikSeg = new IKSeg();
		String parse = ikSeg.parse(text);
		List<Word> parse2 = ikSeg.parse2(text);

		//构建IK分词器，使用smart分词模式
		Analyzer analyzer = new IKAnalyzer();

		//获取Lucene的TokenStream对象
	    TokenStream ts = null;
		try {
			ts = analyzer.tokenStream("myfield", new StringReader(text));
			//获取词元位置属性
		    OffsetAttribute  offset = ts.addAttribute(OffsetAttribute.class); 
		    //获取词元文本属性
		    CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
		    //获取词元文本属性
		    TypeAttribute type = ts.addAttribute(TypeAttribute.class);
		    
		    
		    //重置TokenStream（重置StringReader）
			ts.reset(); 
			//迭代获取分词结果
			while (ts.incrementToken()) {
			  System.out.println(offset.startOffset() + " - " + offset.endOffset() + " : " + term.toString() + " | " + type.type());
			}
			//关闭TokenStream（关闭StringReader）
			ts.end();   // Perform end-of-stream operations, e.g. set the final offset.

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			//释放TokenStream的所有资源
			if(ts != null){
		      try {
				ts.close();
		      } catch (IOException e) {
				e.printStackTrace();
		      }
			}
	    }
		
	}

	@Test
	public void query(){
		IKQueryExpressionParser parser = new IKQueryExpressionParser();
		//String ikQueryExp = "newsTitle:'的两款《魔兽世界》插件Bigfoot和月光宝盒'";
		String ikQueryExp = "(id='ABcdRf' && date:{'20010101','20110101'} && keyword:'魔兽中国') || (content:'KSHT-KSH-A001-18'  || ulr='www.ik.com') - name:'林良益'";
		Query result = parser.parseExp(ikQueryExp , true);
		System.out.println(result);
	}

}
