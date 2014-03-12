package nlp;

import com.github.zaqwes8811.text_processor.common.ImmutableAppUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.Test;
import org.tartarus.snowball.ext.russianStemmer;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: кей
 * Date: 11.05.13
 * Time: 15:11
 * To change this template use File | Settings | File Templates.
 */
public class CompressorsTest {
  @Test
  public void testDevelop() {
  List<String> ignoredWordsRu = new ArrayList<String>(Arrays.asList(
                              "никуда",
                                "кое",
                                "применяться",
                                "чуть",
                                "кой",
                                "немного",
                                "через",
                                "рассматриваться",
                                "ко",
                                "создать",
                                "предложенный",
                                "надо",
                                "дать",
                                "этак",
                                "реализоваться",
                                "прежде",
                                "оттого",
                                "позднее",
                                "где",
                                "несмотря",
                                "близ",
                                "вряд",
                                "преимущество",
                                "проводиться",
                                "вот",
                                "напр",
                                "хоть",
                                "особенный",
                                "под",
                                "не",
                                "описывать",
                                "выполненье",
                                "либо",
                                "на",
                                "достигать",
                                "буду",
                                "ни",
                                "будто",
                                "чему",
                                "подле",
                                "ото",
                                "однако",
                                "чей",
                                "небольшой",
                                "сопоставительный",
                                "чем",
                                "никогда",
                                "достаточный",
                                "определять",
                                "различный",
                                "указывать",
                                "частности",
                                "предназначиться",
                                "отличный",
                                "полно",
                                "затем",
                                "позволять",
                                "вокруг",
                                "ими",
                                "последовать",
                                "должный",
                                "подтверждать",
                                "едва",
                                "пример",
                                "фиг",
                                "случай",
                                "получать",
                                "частый",
                                "над",
                                "разве",
                                "указать",
                                "обеспечивать",
                                "представлять",
                                "нужно",
                                "определенный",
                                "описание",
                                "вкратце",
                                "обеспечить",
                                "несколько",
                                "указываться",
                                "среди",
                                "у",
                                "хотеть",
                                "применять",
                                "предполагать",
                                "характеризовать",
                                "содержаться",
                                "называемый",
                                "цель",
                                "охарактеризовать",
                                "так",
                                "там",
                                "наш",
                                "мало",
                                "некоторый",
                                "она",
                                "оно",
                                "отчего",
                                "том",
                                "того",
                                "использовать",
                                "они",
                                "изобретение",
                                "выполнить",
                                "мимо",
                                "работоспособность",
                                "знать",
                                "что",
                                "предполагаемый",
                                "начать",
                                "столько",
                                "сколько",
                                "сущность",
                                "невозможно",
                                "достигаться",
                                "осуществить",
                                "нет",
                                "почесть",
                                "никакой",
                                "тут",
                                "опять",
                                "поперек",
                                "же",
                                "создавать",
                                "нибудь",
                                "патент",
                                "совокупность",
                                "без",
                                "а",
                                "предлагаемый",
                                "себя",
                                "отличие",
                                "относиться",
                                "можно",
                                "ж",
                                "служить",
                                "ведь",
                                "о",
                                "иметь",
                                "благодаря",
                                "проведение",
                                "проводить",
                                "ничто",
                                "хотя",
                                "требовать",
                                "подтвердить",
                                "и",
                                "описанье",
                                "раз",
                                "нельзя",
                                "просто",
                                "дабы",
                                "отличаться",
                                "показывать",
                                "предложить",
                                "создаваться",
                                "осуществлять",
                                "поз",
                                "лишь",
                                "возле",
                                "решенье",
                                "примениться",
                                "присущий",
                                "следующий",
                                "наибольший",
                                "нем",
                                "ней",
                                "вблизи",
                                "идти",
                                "врозь",
                                "существо",
                                "явиться",
                                "следующее",
                                "сперва",
                                "существовать",
                                "также",
                                "который",
                                "конкретный",
                                "иначе",
                                "предо",
                                "ради",
                                "расположенный",
                                "особенно",
                                "разом",
                                "силу",
                                "использованье",
                                "сколь",
                                "необходимо",
                                "снабдить",
                                "поясняться",
                                "вновь",
                                "частный",
                                "ему",
                                "столь",
                                "гораздо",
                                "такой",
                                "свое",
                                "откуда",
                                "свой",
                                "поначалу",
                                "пр",
                                "с",
                                "по",
                                "но",
                                "создание",
                                "сравнение",
                                "наподобие",
                                "ничего",
                                "упрощенье",
                                "определить",
                                "оба",
                                "эти",
                                "обе",
                                "приведенный",
                                "тогда",
                                "решение",
                                "обо",
                                "ничей",
                                "него",
                                "это",
                                "вдруг",
                                "сам",
                                "понемногу",
                                "преимущественный",
                                "счет",
                                "зачем",
                                "предлагаться",
                                "безо",
                                "содержать",
                                "нигде",
                                "установленный",
                                "когда",
                                "мочь",
                                "поскольку",
                                "снабжать",
                                "зато",
                                "предположить",
                                "ею",
                                "сверх",
                                "сделать",
                                "ввиду",
                                "вследствие",
                                "помимо",
                                "обычный",
                                "тем",
                                "посредством",
                                "во",
                                "предназначаться",
                                "против",
                                "совсем",
                                "новейший",
                                "начинаться",
                                "чертеж",
                                "очень",
                                "дан",
                                "устанавливаемый",
                                "или",
                                "почти",
                                "разный",
                                "работать",
                                "про",
                                "при",
                                "заявка",
                                "заявляемый",
                                "вопреки",
                                "обуславливать",
                                "достаточно",
                                "намного",
                                "предназначить",
                                "никак",
                                "итого",
                                "свои",
                                "ей",
                                "потому",
                                "какой",
                                "ее",
                                "известный",
                                "рассматривать",
                                "весь",
                                "характеризоваться",
                                "известно",
                                "заключаться",
                                "вкупе",
                                "его",
                                "то",
                                "показать",
                                "есть",
                                "выразить",
                                "немалый",
                                "впоследствии",
                                "тоже",
                                "вдоль",
                                "ним",
                                "им",
                                "некто",
                                "этакий",
                                "аналог",
                                "обусловить",
                                "немало",
                                "вслед",
                                "из",
                                "изображенный",
                                "рассмотреть",
                                "потом",
                                "почему",
                                "представить",
                                "каждый",
                                "относительно",
                                "простой",
                                "нисколько",
                                "та",
                                "выполнять",
                                "иной",
                                "некого",
                                "всякий",
                                "позволить",
                                "изготовление",
                                "ли",
                                "чтобы",
                                "самый",
                                "собой",
                                "получить",
                                "именно",
                                "кто",
                                "например",
                                "постольку",
                                "выполнение",
                                "далекий",
                                "близкий",
                                "далее",
                                "наряду",
                                "них",
                                "куда",
                                "некогда",
                                "их",
                                "охарактеризовывать",
                                "все",
                                "недостаток",
                                "тот",
                                "для",
                                "отнюдь",
                                "из-за",
                                "использование",
                                "он",
                                "об",
                                "названный",
                                "формула",
                                "позже",
                                "взамен",
                                "немногое",
                                "бы",
                                "существенный",
                                "новизна",
                                "смочь",
                                "пояснять",
                                "ранее",
                                "даже",
                                "данные",
                                "эта",
                                "применимость",
                                "из-под",
                                "никто",
                                "вроде",
                                "в",
                                "накануне",
                                "к",
                                "необходимость",
                                "обозначенный",
                                "необходимое",
                                "осуществление",
                                "поэтому",
                                "вероятно",
                                "становиться",
                                "наличие",
                                "снова",
                                "ничуть",
                                "после",
                                "результат",
                                "простота",
                                "ль",
                                "заявленный",
                                "впредь",
                                "согласно",
                                "сообразно",
                                "состоять",
                                "пока",
                                "необходимый",
                                "был",
                                "следовать",
                                "пред",
                                "отсюда",
                                "около",
                                "итак",
                                "от",
                                "спереди",
                                "чего",
                                "чтоб",
                                "перед",
                                "насчет",
                                "описать",
                                "нечего",
                                "ниоткуда",
                                "кроме",
                                "подчас",
                                "реализовать",
                                "давать",
                                "всюду",
                                "состоящий",
                                "вместо",
                                "до",
                                "здесь",
                                "указанный",
                                "предназначать",
                                "сначала",
                                "изготовить",
                                "другой",
                                "да",
                                "пользоваться",
                                "только",
                                "реализация",
                                "ближайший",
                                "прототип",
                                "делать",
                                "см",
                                "со",
                                "между",
                                "прежний",
                                "притом",
                                "должно",
                                "изобретательский",
                                "сразу",
                                "упрощение",
                                "наиболее",
                                "быть",
                                "за",
                                "причем",
                                "близкие",
                                "др",
                                "если",
                                "как",
                                "нужный",
                                "новый",
                                "путем",
                                "являться",
                                "еще",
                                "этот",
                                "поставленный"));

    // Нужно сжать стеммером
    Multimap<String, String> test = ArrayListMultimap.create();
    Set<String> compressed = new HashSet<String>();
    russianStemmer rs_new = new russianStemmer();
    for (String word: ignoredWordsRu) {
      rs_new.setCurrent(word);
      rs_new.stem();
      String compressedWord = rs_new.getCurrent();
      test.put(compressedWord, word);
      compressed.add(compressedWord);
    }

    /*for (String key: test.keySet()) {
      ImmutableAppUtils.print(
        Joiner.on(" ").join(
          key,
          test.get(key)));
    } */
    ImmutableAppUtils.print(compressed);

  }
}
