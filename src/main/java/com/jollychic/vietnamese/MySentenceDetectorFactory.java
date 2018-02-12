package com.jollychic.vietnamese;

import vn.hus.nlp.sd.SentenceDetector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author mars
 * @create 2018-02-11 下午2:45
 **/
public class MySentenceDetectorFactory {
    public MySentenceDetectorFactory() {
    }

    public static SentenceDetector create(String var0) {
        String var1 = "";
        if(var0.equalsIgnoreCase("french")) {
            var1 = "models/sentDetection/FrenchSD.bin.gz";
        }

        if(var0.equalsIgnoreCase("vietnamese")) {
            var1 = "models/sentDetection/VietnameseSD.bin.gz";
        }

        try {
            return new SentenceDetector(VietnameseAnalyzer.classPath+var1);
        } catch (IOException var3) {
            System.err.println("Error when constructing the sentence detector.");
            var3.printStackTrace();
            return null;
        }
    }

    public static SentenceDetector create(Properties var0) {
        try {
            return new SentenceDetector(var0);
        } catch (IOException var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public static SentenceDetector create(InputStream var0) {
        try {
            Properties var1 = new Properties();
            var1.load(var0);
            return new SentenceDetector(var1);
        } catch (IOException var2) {
            var2.printStackTrace();
            return null;
        }
    }
}
