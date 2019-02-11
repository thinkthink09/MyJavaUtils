package RandomWithProbablity;

import java.security.SecureRandom;
import java.util.List;

public class RandomWithProbablity {

  public static <E> RandomItem<E> randomSelector(List<RandomItem<E>> items) {
    SecureRandom rand = new SecureRandom();
    double totalProbability = items.stream().mapToDouble(RandomItem::getProbability).sum();

    double randomDouble = rand.nextDouble() * totalProbability;
    for (RandomItem<E> item : items) {
      randomDouble -= item.getProbability();
      if(randomDouble < 0) {
        return item;
      }
    }
    return items.get(items.size() -1);
  }

  public class RandomItem<E> {

    private E item;
    private double probability;

    public RandomItem(E item, double probability){
      this.item = item;
      this.probability = probability;
    }

    public E getItem() {
      return item;
    }

    public void setItem(E item) {
      this.item = item;
    }

    public double getProbability() {
      return probability;
    }

    public void setProbability(double probability) {
      this.probability = probability;
    }
  }

}
