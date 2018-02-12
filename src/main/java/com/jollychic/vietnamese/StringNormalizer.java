package com.jollychic.vietnamese;

/**
 * @author mars
 * @create 2018-02-11 下午3:39
 **/
import vn.hus.nlp.utils.UTF8FileUtility;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class StringNormalizer {
    private static Map<String, String> map;

    private StringNormalizer(String var1) {
        map = new HashMap();
        this.init(var1);
    }

    private void init(String var1) {
        String[] var2 = UTF8FileUtility.getLines(var1);

        for(int var3 = 0; var3 < var2.length; ++var3) {
            String[] var4 = var2[var3].split("\\s+");
            if(var4.length == 2) {
                map.put(var4[0], var4[1]);
            } else {
                System.err.println("Wrong syntax in the map file " + var1 + " at line " + var3);
            }
        }

    }

    public static StringNormalizer getInstance() {
        return new StringNormalizer(VietnameseAnalyzer.classPath+"resources/normalization/rules.txt");
    }

    public String normalize(String var1) {
        String var2 = new String(var1);
        Iterator var3 = map.keySet().iterator();

        while(var3.hasNext()) {
            String var4 = (String)var3.next();
            String var5 = (String)map.get(var4);
            if(var2.indexOf(var4) >= 0) {
                var2 = var2.replace(var4, var5);
            }
        }

        return var2;
    }
}
