package com.jollychic.vietnamese;

/**
 * @author mars
 * @create 2018-02-11 下午3:05
 **/

import vn.hus.nlp.tokenizer.segmenter.AbstractResolver;

public final class TokenizerProvider {

    private static TokenizerProvider tokenizerProvider=null;

    private final Segmenter segmenter;
    private final Tokenizer tokenizer;
    private static boolean instanceFlag = false;

    private TokenizerProvider() {
        AbstractResolver resolver = new UnigramResolver(VietnameseAnalyzer.classPath+"resources/bigram/unigram.xml");
        System.out.println("Creating lexical segmenter...");
        this.segmenter = new Segmenter(resolver);
        System.out.println("Lexical segmenter created.");
        System.out.print("Initializing tokenizer...");
        this.tokenizer = new Tokenizer(VietnameseAnalyzer.classPath+"resources/lexers/lexers.xml", this.segmenter);
        System.out.println("OK");
    }

    public static TokenizerProvider getInstance() {
        if(!instanceFlag) {
            tokenizerProvider = new TokenizerProvider();
            if (tokenizerProvider !=null)
                instanceFlag = true;
        } else {
            System.out.println("The tokenizer provider has already existed.");
        }
        return tokenizerProvider;
    }

    public Segmenter getSegmenter() {
        return this.segmenter;
    }

    public Tokenizer getTokenizer() {
        return this.tokenizer;
    }

    public void dispose() {
        this.tokenizer.dispose();
    }
}
