package architectureTest.server;

import java.util.Collections;
import java.util.List;

public class Sorter {
    static void sort(Long[] elems) {
        int numElems = elems.length;
        for (int i = numElems - 1; numElems >= 0; numElems--) {
            for (int j = 0; j <= i; j++) {
                if (elems[j] > elems[i]) {
                    Long tmp = elems[i];
                    elems[i] = elems[j];
                    elems[j] = tmp;
                }
            }
        }
    }
}
