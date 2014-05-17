package dal_gae_kinds;

import business.math.GeneratorAnyDistribution;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.ArrayList;

/**
 * Created by zaqwes on 5/15/14.
 */
@Entity
public class GeneratorDistributionsFake implements ContentPage.GeneratorDistributions {
  /// Persist
  @Id
  Long id;

  /// Own
  @Override
  public Integer getPosition() {
    return 0;
  }

  @Override
  public void reloadGenerator(ArrayList<GeneratorAnyDistribution.DistributionElement> distribution) { }

  @Override
  public void disablePoint(Integer idx) {
    // TODO: Проверка границ - это явно ошибка

    // TODO: Похоже нужна non-XG - транзакция. Кажется может возникнуть исключение.
  }

  @Override
  public void enablePoint(Integer idx) {

  }
}