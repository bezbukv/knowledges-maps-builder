package dal.gae_kinds;

import business.math.GeneratorDistributionsImpl;
import com.google.appengine.repackaged.org.apache.http.annotation.NotThreadSafe;
import com.google.common.collect.ImmutableList;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static dal.gae_kinds.OfyService.ofy;

/**
 * About: Отражает один элемент данный пользователя, например, один файл субтитров.
 */
@NotThreadSafe
@Entity
public class ContentPage {
  @Id
  Long id;

  @Index
  String name;
  @Load
  List<Key<Word>> words = new ArrayList<Key<Word>>();
  @Load
  List<Key<ContentItem>> items = new ArrayList<Key<ContentItem>>();

  private ContentPage() { }

  private boolean isEmpty_NI() {
    return false;
  }

  // TODO: возможно нужен кеш. см. Guava cache.
  public ImmutableList<GeneratorDistributionsImpl.DistributionElement> getDistribution() {
    List<Word> words = ofy().load().type(Word.class)
      .filterKey("in", this.words).list();

    // TODO: Отосортировать при выборке если можно
    Collections.sort(words, Word.createFreqComparator());
    Collections.reverse(words);

    // Формируем результат
    ArrayList<GeneratorDistributionsImpl.DistributionElement> distribution =
      new ArrayList<GeneratorDistributionsImpl.DistributionElement>();
    for (Word word : words) {
      // нужны частоты для распределения
      // TODO: true -> enabled
      GeneratorDistributionsImpl.DistributionElement elem =
        new GeneratorDistributionsImpl.DistributionElement(word.getFrequency(), true);
      distribution.add(elem);
    }

    return ImmutableList.copyOf(distribution);
  }

  public ContentPage(String name, List<ContentItem> items, List<Word> words) {
    this.name = name;
    this.setWords(words);
    this.setItems(items);
  }

  // TODO: Удяляет на что ссылается из хранилища.
  public void empty_NI() { }

  private void setItems(List<ContentItem> list) {
    for (ContentItem item: list) items.add(Key.create(item));
  }

  public List<Key<ContentItem>> getItems() {
    return items;
  }

  private void setWords(List<Word> words) {
    for (Word word: words) this.words.add(Key.create(word));
  }
}
