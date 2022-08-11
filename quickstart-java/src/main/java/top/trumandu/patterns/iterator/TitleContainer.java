package top.trumandu.patterns.iterator;

/**
 * @author Truman.P.Du
 * @date 2022/08/11
 * @description
 */
public class TitleContainer implements Container {
    String[] titles = {"初级", "中级", "高级", "架构师"};

    @Override
    public Iterator getIterator() {
        return new TitleIterator();
    }

    private class TitleIterator implements Iterator {
        int index = 0;

        @Override
        public boolean hasNext() {
            if (index < titles.length) {
                return true;
            }
            return false;
        }

        @Override
        public Object next() {
            if (hasNext()) {
                return titles[index++];
            }
            return null;
        }
    }

}
