package com.jollychic.vietnamese;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

import java.util.Map;

/**
 * @author mars
 * @create 2018-02-11 上午10:14
 **/
public class VietnameseTokenizerFactory extends TokenizerFactory {

    public VietnameseTokenizerFactory(Map<String, String> args) {
        super(args);
    }

    public Tokenizer create(AttributeFactory attributeFactory) {
        return new VietnameseTokenizer();
    }
}
