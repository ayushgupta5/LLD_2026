class MyHashMap {
public:
    vector<list<pair<int, int>>> buckets;
    int size=10000;
    MyHashMap() {
        buckets.resize(size);
    }

    void put(int key, int value) {
        int bucketNumber = key % size;
        auto &chain = buckets[bucketNumber];
        for(auto &it : chain) {
            if(it.first == key) {
                it.second=value;
                return;
            }
        }
        chain.emplace_back(key, value);
    }

    int get(int key) {
        int bucketNumber = key % size;
        auto &chain = buckets[bucketNumber];
        if(chain.size()==0) return -1;
        for(auto &it: chain) {
            if(it.first == key) return it.second;
        }
        return -1;
    }

    void remove(int key) {
        int bucketNumber = key % size;
        auto &chain = buckets[bucketNumber];
        for(auto it = chain.begin(); it != chain.end(); it++) {
            if(it->first == key) {
                chain.erase(it);
                return;
            }
        }
    }
};