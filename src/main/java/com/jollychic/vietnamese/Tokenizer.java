package com.jollychic.vietnamese;

/**
 * @author mars
 * @create 2018-02-11 下午3:19
 **/
import vn.hus.nlp.lexicon.jaxb.Corpus;
import vn.hus.nlp.lexicon.jaxb.W;
import vn.hus.nlp.tokenizer.ITokenizerListener;
import vn.hus.nlp.tokenizer.ResultMerger;
import vn.hus.nlp.tokenizer.io.Outputer;
import vn.hus.nlp.tokenizer.tokens.LexerRule;
import vn.hus.nlp.tokenizer.tokens.TaggedWord;
import vn.hus.nlp.tokenizer.tokens.WordToken;
import vn.hus.nlp.utils.UTF8FileUtility;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
    private LexerRule[] rules = new LexerRule[0];
    private InputStream inputStream;
    private LineNumberReader lineReader;
    private String line;
    private int column;
    private List<TaggedWord> result = null;
    private final Segmenter segmenter;
    private Outputer outputer = null;
    private final List<ITokenizerListener> tokenizerListener = new ArrayList();
    private boolean isAmbiguitiesResolved = true;
    private Logger logger;
    private final ResultMerger resultMerger;
    private final ResultSplitter resultSplitter;

    public Tokenizer(String var1, Segmenter var2) {
        this.loadLexerRules(var1);
        this.segmenter = var2;
        this.result = new ArrayList();
        this.createOutputer();
        this.resultMerger = new ResultMerger();
        this.resultSplitter = new ResultSplitter();
        this.createLogger();
        this.addTokenizerListener(new Tokenizer.SimpleProgressReporter());
    }

    private void createOutputer() {
        if(this.outputer == null) {
            this.outputer = new Outputer();
        }

    }

    public Outputer getOutputer() {
        return this.outputer;
    }

    public void setOutputer(Outputer var1) {
        this.outputer = var1;
    }

    private void createLogger() {
        if(this.logger == null) {
            this.logger = Logger.getLogger(Segmenter.class.getName());

            try {
                this.logger.addHandler(new FileHandler("tokenizer.log"));
            } catch (SecurityException var2) {
                var2.printStackTrace();
            } catch (IOException var3) {
                var3.printStackTrace();
            }

            this.logger.setLevel(Level.FINEST);
        }

    }

    private void loadLexerRules(String var1) {
        LexiconUnmarshaller var2 = new LexiconUnmarshaller();
        Corpus var3 = var2.unmarshal(var1);
        ArrayList var4 = new ArrayList();
        List var5 = var3.getBody().getW();
        Iterator var6 = var5.iterator();

        while(var6.hasNext()) {
            W var7 = (W)var6.next();
            LexerRule var8 = new LexerRule(var7.getMsd(), var7.getContent());
            var4.add(var8);
        }

        this.rules = (LexerRule[])var4.toArray(this.rules);
    }

    public synchronized void tokenize(Reader var1) throws IOException {
        this.result.clear();
        this.lineReader = new LineNumberReader(var1);
        this.line = null;
        this.column = 1;

        while(true) {
            TaggedWord var2 = this.getNextToken2();
            if(var2 == null) {
                this.result = this.resultMerger.mergeList(this.result);
                return;
            }

            int var11;
            if(!var2.isPhrase()) {
                if(var2.isNamedEntity()) {
                    TaggedWord[] var8 = this.resultSplitter.split(var2);
                    if(var8 != null) {
                        TaggedWord[] var9 = var8;
                        int var10 = var8.length;

                        for(var11 = 0; var11 < var10; ++var11) {
                            TaggedWord var12 = var9[var11];
                            this.result.add(var12);
                        }
                    } else {
                        this.result.add(var2);
                    }
                } else if(var2.getText().trim().length() > 0) {
                    this.result.add(var2);
                }
            } else {
                String var3 = var2.getText().trim();
                if(!this.isSimplePhrase(var3)) {
                    String[] var4 = null;
                    List var5 = this.segmenter.segment(var3);
                    if(var5.size() == 0) {
                        this.logger.log(Level.WARNING, "The segmenter cannot segment the phrase \"" + var3 + "\"");
                    }

                    if(this.isAmbiguitiesResolved() && var5.size() > 1) {
                        var4 = this.segmenter.resolveAmbiguity(var5);
                    } else {
                        Iterator var6 = var5.iterator();
                        if(var6.hasNext()) {
                            var4 = (String[])var6.next();
                        }
                    }

                    if(var4 == null) {
                        this.logger.log(Level.WARNING, "Problem: " + var3);
                    }

                    for(var11 = 0; var11 < var4.length; ++var11) {
                        WordToken var7 = new WordToken(new LexerRule("word"), var4[var11], this.lineReader.getLineNumber(), this.column);
                        this.result.add(var7);
                        this.column += var4[var11].length();
                    }
                } else if(var3.length() > 0) {
                    this.result.add(var2);
                }
            }

            this.fireProcess(var2);
        }
    }

    public void tokenize(String var1) {
        UTF8FileUtility.createReader(var1);

        try {
            this.tokenize((Reader)UTF8FileUtility.reader);
        } catch (IOException var3) {
            var3.printStackTrace();
        }

        UTF8FileUtility.closeReader();
    }

    private boolean isSimplePhrase(String var1) {
        var1 = var1.trim();
        return var1.indexOf(32) < 0;
    }

    private TaggedWord getNextToken() throws IOException {
        if(this.line == null || this.line.length() == 0) {
            this.line = this.lineReader.readLine();
            if(this.line == null) {
                if(this.inputStream != null) {
                    this.inputStream.close();
                }

                this.lineReader = null;
                return null;
            }

            if(this.line.length() == 0) {
                return new TaggedWord("\n");
            }

            this.column = 1;
        }

        TaggedWord var1 = null;
        int var2 = -1;
        int var3 = -1;
        String var4 = "";

        for(int var5 = 0; var5 < this.rules.length; ++var5) {
            LexerRule var6 = this.rules[var5];
            Pattern var7 = var6.getPattern();
            Matcher var8 = var7.matcher(this.line);
            if(var8.lookingAt()) {
                int var9 = var8.end();
                if(var9 > var3) {
                    var3 = var9;
                    var4 = var8.group(0);
                    System.err.println(var6.getName() + ": " + var4);
                    int var10 = this.lineReader.getLineNumber();
                    var1 = new TaggedWord(var6, var4, var10, this.column);
                    var2 = var9;
                }
            }
        }

        if(var1 == null) {
            this.logger.log(Level.WARNING, "Error! line = " + this.lineReader.getLineNumber() + ", col = " + this.column);
            System.out.println(this.line);
            System.exit(1);
            return null;
        } else {
            this.column += var2;
            this.line = this.line.substring(var2);
            return var1;
        }
    }

    private TaggedWord getNextToken2() throws IOException {
        if(this.line == null || this.line.length() == 0) {
            this.line = this.lineReader.readLine();
            if(this.line == null) {
                if(this.inputStream != null) {
                    this.inputStream.close();
                }

                this.lineReader = null;
                return null;
            }

            if(this.line.length() == 0) {
                return new TaggedWord("\n");
            }

            this.column = 1;
        }

        TaggedWord var1 = null;
        int var2 = -1;
        int var3 = -1;
        int var4 = this.lineReader.getLineNumber();
        LexerRule var5 = null;

        int var6;
        for(var6 = 0; var6 < this.rules.length; ++var6) {
            LexerRule var7 = this.rules[var6];
            Pattern var8 = var7.getPattern();
            Matcher var9 = var8.matcher(this.line);
            if(var9.lookingAt()) {
                int var10 = var9.end();
                if(var10 > var3) {
                    var3 = var10;
                    var2 = var10;
                    var5 = var7;
                }
            }
        }

        var6 = var2;
        if(var2 < this.line.length() && this.line.charAt(var2) == 64) {
            while(this.line.charAt(var6) != 32) {
                --var6;
            }
        }

        String var11 = this.line.substring(0, var6);
        var1 = new TaggedWord(var5, var11, var4, this.column);
        this.column += var6;
        this.line = this.line.substring(var6).trim();
        return var1;
    }

    public void exportResult(String var1, Outputer var2) {
        System.out.print("Exporting result of tokenization...");

        try {
            FileOutputStream var3 = new FileOutputStream(var1);
            OutputStreamWriter var4 = new OutputStreamWriter(var3, "UTF-8");
            BufferedWriter var5 = new BufferedWriter(var4);
            var5.write(var2.output(this.result));
            var5.flush();
            var5.close();
        } catch (IOException var6) {
            var6.printStackTrace();
        }

        System.out.println("OK.");
    }

    public void exportResult(String var1) {
        System.out.print("Exporting result of tokenization...");
        UTF8FileUtility.createWriter(var1);
        Iterator var2 = this.result.iterator();

        while(var2.hasNext()) {
            TaggedWord var3 = (TaggedWord)var2.next();
            UTF8FileUtility.write(var3.toString() + "\n");
        }

        UTF8FileUtility.closeWriter();
        System.out.println("OK");
    }

    public List<TaggedWord> getResult() {
        return this.result;
    }

    public void setResult(List<TaggedWord> var1) {
        this.result = var1;
    }

    public void addTokenizerListener(ITokenizerListener var1) {
        this.tokenizerListener.add(var1);
    }

    public void removeTokenizerListener(ITokenizerListener var1) {
        this.tokenizerListener.remove(var1);
    }

    public List<ITokenizerListener> getTokenizerListener() {
        return this.tokenizerListener;
    }

    private void fireProcess(TaggedWord var1) {
        Iterator var2 = this.tokenizerListener.iterator();

        while(var2.hasNext()) {
            ITokenizerListener var3 = (ITokenizerListener)var2.next();
            var3.processToken(var1);
        }

    }

    public void dispose() {
        this.segmenter.dispose();
        this.result.clear();
        this.tokenizerListener.clear();
    }

    public boolean isAmbiguitiesResolved() {
        return this.isAmbiguitiesResolved;
    }

    public void setAmbiguitiesResolved(boolean var1) {
        this.isAmbiguitiesResolved = var1;
    }

    public Segmenter getSegmenter() {
        return this.segmenter;
    }

    private class SimpleProgressReporter implements ITokenizerListener {
        private SimpleProgressReporter() {
        }

        public void processToken(TaggedWord var1) {
            if(Tokenizer.this.result.size() % 1000 == 0) {
                System.out.print(".");
            }

        }
    }
}
