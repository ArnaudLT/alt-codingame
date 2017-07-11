
public class AltIntArray {

    int[] data;
    int size;

    AltIntArray(int[] initialElements, int initialSize) {
        this.data = initialElements;
        this.size = initialSize;
    }

    void clear() {
        this.size = 0;
    }

    void add(int element) {
        this.data[size] = element;
        size++;
    }

    int get(int index) {
        return this.data[index];
    }

}
