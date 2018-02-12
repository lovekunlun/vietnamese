package com.jollychic.vietnamese;

import vn.hus.nlp.lexicon.LexiconUnmarshaller;
import vn.hus.nlp.lexicon.jaxb.Corpus;
import vn.hus.nlp.lexicon.jaxb.W;
import vn.hus.nlp.tokenizer.tokens.LexerRule;
import vn.hus.nlp.tokenizer.tokens.TaggedWord;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ResultSplitter {
    private Set<String> prefix;

    public ResultSplitter() {
        this(VietnameseAnalyzer.classPath+"resources/prefix/namedEntityPrefix.xml");
    }

    public ResultSplitter(String var1) {
        LexiconUnmarshaller var2 = new LexiconUnmarshaller();
        Corpus var3 = var2.unmarshal(var1);
        List var4 = var3.getBody().getW();
        this.prefix = new HashSet();
        Iterator var5 = var4.iterator();

        while(var5.hasNext()) {
            W var6 = (W)var5.next();
            this.prefix.add(var6.getContent().toLowerCase());
        }

    }

    private boolean isPrefix(String var1) {
        return this.prefix.contains(var1.toLowerCase());
    }

    public TaggedWord[] split(TaggedWord var1) {
        String[] var2 = var1.getText().split("\\s+");
        if(var2.length > 1 && this.isPrefix(var2[0])) {
            int var3 = var2[0].length() + 1;
            String var4 = var1.getText().substring(var3);
            TaggedWord[] var5 = new TaggedWord[]{new TaggedWord(new LexerRule("word"), var2[0]), new TaggedWord(new LexerRule("name"), var4.trim())};
            return var5;
        } else {
            return null;
        }
    }
}
