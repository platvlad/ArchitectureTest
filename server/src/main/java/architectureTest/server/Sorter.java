package architectureTest.server;

public class Sorter {
    public static void sort(Long[] elems) {
        int numElems = elems.length;
        for (int i = numElems - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                if (elems[j] > elems[i]) {
                    Long tmp = elems[i];
                    elems[i] = elems[j];
                    elems[j] = tmp;
                }
            }
        }
    }
}
