package com.jollychic.vietnamese;

/**
 * @author mars
 * @create 2018-02-12 上午11:36
 **/
import vn.hus.nlp.lexicon.LexiconUnmarshaller;
import vn.hus.nlp.lexicon.jaxb.Corpus;
import vn.hus.nlp.lexicon.jaxb.W;
import vn.hus.nlp.tokenizer.segmenter.AbstractResolver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UnigramResolver extends AbstractResolver {
    private LexiconUnmarshaller unmarshaller;
    private Map<String, Integer> unigram;

    public UnigramResolver(String var1) {
        this.init();
        this.loadUnigram(var1);
    }

    private void init() {
        this.unmarshaller = new LexiconUnmarshaller();
        this.unigram = new HashMap();
    }

    private void loadUnigram(String var1) {
        System.out.print("Loading unigram model...");
        Corpus var2 = this.unmarshaller.unmarshal(var1);
        List var3 = var2.getBody().getW();
        Iterator var4 = var3.iterator();

        while(var4.hasNext()) {
            W var5 = (W)var4.next();
            String var6 = var5.getMsd();
            String var7 = var5.getContent();
            this.unigram.put(var7, Integer.valueOf(Integer.parseInt(var6)));
        }

        System.out.println("OK");
    }

    public String[] resolve(List<String[]> var1) {
        String[] var2 = null;
        int var3 = 0;
        Iterator var4 = var1.iterator();

        while(var4.hasNext()) {
            String[] var5 = (String[])var4.next();
            int var6 = 0;

            for(int var7 = 0; var7 < var5.length; ++var7) {
                String var8 = var5[var7];
                int var9 = 0;
                if(this.unigram.containsKey(var8)) {
                    var9 = ((Integer)this.unigram.get(var8)).intValue();
                }

                var6 += var9;
            }

            if(var6 >= var3) {
                var3 = var6;
                var2 = var5;
            }
        }

        return var2;
    }
}