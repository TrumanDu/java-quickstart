package top.trumandu.patterns.iterator;

/**
 * @author Truman.P.Du
 * @date 2022/08/11
 * @description
 */
public class Client {
    public static void main(String[] args) {
        Container titleContainer = new TitleContainer();
        Iterator iterator = titleContainer.getIterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }
}
