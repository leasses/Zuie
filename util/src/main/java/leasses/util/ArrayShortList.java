package leasses.util;

public class ArrayShortList {
    private int size = 0;
    private short[] elements;

    public ArrayShortList() {
        this(10);
    }

    public ArrayShortList(int initialCapacity) {
        elements = new short[initialCapacity];
    }

    public short get(int i) {
        return elements[i];
    }

    public void add(short o) {
        if (size >= elements.length) grow();
        elements[size++] = o;
    }

    private void grow() {
        var old = elements;
        elements = new short[size + 10];
        System.arraycopy(old, 0, elements, 0, size);
    }
}
