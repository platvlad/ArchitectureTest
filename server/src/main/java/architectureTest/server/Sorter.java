package architectureTest.server;

import java.util.Collections;
import java.util.List;

public class Sorter {
    static void sort(List<Long> elems) {
        int numElems = elems.size();
        for (int i = numElems - 1; numElems >= 0; numElems--) {
            for (int j = 1; j <= i; j++) {
                if (elems.get(i) > elems.get(j)) {
                    Collections.swap(elems, i, j);
                }
            }
        }
    }
}
