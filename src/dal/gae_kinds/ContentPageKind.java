package dal.gae_kinds;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

import java.util.ArrayList;
import java.util.List;

/**
 * About: Отражает один элемент данный пользователя, например, один файл субтитров.
 */
@Entity
public class ContentPageKind {
  @Id
  Long id;

  @Index String name;

  private ContentPageKind() {}

  public static ContentPageKind createFromComponents(String name, List<ContentItem> list, List<Word> words) {
    ContentPageKind page = new ContentPageKind(name);
    page.setWords(words);
    page.setItems(list);
    return page;
  }

  public static ContentPageKind create(String name, List<ContentItem> list) {
    return null;
  }


  public ContentPageKind(String name) {
    this.name = name;
  }

  // TODO:
  public void empty() {

  }

  // Content items
  @Load
  List<Key<ContentItem>> items = new ArrayList<Key<ContentItem>>();

  private void setItems(List<ContentItem> list) {
    for (ContentItem item: list) {
      items.add(Key.create(item));
    }
  }

  public List<Key<ContentItem>> getItems() { return items; }

  // Words
  @Load
  List<Key<Word>> words = new ArrayList<Key<Word>>();

  private void setWords(List<Word> words) {
    for (Word word: words) {
      this.words.add(Key.create(word));
    }
  }
}