class RandomizedSet {
    unordered_map<int, int> mp;
    vector<int> v;

public:
    RandomizedSet() {}

    bool insert(int val) {
        if (mp.find(val) != mp.end()) return 0;
        mp[val] = v.size();
        v.push_back(val);
        return 1;
    }

    bool remove(int val) {
        if (mp.find(val) == mp.end()) return 0;
        v[mp[val]] = v.back();
        mp[v.back()] = mp[val];
        v.pop_back();
        mp.erase(val);
        return 1;
    }

    int getRandom() { return v[rand() % v.size()]; }
};