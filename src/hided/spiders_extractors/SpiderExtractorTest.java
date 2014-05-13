package hided.spiders_extractors;//package com.github.zaqwes8811.text_processor.sandbox;


import business.adapters_3rdparty.TikaWrapper;
import common.InnerReuse;
import hided.jobs_processors.ProcessorTargets;
import hided.crosscuttings.AppConstants;
import hided.crosscuttings.ThroughLevelBoundaryError;
import org.junit.Test;

import java.util.List;

public class SpiderExtractorTest {
  static void print(Object msg) {
     System.out.println(msg);

  }

  @Test
  public void testDevelopSpider() {
      try {
        // Получаем цели
        String spiderTargetsFilename = AppConstants.SPIDER_TARGETS_FILENAME;
        List<List<String>> targets = ProcessorTargets.runParser(spiderTargetsFilename);
        for (List<String> target : targets) {
          try {
            // TODO(zaqwes): если файл существует, то будет перезаписан. Нужно хотя бы предупр.
            TikaWrapper.extractAndSaveText(target);
            TikaWrapper.extractAndSaveMetadata(target);
            //break;  // DEVELOP
          } catch (ExtractorException e) {
            // Ошибка может произойти на каждой итерации, но пусть обработка предолжается
            InnerReuse.print(e.getMessage());
          }
        }

      } catch (ThroughLevelBoundaryError e) {
        System.out.println(e.getMessage());
      }
    }
}
