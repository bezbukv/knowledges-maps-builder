package business.math;

import com.google.appengine.repackaged.org.apache.http.annotation.NotThreadSafe;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// About:
//   Класс способен генерировать последовательности любого дискретного распределения
//   Возвращает индекс массива исходного распределения.
//
@NotThreadSafe
public class GeneratorDistributionsImpl implements GeneratorDistributions {
  private Integer countPoints;
  private Integer maxValue;
  private ImmutableList<ImmutableList<Integer>> codeBook;  // TODO: Это сохранится в gae storage?

  // Любой список с числами
  // @throws: GeneratorDistributionExc
  public static GeneratorDistributionsImpl create(ArrayList<Integer> distribution) {
    if (distribution.isEmpty())
      throw new GeneratorDistributionExc("Input list must be no empty.");
    return new GeneratorDistributionsImpl(distribution);
  }

  @Override
  public Integer getPosition() {
    // Используется рекурсивная реализация на базе бинарного поиска.
    // На модели она показала наилучшую масштабирумость и скорость работы.
    Integer INTERVAL_POS = 1;
    Integer IDX_POS = 2;
    Float value = new Random().nextFloat()* maxValue;
    ImmutableList<Integer> result =  split(codeBook, countPoints, value).getValue1().get();
    return result.get(IDX_POS);
  }

  @Override
  public void reloadGenerator(ArrayList<DistributionElement> distribution) {
    // Transpose
    ArrayList<Integer> transposed = new ArrayList<Integer>();
    for (DistributionElement elem: distribution)
      // TODO: Проблема!! Похоже нужно обнулять частоты. Иначе индексы собъются.
      // TODO: но как быть при выборке. while(not null)? Это может быть долго...
      // TODO: Стоп - нулевые не должны вообще выпадать!
      if (elem.enabled)
        transposed.add(elem.frequency);
      else {
        transposed.add(0);
      }

    // TODO: Now only enabled
    Triplet<ArrayList<Integer>, Integer, Integer> tupleFx = makeFx(transposed);
    ArrayList<Integer> Fx = tupleFx.getValue0();
    countPoints = tupleFx.getValue1();
    maxValue = tupleFx.getValue2();
    codeBook = ImmutableList.copyOf(makeRanges(Fx));
  }

  private Triplet<ArrayList<Integer>, Integer, Integer> makeFx(ArrayList<Integer> distribution) {
    ArrayList<Integer> Fx = new ArrayList<Integer>();
    Integer Fxi = 0;
    Integer count = 0;
    for (final Integer frequency: distribution) {
      Fxi += frequency;
      Fx.add(Fxi);
      count++;
    }
    return Triplet.with(Fx, count, Fxi);
  }

  private List<ImmutableList<Integer>> makeRanges(ArrayList<Integer> Fx) {
    List<ImmutableList<Integer>> ranges = new ArrayList<ImmutableList<Integer>>();
    ranges.add(ImmutableList.of(0, Fx.get(0), 0));
    for (Integer i = 0; i < countPoints -1; i++) {
      ranges.add(ImmutableList.of(Fx.get(i), Fx.get(i+1), i+1));
    }
    return ranges;
  }

  private GeneratorDistributionsImpl(ArrayList<Integer> distribution) {
    // TODO: Сделать или нет? Можно ли вызывать виртуальные функции.
    //reloadGenerator(distribution);
    Triplet<ArrayList<Integer>, Integer, Integer> tupleFx = makeFx(distribution);
    ArrayList<Integer> Fx = tupleFx.getValue0();
    countPoints = tupleFx.getValue1();
    maxValue = tupleFx.getValue2();
    codeBook = ImmutableList.copyOf(makeRanges(Fx));
  }

  private Pair<Boolean, Optional<ImmutableList<Integer>>> split(
    ImmutableList<ImmutableList<Integer>> ranges, Integer n, Float value) {
    Boolean contain = isContain(ranges, n, value);
    if (!contain) {
      Optional<ImmutableList<Integer>> none = Optional.absent();
      return Pair.with(false, none);
    }
    if (n == 1) return Pair.with(true, Optional.of(ranges.get(0)));
    else {
      Integer oneSize = n/2;
      Integer twoSize = n-oneSize;
      Pair<Boolean, Optional<ImmutableList<Integer>>> resultSubTree =
          split(ranges.subList(0, oneSize), oneSize, value);
      if (!resultSubTree.getValue0()) {
        resultSubTree = split(ranges.subList(oneSize, n), twoSize, value);
      }
      return resultSubTree;
    }
  }

  private Boolean isContain(ImmutableList<ImmutableList<Integer>> ranges, Integer n, Float value) {
      return ranges.get(0).get(0) < value
        && value <= ranges.get(n-1).get(1);
  }
}
