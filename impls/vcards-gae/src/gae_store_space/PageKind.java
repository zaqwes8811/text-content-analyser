// Tasks:
//   Сперва подключить кеш,
//   Затем думать о распределении
//   Затем думать об удалении.

// http://www.oracle.com/technetwork/java/javase/documentation/index-137868.html#styleguide
// TODO: http://www.oracle.com/technetwork/articles/marx-jpa-087268.html
// TODO: скрыть персистентность в этом классе, пусть сам себя сохраняет и удаляет.
// TODO: Функция очистки данных связанных со страницей, себя не удаляет.
// TODO: Добавить оценки текста
// не хочется выносить ofy()... выше. Но может быть, если использовать класс пользователя, то он может.
/**
 * About:
 *   Отражает один элемент данный пользователя, например, один файл субтитров.
 */

package gae_store_space;

import static gae_store_space.OfyService.ofy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;
import pipeline.TextPipeline;
import pipeline.math.DistributionElement;
import servlets.protocols.PathValue;
import servlets.protocols.WordDataValue;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.cmd.Query;


@NotThreadSafe
@Entity
public class PageKind {
	
	
  private PageKind() { }

  public @Id Long id;

  @Index String name;
  
  String rawSource;  // для обновленной версии
  
  Integer boundaryPtr;  // указатель на текующю границу

  // Формированием не управляет, но остальным управляет.
  // Обязательно отсортировано
  @Ignore 
  private ArrayList<NGramKind> unigramKinds = new ArrayList<NGramKind>();
  
  // Хранить строго как в исходном контексте 
  @Ignore 
  private ArrayList<SentenceKind> sentencesKinds = new ArrayList<SentenceKind>();

  // FIXME: почему отношение не работает?
  // Попытка сделать так чтобы g не стал нулевым указателем
  // все равно может упасть. с единичным ключем фигня какая-то
  // FIXME: вообще это проблема
  @Load  
  private List<Key<GeneratorKind>> generators = new ArrayList<Key<GeneratorKind>>(); 
  
  @Ignore
	private static final Integer STEP_WINDOW_SIZE = 20;  // по столько будем шагать 
  
  //@Ignore
  private static TextPipeline buildPipeline() {
    return new TextPipeline();
  }
  
  public String getName() { 
  	return name; 
  }
  
  private void deleteGenerators() {
  	ofy().delete().keys(generators).now();
  }
  
  private void moveBoundary() {
  	// FIXME: 
  }
  
  public ArrayList<Integer> getLengthsSentences() {
  	ArrayList<Integer> r = new ArrayList<Integer>();
  	for (SentenceKind k : this.sentencesKinds)
  		r.add(k.getCountWords());
  	return r;
  }
  
  public void syncCreateInStore() {
  	persist();
  	
  	int j = 0;
		while (true) {
			if (j > GAESpecific.COUNT_TRIES)
				throw new IllegalStateException();
			
			Optional<PageKind> page_readed = Optional.of(ofy().load().type(PageKind.class).id(id).now()); 
	  	if (!page_readed.isPresent()) {
	  		j++;
	  		try {
	        Thread.sleep(GAESpecific.TIME_STEP_MS);
        } catch (InterruptedException e1) {
	        throw new RuntimeException(e1);
        }
	  		continue;
	  	}
			break;
		}
  }
  
  private static Optional<PageKind> syncGetPage(String name) {
  	// FIXME: можно прочитать только ключи, а потом делать выборки
   	List<PageKind> pages = ofy().load().type(PageKind.class).filter("name = ", name).list();
   	
   	int i = 0;
 		while (true) {
 			if (i > GAESpecific.COUNT_TRIES)
 				if (pages.size() != 0)
 					throw new IllegalStateException();
 			
 			// FIXME: не ясно нужно ли создавать каждый раз или можно реюзать
 			Query<PageKind> q = ofy().load().type(PageKind.class).filter("name = ", name);
 			pages = q.list();
 			
 			if (pages.size() > 1 || pages.size() == 0) {
 				try {
 					Thread.sleep(GAESpecific.TIME_STEP_MS);
 				} catch (InterruptedException e1) {
 					throw new RuntimeException(e1);
 				}
 				i++;
 				continue;
 		  }
 			break;
 		}
 		
		 if (pages.size() == 0)
		 	return Optional.absent();
		 
		 return Optional.of(pages.get(0));
  }
  
  // FIXME: если появится пользователи, то одного имени будет мало
  public static Optional<PageKind> syncRestore(String pageName) {
  	Optional<PageKind> page = PageKind.syncGetPage(pageName);
  	
  	if (page.isPresent()) {
	    String rawSource = page.get().rawSource;
	    
	    // обрабатываем
	    PageKind tmpPage = buildPipeline().pass(page.get().name, rawSource);
	    
	    // теперь нужно запустить процесс обработки,
	    page.get().assign(tmpPage);
    }
    
    return page;  // 1 item
  }
  
  private void assign(PageKind rhs) {
  	unigramKinds = rhs.unigramKinds;
  	sentencesKinds = rhs.sentencesKinds;
  }
  
  public void persist() {
  	ofy().save().entity(this).now();
  }

  // TODO: перенести бы в класс генератора, но!! это затрудняет выборку, т.к. имя не уникально 
  //
  // throws: 
  //   IllegalStateException - генератор не найден. Система замкнута, если 
  //     по имение не нашли генератора - это нарушение консистентности. Имена генереторов
  //     вводится только при создании, потом они только читаются.
  public Optional<GeneratorKind> getGenerator(String name) { 
  	if (name == null)
  		throw new IllegalArgumentException();
  	
  	if (generators.isEmpty())
  		throw new IllegalStateException();
  	
  	List<GeneratorKind> gen = 
  			ofy().load().type(GeneratorKind.class)
	  			.filterKey("in", generators)
	  			.filter("name = ", name)
	  			.list();

  	if (gen.isEmpty())
  		return Optional.absent();
  		
  	if (gen.size() > 1)
  		throw new IllegalStateException(name);
  	
  	gen.get(0).restore();
  	
  	return Optional.fromNullable(gen.get(0));
  }
  
  public List<String> getGenNames() {
  	List<GeneratorKind> gs = 
  			ofy().load().type(GeneratorKind.class)
	  			.filterKey("in", generators).list();
  	
  	List<String> r = new ArrayList<String>();
  	for (GeneratorKind g: gs) 
  		r.add(g.getName()); 

  	return r;
  }

  public void addGenerator(GeneratorKind gen) {
  	Key<GeneratorKind> k = Key.create(gen);
    generators.add(k);
  }

  // Это при создании с нуля
  public PageKind(
  		String name, ArrayList<SentenceKind> items, ArrayList<NGramKind> words, String rawSource) 
  	{
    this.name = name;
   	this.unigramKinds = words;
   	this.sentencesKinds = items;    
    this.rawSource = rawSource;
  }

  // About: Возвращать частоты, сортированные по убыванию.
  public ArrayList<DistributionElement> getRawDistribution() {
    // Сортируем - элементы могут прийти в случайном порядке
    Collections.sort(unigramKinds, NGramKind.createImportanceComparator());
    Collections.reverse(unigramKinds);
    
    // Как быть с окном?

    // Form result
    ArrayList<DistributionElement> r = new ArrayList<DistributionElement>();
    for (NGramKind word : unigramKinds)
      r.add(new DistributionElement(word.getImportance()));

    return r;
  }
   
  public Optional<WordDataValue> getWordData(String genName) {
  	GeneratorKind go = getGenerator(genName).get();  // FIXME: нужно нормально обработать
    
		Integer pointPosition = go.getPosition();
		NGramKind ngramKind =  getNGram(pointPosition);
		String value = ngramKind.getValue();
		ImmutableList<SentenceKind> sentenceKinds = ngramKind.getContendKinds();

		ArrayList<String> content = new ArrayList<String>(); 
		for (SentenceKind k: sentenceKinds)
		  content.add(k.getSentence());
		
		return Optional.of(new WordDataValue(value, content, pointPosition));
  }
  
  // FIXME: а логика разрешает Отсутствующее значение?
  // http://stackoverflow.com/questions/2758224/assertion-in-java
  // генераторы могут быть разными, но набор слов один.
  private NGramKind getNGram(Integer pos) {
  	if (! (pos < this.unigramKinds.size()))
  		throw new IllegalArgumentException();
  	
		return unigramKinds.get(pos);
  }
  
  public void disablePoint(PathValue p) {
  	GeneratorKind g = getGenerator(p.genName).get();
		g.disablePoint(p.pointPos);
		
		// Если накопили все в пределах границы сделано, то нужно сдвинуть границу и перегрузить генератор.
		
		ofy().save().entity(g).now();
  }
  
  public Optional<ImmutableList<DistributionElement>>  getDistribution(String genName) {
  	GeneratorKind gen = getGenerator(genName).get();
  	return Optional.of(gen.getDistribution());
  }
  
  public void asyncDeleteFromStore() {
  	deleteGenerators();  // это нужно вызвать, но при этом удаляется генератор новой страницы
		ofy().delete().type(PageKind.class).id(id).now();
  }
}
