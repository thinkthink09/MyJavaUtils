package CollectionDiffAndIntersectUtil;

import java.lang.reflect.Method;
import java.util.*;

public class CollectionDiffAndIntersectUtil {
  /**
   * 從一組list轉換為指定的getter method取得得直為key，value為list的一對多的map
   *
   * @param methodNameOfKey
   * @param keyType
   * @param entities
   * @return
   */
  public static final <K, T> Map<K, List<T>> getGroupKeyMapping(String methodNameOfKey, Class<K> keyType, Collection<T> entities) {
    return getGroupKeyMapping(methodNameOfKey, keyType, entities, true, false);
  }

  /**
   * 從一組list轉換為指定的getter method取得得直為key，value為list的一對多的map。可指定是否忽略null值及資料是否被攤平
   * (攤平：key中有list會被攤成一個一個的item)
   *
   * @param methodNameOfKey
   * @param keyType
   * @param entities
   * @return
   */
  public static final <K, T> Map<K, List<T>> getGroupKeyMapping(String methodNameOfKey, Class<K> keyType, Collection<T> entities, boolean ignoreNullKey,
                                                                boolean flattenList) {

    Map<K, List<T>> map = new LinkedHashMap<>();

    Class noparams[] = {};
    Object[] emptyparam = new Object[] {};
    if (entities != null) {
      for (T entity : entities) {
        Class cls;
        try {
          Object tempK;
          if (entity instanceof Map) {
            tempK = ((Map) entity).get(methodNameOfKey);
          } else {
            cls = Class.forName(entity.getClass().getName());
            Method method = cls.getMethod(methodNameOfKey, noparams);
            tempK = method.invoke(entity, emptyparam);
          }
          List<K> lk = new ArrayList<>();
          if (flattenList && tempK instanceof List) {
            for (Object ok : (List) tempK) {
              lk.add(keyType.cast(ok));
            }
          } else {
            lk.add(keyType.cast(tempK));
          }
          for (K k : lk) {
            if (ignoreNullKey && k == null) {
              continue;
            }
            List<T> v = map.computeIfAbsent(k, k1 -> new ArrayList<>());
            v.add(entity);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    return map;
  }

  /**
   * 比較兩個collection,回傳兩個collection的差集以及交集 此方法使用Hash,速度上比iterator的方法快
   * 但只能以entity的某一個param做為key
   *
   * @param collectionA
   *          新的collection
   * @param collectionB
   *          舊的collection
   * @param methodNameOfKey
   *          key的getter
   * @param keyType
   *          key的ClassType
   * @param returnA
   *          intersect是否回傳collectionA中的entity, 反之則回傳B
   * @return DiffAndIntersect Class 分別可以getDiffA, getDiffB,
   *         getIntersect取得List<K>
   */
  public static <K, T> DiffAndIntersect<K> hashDiffAndIntersect(List<K> collectionA, List<K> collectionB, String methodNameOfKey, Class<T> keyType,
                                                                boolean returnA) {

    List<K> diffA = new ArrayList<>();
    List<K> diffB = new ArrayList<>();
    List<K> intersect = new ArrayList<>();

    Map<T, List<K>> setA = getGroupKeyMapping(methodNameOfKey, keyType, collectionA);
    Map<T, List<K>> setB = getGroupKeyMapping(methodNameOfKey, keyType, collectionB);

    Set<T> allSet = new HashSet<>();
    allSet.addAll(setA.keySet());
    allSet.addAll(setB.keySet());
    for (T t : allSet) {
      if (setA.get(t) == null || setA.get(t).isEmpty()) {
        diffB.addAll(setB.get(t));
      } else if (setB.get(t) == null || setB.get(t).isEmpty()) {
        diffA.addAll(setA.get(t));
      } else {
        if (returnA)
          intersect.addAll(setA.get(t));
        else
          intersect.addAll(setB.get(t));
      }
    }

    return new DiffAndIntersect<>(diffA, diffB, intersect);

  }

  /**
   * 比較兩個collection,回傳兩個collection的差集以及交集 此方法需自行實作EntityComparator 效能較Hash方法慢
   *
   * @param collectionA
   *          新的collection
   * @param collectionB
   *          舊的collection
   * @param comparator
   *          請自行實作EntityComparator的compare方法
   * @param returnNew
   *          intersect是否回傳collectionA中的entity, 反之則回傳B
   * @return DiffAndIntersect Class 分別可以getDiffA, getDiffB,
   *         getIntersect取得List<K>
   */
  public static <K> DiffAndIntersect<K> iterateDiffAndIntersect(List<K> collectionA, List<K> collectionB, Comparator<K> comparator, boolean returnNew) {

    List<K> diffA = new ArrayList<>();
    List<K> diffB = new ArrayList<>();
    List<K> intersect = new ArrayList<>();

    for (K entity : collectionB) {

      if (containsCheck(collectionA, entity, comparator)) {
        intersect.add(entity);
        removeFrom(collectionA, entity, comparator);
      } else {
        diffB.add(entity);
      }
    }
    diffA.addAll(collectionA);

    return new DiffAndIntersect<>(diffA, diffB, intersect);
  }

  public static class DiffAndIntersect<K> {

    public List<K> diffA;
    public List<K> diffB;
    public List<K> intersect;

    public DiffAndIntersect(List<K> diffA, List<K> diffB, List<K> intersect) {
      this.diffA = diffA;
      this.diffB = diffB;
      this.intersect = intersect;
    }
  }

  public static <K> boolean containsCheck(List<K> collection, K entity, Comparator<K> comparator) {

    for (K k : collection)
      if (comparator.compare(k, entity) == 0)
        return true;
    return false;
  }

  public static <K> void removeFrom(List<K> collection, K entity, Comparator<K> comparator) {

    for (K k : collection)
      if (comparator.compare(k, entity) == 0)
        collection.remove(k);
  }
}
