@SuppressWarnings("unchecked")
class ArrayList<E> {
    /** 元素的数量 */
    private int size;
    /** 所有的元素 */
    private E[] elements;
    /** 容量 */
    private static final int DEFAULT_CAPATICY = 10;
    private static final int ELEMENT_NOT_FOUND = -1;

    public ArrayList(int capaticy) {
        capaticy  = capaticy < DEFAULT_CAPATICY ? DEFAULT_CAPATICY : capaticy;
        elements = (E[])new Object[capaticy];
    }

    public ArrayList() {
        this(DEFAULT_CAPATICY);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(E element) {
        return indexOf(element) != ELEMENT_NOT_FOUND;
    }

    /**
     * 在index位置插入一个元素
     * @param index
     * @param element
     */
    public void add(int index, E element) {
        // 检查下标越界
        if(index < 0 || index > size) {
            return throw new IndexOutOfBoundsException("index:" + index + ",size: " + size);
        }

        // 确保容量够大  扩容
        int oldCapacity = elements.length;
        if(oldCapacity < size + 1) {
            // 新容量为旧容量的1.5倍  >> 位运算
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            E[] newElements = (E[])new Object[newCapacity];

            for(int i = 0; i < size; i++) {
                newElements[i] = elements[i];
            }
            elements = newElements;
            System.out.println("size="+oldCapacity+", 扩容到了"+newCapacity);
        }

        // 先从后往前开始, 将每个元素往后移一位, 然后再赋值。如果从前往后开始移动元素，会造成错误的覆盖顺序
        for (int i = size - 1; i > index; i--) {
            elements[i + 1] = elements[i];
        }
        elements[index] = element;
        size++;
    }

    // 默认往数组最后添加元素
    public void add(E element) {
        add(size, element);
    }


    public E get(int index) {
        if(index < 0 || index >=size) {
            return throw new IndexOutOfBoundsException("index:" + index + ",size: " + size);
        }
        return elements[index];
    }

    /** 设置index位置的元素 */
    public E set(int index, E element) {
        if(index < 0 || index >=size) {
            return throw new IndexOutOfBoundsException("index:" + index + ",size: " + size);
        }
        E old = elements[index];
        elements[index] = element;
        return old;
    }


    /**删除index位置的元素 */
    public E remove(int index) {
        if(index < 0 || index >=size) {
            rreturn throw new IndexOutOfBoundsException("index:" + index + ",size: " + size);
        }

        // 缩容
        int capacity = elements.length;
        // size 小于容量一半 且容量大于默认容量 才进行缩容
        // 新容量为旧容量的0.5倍  >> 位运算 右移一位（除以二）
        int newCapacity = capacity >> 1
        if(size <= newCapacity && capacity >= DEFAULT_CAPATICY) {
            E[] newElements = (E[])new Object[newCapacity];
            for(int i = 0; i < size; i++) {
                newElements[i] = elements[i];
            }
            elements = newElements;
            System.out.println("size=, 缩容到了"+newCapacity);
        }


        // 0 1 2 3 4 5 	(index)
        // 1 2 3 4 5 6 	(原数组)
        // 删除index为2的元素，元素前移
        // 1 2 4 5 6	(remove后的数组)
        // 从前往后开始移, 用后面的元素覆盖前面的元素
        E old = elements[index];
        for(int i = index; i < size; i++) {
            elements[i] = elements[i+1];
        }
        elements[--size] = null; // 删除元素后, 将最后一位设置为null
        return old;
    }

    /** 查看元素的索引 */
    public int indexOf(E element) {
        if (element == null) {
            for (int i = 0; i < size; i++) {
                if(elements[i] == null) return i;
            }
        } else {
            for (int i = 0; i < size; i++) {
                // 对比两个对象是否相等
                if(element.equals(elements[i])) return i;
            }
        }
        return ELEMENT_NOT_FOUND;
    }

    /** 清除所有元素 */
    public void clear(){
        // 使用泛型数组后要注意内存管理(将元素置null)
        for (int i = 0; i < size; i++) {
            elements[i] = null;
        }
        size = 0;
    }

    @Overrid
    public boolean equals(Object obj){
        // Person person = (Person) obj
        return this.age == person.age
    }


    public toString() {
        StringBuilder string = new StringBuilder();
        string.append("size=").append(size).append(", [");
        for (int i = 0; i < size; i++) {
            if(0 != i) string.append(", ");
            string.append(elements[i]);
        }
        string.append("]");
        return string.toString();
    }

}
