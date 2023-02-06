package online.flowerinsnow.mapreleaser.shaded.online.flowerinsnow.saussureautils.collection;

import java.util.ArrayList;
import java.util.List;

public abstract class ListUtils {
    private ListUtils() {
    }

    public static <T> ArrayList<T> reserve(List<T> list) {
        ArrayList<T> arrayList = new ArrayList<>();
        for (int i = list.size() - 1; i >= 0; i--) {
            arrayList.add(list.get(i));
        }
        return arrayList;
    }
}
