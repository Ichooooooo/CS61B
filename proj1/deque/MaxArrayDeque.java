package  deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T>{
    private Comparator<T> p;

    public MaxArrayDeque(Comparator<T> c) {
        p = c;
    }

    public T max() {
        return max(p);
    }

    public T max(Comparator<T> c) {
        if (isEmpty()) return null;

        T returnNum = get(0);
        for (int i = 1; i < size(); i++) {
            if (c.compare(get(i), returnNum) > 0) {
                returnNum = get(i);
            }
        }

        return returnNum;
    }
}