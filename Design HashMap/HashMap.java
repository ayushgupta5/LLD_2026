import java.util.LinkedList;
import java.util.List;

/*
key-----> (HashFunction)----> Index
key % size
size=10000
Collision ho skta hai
Toh Ham chaining krte hai
chain bahut bada nahi hona chahiye nahi toh TC O(n) Lag Jayega
Worst Case mein chaining mein TC- O(n)
Then LoanFactor Comes into the Picture



*/

import java.util.*;

class MyHashMap {

    private static class Pair {
        int key;
        int value;

        Pair(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    private int SIZE = 10000;
    private List<LinkedList<Pair>> buckets;

    public MyHashMap() {
        buckets = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            buckets.add(new LinkedList<>());
        }
    }

    private int getBucketIndex(int key) {
        return key % SIZE;
    }

    public void put(int key, int value) {
        int bucketIndex = getBucketIndex(key);
        LinkedList<Pair> chain = buckets.get(bucketIndex);

        for (Pair pair : chain) {
            if (pair.key == key) {
                pair.value = value;   // update
                return;
            }
        }
        chain.add(new Pair(key, value));
    }

    public int get(int key) {
        int bucketIndex = getBucketIndex(key);
        LinkedList<Pair> chain = buckets.get(bucketIndex);

        for (Pair pair : chain) {
            if (pair.key == key) {
                return pair.value;
            }
        }
        return -1;
    }

    public void remove(int key) {
        int bucketIndex = getBucketIndex(key);
        LinkedList<Pair> chain = buckets.get(bucketIndex);

        Iterator<Pair> iterator = chain.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().key == key) {
                iterator.remove();
                return;
            }
        }
    }
}
