package com.jollychic.vietnamese;

import vn.hus.nlp.graph.*;
import vn.hus.nlp.graph.io.GraphIO;
import vn.hus.nlp.graph.search.ShortestPathFinder;
import vn.hus.nlp.graph.util.GraphConnectivity;
import vn.hus.nlp.tokenizer.segmenter.AbstractLexiconRecognizer;
import vn.hus.nlp.tokenizer.segmenter.AbstractResolver;
import vn.hus.nlp.tokenizer.segmenter.DFALexiconRecognizer;
import vn.hus.nlp.utils.CaseConverter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mars
 * @create 2018-02-11 下午3:16
 **/
public class Segmenter {

    private static StringNormalizer normalizer;
    private Logger logger;
    private static AbstractLexiconRecognizer lexiconRecognizer;
    private static AbstractLexiconRecognizer externalLexiconRecognizer;
    private final List<String[]> result;
    private AbstractResolver resolver;
    private static double MAX_EDGE_WEIGHT = 100.0D;
    private static boolean DEBUG = false;

    public Segmenter() {
        this.resolver = null;
        this.result = new ArrayList();
        this.createLogger();
        this.getDFALexiconRecognizer();
        this.getExternalLexiconRecognizer();
        normalizer = StringNormalizer.getInstance();
    }

    public Segmenter(AbstractResolver var1) {
        this();
        this.resolver = var1;
    }

    private void createLogger() {
        if(this.logger == null) {
            this.logger = Logger.getLogger(Segmenter.class.getName());
            this.logger.addHandler(new ConsoleHandler());
            this.logger.setLevel(Level.FINEST);
        }

    }

    public List<String[]> getResult() {
        return this.result;
    }

    private static String normalize(String var0) {
        StringBuffer var1 = new StringBuffer(var0);
        char var2 = var1.charAt(0);
        char var3 = var2;
        if(65 <= var2 && var2 <= 90) {
            var3 = Character.toLowerCase(var2);
        } else if(CaseConverter.isValidUpper(var2)) {
            var3 = CaseConverter.toLower(var2);
        }

        var1.setCharAt(0, var3);
        return normalizer.normalize(var1.toString());
    }

    private IWeightedGraph makeGraph(String[] var1) {
        int var2 = var1.length + 1;
        AdjacencyListWeightedGraph var3 = new AdjacencyListWeightedGraph(var2, true);

        for(int var4 = 0; var4 < var2 - 1; ++var4) {
            String var5 = "";

            for(int var6 = 0; var6 < var2 - 1 - var4; ++var6) {
                if(var5.length() == 0) {
                    var5 = var1[var4];
                } else {
                    var5 = var5 + ' ' + var1[var4 + var6];
                }

                if(this.getDFALexiconRecognizer().accept(var5) || this.getExternalLexiconRecognizer().accept(var5)) {
                    double var7 = 1.0D / (double)(var6 + 1);
                    var7 = Math.floor(var7 * 100.0D);
                    var3.insert(new Edge(var4, var4 + var6 + 1, var7));
                }
            }
        }

        return var3;
    }

    private AbstractLexiconRecognizer getDFALexiconRecognizer() {
        if(lexiconRecognizer == null) {
            lexiconRecognizer = DFALexiconRecognizer.getInstance(VietnameseAnalyzer.classPath+"resources/automata/dfaLexicon.xml");
        }

        return lexiconRecognizer;
    }

    private AbstractLexiconRecognizer getExternalLexiconRecognizer() {
        if(externalLexiconRecognizer == null) {
            externalLexiconRecognizer = new ExternalLexiconRecognizer();
        }

        return externalLexiconRecognizer;
    }

    private void connect(IGraph var1) {
        if(GraphConnectivity.countComponents(var1) != 1) {
            int[] var2 = GraphConnectivity.getIsolatedVertices(var1);
            int var4;
            int var5;
            if(DEBUG) {
                System.err.println("The graph for the phrase is: ");
                GraphIO.print(var1);
                System.out.println("Isolated vertices: ");
                int[] var3 = var2;
                var4 = var2.length;

                for(var5 = 0; var5 < var4; ++var5) {
                    int var6 = var3[var5];
                    System.out.println(var6);
                }
            }

            boolean var7 = false;

            for(var4 = 0; var4 < var2.length; ++var4) {
                var5 = var2[var4];
                if(var5 == 0) {
                    var7 = true;
                    var1.insert(new Edge(0, 1, MAX_EDGE_WEIGHT));
                } else if(var5 != 1) {
                    var1.insert(new Edge(var5 - 1, var5, MAX_EDGE_WEIGHT));
                } else if(!var7) {
                    var1.insert(new Edge(var5 - 1, var5, MAX_EDGE_WEIGHT));
                }
            }

            if(GraphConnectivity.countComponents(var1) != 1) {
                this.logger.log(Level.INFO, "Hmm, fail to connect the graph!");
            }

        }
    }

    private String[] prepare(String var1) {
        this.result.clear();
        var1 = normalize(var1);
        String[] var2 = var1.split("\\s+");
        return var2;
    }

    private String[] buildSegmentation(String[] var1, int[] var2) {
        String[] var3 = new String[var2.length - 1];
        int var4 = 0;
        int var5 = 0;

        for(int var6 = 1; var6 < var2.length; ++var6) {
            int var7 = var2[var6];
            String var8 = "";

            for(int var9 = var4; var9 < var7; ++var9) {
                var8 = var8 + var1[var9] + ' ';
            }

            var8 = var8.trim();
            var3[var5++] = var8;
            var4 = var7;
        }

        return var3;
    }

    public List<String[]> segment(String var1) {
        String[] var2 = var1.split("\\s+");
        String[] var3 = this.prepare(var1);
        IWeightedGraph var4 = this.makeGraph(var3);
        int var5 = var4.getNumberOfVertices();
        if(!GraphConnectivity.isConnected(var4, 0, var5 - 1)) {
            this.connect(var4);
        }

        ShortestPathFinder var6 = new ShortestPathFinder(var4);
        Node[] var7 = var6.getAllShortestPaths(var5 - 1);

        for(int var8 = 0; var8 < var7.length; ++var8) {
            Node var9 = var7[var8];
            int[] var10 = var9.toArray();
            String[] var11 = this.buildSegmentation(var2, var10);
            this.result.add(var11);
        }

        return this.result;
    }

    public String[] resolveAmbiguity(List<String[]> var1) {
        return this.resolver.resolve(var1);
    }

    public void printResult() {
        Iterator var1 = this.result.iterator();

        while(var1.hasNext()) {
            String[] var2 = (String[])var1.next();

            for(int var3 = 0; var3 < var2.length; ++var3) {
                System.out.print("[" + var2[var3] + "] ");
            }

            System.out.println();
        }

    }

    public void dispose() {
        this.result.clear();
        lexiconRecognizer.dispose();
        externalLexiconRecognizer.dispose();
    }
}
