package com.jollychic.vietnamese;

/**
 * @author mars
 * @create 2018-02-12 上午11:41
 **/
import vn.hus.nlp.lexicon.jaxb.Corpus;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class LexiconUnmarshaller {
    JAXBContext jaxbContext;
    Unmarshaller unmarshaller;

    public LexiconUnmarshaller() {
        this.createContext();
    }

    private void createContext() {
        this.jaxbContext = null;

        try {
            this.jaxbContext = JAXBContext.newInstance("vn.hus.nlp.lexicon.jaxb");
        } catch (JAXBException var2) {
            var2.printStackTrace();
        }

    }

    protected Unmarshaller getUnmarshaller() {
        if(this.unmarshaller == null) {
            try {
                this.unmarshaller = this.jaxbContext.createUnmarshaller();
            } catch (JAXBException var2) {
                var2.printStackTrace();
            }
        }

        return this.unmarshaller;
    }

    public Corpus unmarshal(String var1) {
        try {
            Object var2 = this.getUnmarshaller().unmarshal(new FileInputStream(var1));
            if(var2 instanceof Corpus) {
                Corpus var3 = (Corpus)var2;
                return var3;
            }
        } catch (FileNotFoundException var4) {
            var4.printStackTrace();
        } catch (JAXBException var5) {
            var5.printStackTrace();
        }

        return null;
    }
}
