package gae_store_space;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Serialize;
import cross_cuttings_layer.GlobalIO;
import instances.AppInstance;
import org.apache.log4j.Logger;
import org.javatuples.Pair;
import web_relays.protocols.PageSummaryValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static gae_store_space.OfyService.ofy;

// Must be full thread-safe
//
// Очень важен - попробую им гарантировать согласованность
@Entity
public class UserKind {
  private static Logger log = Logger.getLogger(UserKind.class.getName());
  //private  // FIXME:
  public UserKind() { }

  // FIXME: external lock
  // http://stackoverflow.com/questions/13197756/synchronized-method-calls-itself-recursively-is-this-broken

  // user is unique! can't do that with pages!
  @Id private String id;
  @Serialize private Set<String> pageNamesRegister;
  // FIXME: keys for pages!
  Set<Key<PageKind>> pageKeys = new HashSet<>();
  // FIXME: keys for filters!

  // FIXME: если кеш убрать работает много стабильнее
  private static final Integer CACHE_SIZE = 5;
  @Ignore
  LoadingCache<String, Optional<PageKind>> pagesCache = null;

  @Ignore
  GAEStoreAccessManager store = new GAEStoreAccessManager();

  public String getId() {
    return id;
  }

  private void checkPersisted() {
    if (id == null)
      throw new IllegalStateException();
  }

  private void reset() {
    if (!Optional.fromNullable(pageNamesRegister).isPresent())
      pageNamesRegister = new HashSet<>();

    if (pagesCache == null)
      pagesCache = CacheBuilder.newBuilder()
        .maximumSize(CACHE_SIZE)
        .build(
          new CacheLoader<String, Optional<PageKind>>() {
            @Override
            public Optional<PageKind> load(String key) {
              return PageKind.restore(key);
            }
          });
  }

  public void clear() {
    pageNamesRegister.clear();
  }

  public static UserKind createOrRestoreById(final String id) {
    UserKind r = ofy().transact(new Work<UserKind>() {
      @Override
      public UserKind run() {
        UserKind th = ofy().load().type(UserKind.class).id(id).now();
        if (th == null) {
          th = new UserKind();
          th.id = id;
          ofy().save().entity(th);
        }
        return th;
      }
    });

    r.checkPersisted();  // Это должно быть здесь
    r.reset();
    return r;
  }

  private void checkPageName(String pageName) {
    if (pageName == null)
      throw new IllegalArgumentException();
  }

  private boolean removePage(String pageName) {
    checkPageName(pageName);
    return pageNamesRegister.remove(pageName);
  }

  private boolean isContain(String pageName) {
    checkPageName(pageName);
    return pageNamesRegister.contains(pageName);
  }

  // скорее исследовательский метод
  // https://code.google.com/p/objectify-appengine/wiki/Transactions
  // FIXME: вот тут важна транзактивность
  public synchronized void createOrReplacePage(String pageName, String text) {
    log.info("Create = " + pageName);
    log.info(pageNamesRegister);
    // check register
    if (isContain(pageName)) {
      // страница была сохранена до этого
      PageKind page = getPage(pageName).get();
      removePage(pageName);
      page.atomicDelete();
      pagesCache.invalidate(pageName);
    }

    // Нужно чтобы ни в памяти, ни в хранилище не было пар!
    // это проверка только из памяти!!
    checkNotContain(pageName);

    boolean success = false;
    try {
      pageNamesRegister.add(pageName);
      //PageKind.atomicCreatePage(pageName, text);

      Pair<PageKind, GeneratorKind> pair = PageKind.process(pageName, text);
      final PageKind page = pair.getValue0();
      final GeneratorKind g = pair.getValue1();
      final UserKind user = this;

      // transaction boundary
      VoidWork work = new VoidWork() {
        @Override
        public void vrun() {
          ofy().save().entity(g).now();

          // нельзя не сохраненны присоединять - поэтому нельзя восп. сущ. методом
          page.setGenerator(g);

          ofy().save().entity(page).now();

          // need to save user!
          ofy().save().entity(user).now();
        }
      };
      // FIXME: база данный в каком состоянии будет тут? согласованном?
      // check here, but what can do?

      ofy().transactNew(GAEStoreAccessManager.COUNT_REPEATS, work);
      success = true;
    } finally {
      if (!success)
        pageNamesRegister.remove(pageName);
    }
  }

  public synchronized PageKind getPagePure(String pageName) {
    // check register
    Optional<PageKind> r = getPage(pageName);
    if (!r.isPresent())
      throw new IllegalArgumentException();

    return r.get();
  }

  // FIXME: may be non thread safe. Да вроде бы должно быть база то потокобезопасная?
  private Optional<PageKind> getPage(String pageName) {
    if (!isContain(pageName))
      return Optional.absent();

    Optional<PageKind> r = Optional.absent();
    // FIXME: danger but must work
    Integer countTries = 1000;
    while (true) {
      try {
        r = pagesCache.get(pageName);
      //} catch (UncheckedExecutionException ex) {

      } catch (ExecutionException ex) { }

      if (r.isPresent())
        break;

      // insert into cache but absent
      pagesCache.invalidate(pageName);
      countTries--;
      if (countTries < 0)
        throw new IllegalStateException(pageName);
    }
    return r;
  }

  public synchronized void createDefaultPage() {
    pagesCache.cleanUp();
    String pageName = AppInstance.defaultPageName;

    if (isContain(pageName))
      return;

    String text = GlobalIO.getGetPlainTextFromFile(AppInstance.getTestFileName());
    createPage(pageName, text);
  }

  private void checkNotContain(String pageName) {
    if (isContain(pageName))
      throw new AssertionError();
  }

  private void createPage(String pageName, String text) {
    boolean success = false;
    try {
      checkNotContain(pageName);
      pageNamesRegister.add(pageName);
      PageKind.atomicCreatePage(pageName, text);
      success = true;
    } finally {
      if (!success)
        pageNamesRegister.remove(pageName);
    }
  }

  public List<PageSummaryValue> getUserInformation() {
    List<PageKind> pages = ofy().load().type(PageKind.class).list();

    List<PageSummaryValue> r = new ArrayList<>();
    for (PageKind page: pages)
      r.add(PageSummaryValue.create(page.getName(), page.getGenNames()));

    return r;
  }
}
