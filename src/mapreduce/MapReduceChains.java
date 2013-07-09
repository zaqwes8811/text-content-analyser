package mapreduce;


import com.google.common.base.Optional;
import com.google.common.collect.*;
import common.Util;
import crosscuttings.AppConstants;
import crosscuttings.CrosscuttingsException;
import crosscuttings.jobs_processors.ImmutableJobsFormer;
import crosscuttings.jobs_processors.ProcessorTargets;
import com.google.common.base.Joiner;
import com.google.common.io.Closer;
import com.google.gson.Gson;
import parsers.ImmutableBECParser;

import java.io.BufferedWriter;
import java.io.CharArrayReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/** */
public class MapReduceChains {
  private MapReduceChains() {}

  public static void main(String [] args) {
    MapReduceChains.runBECChain();
  }

  public static void runSentencesLevelProcess () {
    /*
    // Получаем работы
    List<List<String>> jobs = ImmutableJobsFormer.getJobs();

    // Map Stage
    List<List> result_map_stage = new ArrayList<List>();
    for (List<String> job : jobs) {
      result_map_stage.add(Mappers.mapper_sentences_level(job));
      //break;  // DEVELOP
    }

    // Shuffle Stage - сейчас фактически нет - один узел - один файл
    List<List> result_shuffle_stage  = result_map_stage;

    // Reduce Stage  - так же нет, т.к. - один узел - один файл
    Map<String, Map<String, String>> result_reduce_stage =
        new HashMap<String, Map<String, String>>();
    for (List task: result_shuffle_stage) {
      Map<String, String> one = Reduces.reduce_sentences_level(task);

      String node_name = (String)task.get(Mappers.IDX_NODE_NAME);
      result_reduce_stage.put(node_name, one);
    }


    // Save result
    try {
      Closer closer = Closer.create();
      try {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String resultInJson = gson.toJson(result_reduce_stage);
        // Write
        String path_for_save = Joiner.on(AppConstants.PATH_SPLITTER)
          .join(
            ImmutableProcessorTargets.getPathToIndex(),
            AppConstants.STATIC_NOTES_FILENAME);

        BufferedWriter out = closer.register(new BufferedWriter(new FileWriter(path_for_save)));
        out.write(resultInJson);
        Util.print(resultInJson);

        Util.print("Notes write to file : "+path_for_save);

      } catch (Throwable e) {
        closer.rethrow(e);
      } finally {
        closer.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    Util.print("Done. Sentences level processing.");
    */
  }


  public void runWordLevelProcess () {
    Optional<List<List<String>>> jobs = Optional.absent();
    try {
      // Получаем работы
      jobs = Optional.of(ImmutableJobsFormer.getJobs());

    } catch (CrosscuttingsException e) {
      Util.print(e.getMessage());

    }
    // Map Stage
    List<List> resultMapStage = new ArrayList<List>();
    for (List<String> job : jobs.get()) {
      List one = Mappers.mapperWordLevel(job);
      resultMapStage.add(one);
      //break;  // DEVELOP
    }

    // Shuffle Stage - сейчас фактически нет - один узел - один файл
    List<List> resultShuffleStage  = resultMapStage;

    // Reduce Stage  - так же нет, т.к. - один узел - один файл
    List<List> resultReduceStage = new ArrayList<List>();
    for (List task: resultShuffleStage) {
      List one = Reduces.reduce_word_level_base(task);
      resultReduceStage.add(one);
    }

    // Save results
    for (final List nodeResult: resultReduceStage) {
      String pathToNode;
      try {
        pathToNode = Joiner.on(AppConstants.PATH_SPLITTER)
          .join(
            ProcessorTargets.getPathToIndex(),
            AppConstants.COMPRESSED_IDX_FOLDER,
            nodeResult.get(Mappers.IDX_NODE_NAME));
      } catch (CrosscuttingsException e) {
        Util.print(e.getMessage());
        pathToNode = "";
      }

      // TODO(zaqwes) TOTH: в защитной секции должно быть только то что нужно, или разное?
      // Может быть они тоже нормально сереализуются?
      // Вообще наверное лучше хранить в базе данных, а не в файлах.
      Multimap<String, Integer> idxSentences =
          (Multimap<String, Integer>)nodeResult.get(Mappers.IDX_SENT_MAP);
      Multiset<String> idxFrequencies =
          (Multiset<String>)nodeResult.get(Mappers.IDX_FREQ_INDEX);
      Multimap<String, String> idxRestWords =
          (Multimap<String, String>)nodeResult.get(Mappers.IDX_RESTS_MAP);
      List<String> idxSortedByFrequency =
        (ArrayList<String>)nodeResult.get(Reduces.IDX_SORTED_IDX);

      // Структуры для сереализации
      List<String> idxSortedByFrequencyForSave = idxSortedByFrequency;
      Map<String, Integer> idxFrequenciesForSave = new HashMap<String, Integer>();
      Map<String, String> idxRestWordsForSave = new HashMap<String, String>();
      Map<String, List<Integer>> idxSentencesForSave = new HashMap<String, List<Integer>>();

      // Перекомпановка. Возможно есть средства в Guava, но пусть будет
      //   дополнительная фильтрация по сортированному индексу.
      for (final String word: idxSortedByFrequencyForSave) {
        idxFrequenciesForSave.put(word, idxFrequencies.count(word));
      }
      for (final String word: idxSortedByFrequencyForSave) {
        idxRestWordsForSave.put(word, Joiner.on(" ").join(idxRestWords.get(word)));
      }
      Map<String, Collection<Integer>> idxSentencesTmp = idxSentences.asMap();
      for (final String word: idxSortedByFrequencyForSave) {
        idxSentencesForSave.put(word, new ArrayList<Integer>(idxSentencesTmp.get(word)));
      }

      try {
        // Само сохранение. Вряд ли удасться выделить в метод. И свернуть в цикл.
        Closer closer = Closer.create();
        try {
          // Сохраняем в JSON
          closer.register(new BufferedWriter(new FileWriter(
            Joiner.on(AppConstants.PATH_SPLITTER).join(pathToNode, AppConstants.SORTED_IDX_FILENAME))))
            .write(new Gson().toJson(idxSortedByFrequencyForSave));
          closer.register(new BufferedWriter(new FileWriter(
              Joiner.on(AppConstants.PATH_SPLITTER).join(pathToNode, AppConstants.FREQ_IDX_FILENAME))))
            .write(new Gson().toJson(idxFrequenciesForSave));
          closer.register(new BufferedWriter(new FileWriter(
              Joiner.on(AppConstants.PATH_SPLITTER).join(pathToNode, AppConstants.FILENAME_REST_IDX))))
            .write(new Gson().toJson(idxRestWordsForSave));
          closer.register(new BufferedWriter(new FileWriter(
              Joiner.on(AppConstants.PATH_SPLITTER).join(pathToNode, AppConstants.FILENAME_SENTENCES_IDX))))
            .write(new Gson().toJson(idxSentencesForSave));
        } catch (Throwable e) {
          closer.rethrow(e);
        } finally {
          closer.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }  // for..
  }

  // Chain for BEC dictionary.
  public static void runBECChain() {
    // Как-то нужно правильно сопоставить слово и контент.
    try {
      ImmutableList<String> content =
          Util.fileToList(
            Joiner.on(AppConstants.PATH_SPLITTER)
              .join("raw-dicts", "vocabularity-folded.txt"));
      ImmutableBECParser cash = ImmutableBECParser.create(content);

      // Извлекаем данные и обрабатываем их
      List<String> sortedByFreqIdxForSave = cash.getDict();
      Multiset<String> frequencyIdx = HashMultiset.create();
      Multimap<String, String> dictContent = cash.getContent();
      Multimap<String, String> dictTranslate = cash.getWordTranslates();

      Multimap<String, Integer> sentencesPtrs = HashMultimap.create();
      List<String> sentences = new ArrayList<String>();

      // Нуменация предложения с нуля!
      int idxSentence = 1;
      Set<String> keys = dictContent.keySet();
      for (final String key: keys) {
        Collection<String> value = dictContent.asMap().get(key);
        for (final String item: value) {
          sentences.add(item);
          sentencesPtrs.put(key, idxSentence);
          ++idxSentence;
        }
      }

      // Make frequency index. Пока плохо - словарь грязный.
      for (final String word: sortedByFreqIdxForSave) {
        frequencyIdx.add(word);
      }

      // Saver
     //Util.print(frequencyIdx);
      String pathToDefaultNode = "apps/default-node";
      //List tmp = new ArrayList();
      //tmp.add(frequencyIdx);
      //tmp.add(dictTranslate);   // NO WAY!
      //Util.print(new Gson().toJson(dictContent));



    } catch (IOException e) {
      Util.print(e.getMessage());
    } catch (IllegalStateException e) {
      Util.print(e.getMessage());
    }
  }
}
