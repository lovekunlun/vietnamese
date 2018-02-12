package com.jollychic.vietnamese;

/**
 * @author mars
 * @create 2018-02-11 下午3:31
 **/
import vn.hus.nlp.lexicon.LexiconUnmarshaller;
import vn.hus.nlp.lexicon.jaxb.Corpus;
import vn.hus.nlp.lexicon.jaxb.W;
import vn.hus.nlp.tokenizer.segmenter.AbstractLexiconRecognizer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ExternalLexiconRecognizer extends AbstractLexiconRecognizer {
    private Set<String> externalLexicon;

    public ExternalLexiconRecognizer() {
        this(VietnameseAnalyzer.classPath+"resources/automata/externalLexicon.xml");
    }

    public ExternalLexiconRecognizer(String var1) {
        LexiconUnmarshaller var2 = new LexiconUnmarshaller();
        Corpus var3 = var2.unmarshal(var1);
        List var4 = var3.getBody().getW();
        this.externalLexicon = new HashSet();
        Iterator var5 = var4.iterator();

        while(var5.hasNext()) {
            W var6 = (W)var5.next();
            this.externalLexicon.add(var6.getContent().toLowerCase());
        }

        System.out.println("External lexicon loaded.");
    }

    public boolean accept(String var1) {
        return this.externalLexicon.contains(var1);
    }

    public void dispose() {
        this.externalLexicon.clear();
        this.externalLexicon = null;
    }

    public Set<String> getExternalLexicon() {
        return this.externalLexicon;
    }
}
