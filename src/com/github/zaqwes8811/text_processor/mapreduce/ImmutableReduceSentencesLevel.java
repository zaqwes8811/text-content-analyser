package com.github.zaqwes8811.text_processor.mapreduce;

import com.github.zaqwes8811.text_processor.common.ImmutableAppUtils;
import com.github.zaqwes8811.text_processor.math.ImmutableSummators;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: кей
 * Date: 11.05.13
 * Time: 20:38
 * To change this template use File | Settings | File Templates.
 */
final public class ImmutableReduceSentencesLevel {
  public static final int IDX_NODE_NAME = 0;
  public static final int IDX_SENT_LENGTH_MEAN = 1;
  public static final int IDX_RE = 2;
  public static final int IDX_LANG = 3;

  public static final double RU_MEAN_SPEED_READ = 250.0;  // word/min

  public static Map<String, String> reduce_sentences_level(List result_shuffle_stage) {
    // Средняя длина предложения
    List<Integer> s = (List<Integer>)result_shuffle_stage.get(
        ImmutableMapperSentencesLevel.IDX_SENTENCES_LENS);
    Double meanLengthSentence = ImmutableSummators.meanList(s);
    Double countWords = ImmutableSummators.sumIntList(s)*1.0;

    // Средняя длина слога
    s = (List<Integer>)result_shuffle_stage.get(ImmutableMapperSentencesLevel.IDX_COUNT_SYLLABLES);
    Double meanLengthSyllable = ImmutableSummators.sumIntList(s)/countWords;

    Double RE = new Double(-1);
    Double timeForRead = new Double(-1);
    String lang = (String)result_shuffle_stage.get(ImmutableMapperSentencesLevel.IDX_LANG);
    if (lang.equals("ru")) {
      RE = (206.835 - 60.1*meanLengthSyllable - 1.3*meanLengthSentence);

      timeForRead = countWords/RU_MEAN_SPEED_READ/60;  // часов
    } else if (lang.equals("en")) {
      RE = (206.835 - 84.6*meanLengthSyllable - 1.015*meanLengthSentence);
      timeForRead = countWords/RU_MEAN_SPEED_READ/60;  // часов
    } else {
      ImmutableAppUtils.print("Lang no used");
    }

    // Make results
    Map<String, String> result_reduce_stage = new TreeMap<String, String>();
    result_reduce_stage.put("mean_length_sentence", meanLengthSentence.toString());
    result_reduce_stage.put("RE", RE.toString());
    result_reduce_stage.put("mean_time_for_read", timeForRead.toString());
    result_reduce_stage.put("mean_language", lang);
    return result_reduce_stage;
  }
}
