import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import backend.KindGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import backend.math.DistributionElement;
import backend.math.GeneratorDistributionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static backend.OfyService.ofy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class KindGeneratorTest {
  private static final LocalServiceTestHelper helper =
    new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  

  @Before
  public void setUp() { helper.setUp(); }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  KindGenerator build(ArrayList<Integer> distribution) {

    ArrayList<DistributionElement> real = new ArrayList<DistributionElement>();

    for (Integer value: distribution) {
      DistributionElement d = new DistributionElement(value, true);
      d.setBoundary(true);
      real.add(d);
    }
    
    return KindGenerator.create(real);
  }

  @Test
  public void testCreate() throws GeneratorDistributionException {
    ArrayList<Integer> distribution = new ArrayList<Integer>(Arrays.asList(1, 6, 0, 14, 5, 7));

    KindGenerator g = build(distribution);
    
    assertNotNull(g);  // иногда падает

    List<Integer> experiment = new ArrayList<Integer>();
    for (int i = 0; i < 10000; ++i) {
      experiment.add(g.getPosition());
    }

    assertFalse(experiment.contains(new Integer(distribution.indexOf(0))));
  }

  @Test
  public void testPersist() {
    try (Closeable c = ObjectifyService.begin()) {
      {
        ArrayList<Integer> distribution = new ArrayList<Integer>(Arrays.asList(1, 6, 0, 14, 5, 7));
        KindGenerator d = build(distribution);

        ofy().save().entity(d).now();

        d.disablePoint(0);

        ofy().save().entity(d).now();
      }

      // try load
      {
        ofy().load().type(KindGenerator.class).id(1).now();
      }
    }
  }
}
