package numberparser.nlp;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * packagename:com.hualife.task.inner.numberparser.nlp
 * @author zhangqian
 * Time:2020年12月9日下午6:02:26
 * describe:实现python的zip函数
 */
public class Zip {
  /**
   * 
   * location:
   * @author zhangqian
   * Time:2020年12月9日下午6:03:16
   * describe:zip函数的实现
   * @param lists
   * @return
   */
  @SafeVarargs
  @SuppressWarnings("unchecked")
  public static <T> List<List<T>> zip(List<T>... lists) {
    List<List<T>> zipped = new ArrayList<List<T>>();
    for (List<T> list : lists) {
        for (int i = 0, listSize = list.size(); i < listSize; i++) {
            List<T> list2;
            if (i >= zipped.size())
                zipped.add(list2 = new ArrayList<T>());
            else
                list2 = zipped.get(i);
            list2.add(list.get(i));
        }
    }
    return zipped;
}

}
