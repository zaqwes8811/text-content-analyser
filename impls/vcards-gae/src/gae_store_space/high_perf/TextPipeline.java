package gae_store_space.high_perf;

import gae_store_space.PageKind;
import gae_store_space.SentenceKind;
import gae_store_space.WordKind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import core.mapreduce.CountReducer;
import core.mapreduce.CountReducerImpl;
import core.mapreduce.CounterMapper;
import core.mapreduce.CounterMapperImpl;
import core.nlp.PlainTextTokenizer;
import core.text_extractors.Convertor;
import core.text_extractors.SubtitlesToPlainText;

public class TextPipeline {
	public static final String defaultPageName = "Korra";
	public static final String defaultGenName = "Default";
	public static final String defaultUserId = "User";
	
	private Convertor convertor = new SubtitlesToPlainText();
	//private CountReducer reducer = new CountReducer(wordHistogramSink);
  //private CounterMapper mapper = new CounterMapper(reducer);
	private PlainTextTokenizer tokenizer = new PlainTextTokenizer();
	
  private String removeFormatting(String rawText) {
  	return convertor.convert(rawText);  	
  }

  private ArrayList<SentenceKind> packSentences(ImmutableList<String> sentences) {
    ArrayList<SentenceKind> r = new ArrayList<SentenceKind>();
    for (String sentence: sentences)
      r.add(new SentenceKind(sentence));
    return r;
  }
  
  private ArrayList<WordKind> sort(ArrayList<WordKind> value) {
  	Collections.sort(value, WordKind.createFrequencyComparator());
    Collections.reverse(value);
    return value;
  }
  
  private Multimap<String, SentenceKind> buildHisto(ArrayList<SentenceKind> contentElements) {
  	Multimap<String, SentenceKind> wordHistogramSink = HashMultimap.create();
    
    CountReducer reducer = new CountReducerImpl(wordHistogramSink);
    CounterMapper mapper = new CounterMapperImpl(reducer);

    // Split
    mapper.map(contentElements);
    
    return wordHistogramSink;
  }
  
  private ArrayList<WordKind> unpackHisto(Multimap<String, SentenceKind> wordHistogramSink) {
  	ArrayList<WordKind> value = new ArrayList<WordKind>();
    for (String word: wordHistogramSink.keySet()) {
      Collection<SentenceKind> content = wordHistogramSink.get(word);
      int rawFrequency = content.size();
      WordKind k = WordKind.create(word, content, rawFrequency);
      value.add(k);
    }
    return value;
  }
  
  private ArrayList<WordKind> assignIndexes(ArrayList<WordKind> words) {
    for (int i = 0; i < words.size(); i++) {
    	words.get(i).setPointPos(i);
    }
    return words;
  }
  
  // Now no store operations
  public PageKind pass(String name, String text) {
  	String pureText = removeFormatting(text);
  	
  	ImmutableList<String> sentences = tokenizer.getSentences(pureText);
  	
  	ArrayList<SentenceKind> contentElements = packSentences(sentences);
  	
    // Assemble statistic
    Multimap<String, SentenceKind> histo = buildHisto(contentElements);

    ArrayList<WordKind> words = unpackHisto(histo);

    // Sort words by frequency
    words = sort(words);

    // Элементы отсортированы и это важно
    words = assignIndexes(words);

    // Слова сортированы
    return new PageKind(name, contentElements, words, text);
  }
}
