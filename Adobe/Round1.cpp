class RandomizedSet {
public:
    vector<int> arr;
    unordered_map<int, int> mp;
    RandomizedSet() {

    }

    bool insert(int val) {
        //present in the map
        if(mp.find(val) != mp.end()) {
            return false;
        }
        mp[val]=arr.size();
        arr.push_back(val);
        return true;
    }

    bool remove(int val) {
        // Not Present in the Map
        if(mp.find(val) == mp.end()) {
            return false;
        }
        int index=mp[val];
        arr[index]=arr.back();
        mp[arr.back()] = mp[val];

        arr.pop_back(); // erase from array
        mp.erase(val); // erase from map
        return true;
    }
    /*
    0 1 2 3 4 5
    1 2 6 4 5 6
    mp[3]=2 --> 5


    mp[4]=3
    mp[5]=4
    mp[6]=5 --> 2
    mp.erase(val);
    arr.pop_back();
    */

    int getRandom() {
        int len=arr.size();
        return arr[rand()%len];
    }
};


/*
Input
["RandomizedSet", "insert", "remove", "insert", "getRandom", "remove", "insert", "getRandom"]
[[], [1], [2], [2], [], [1], [2], []]
Output
[null, true, false, true, 2, true, false, 2]

ds=[2]
ds={1 2 3 4 5 6 7}
*/

/**
 * Definition for a binary tree node.
 * struct TreeNode {
 *     int val;
 *     TreeNode *left;
 *     TreeNode *right;
 *     TreeNode() : val(0), left(nullptr), right(nullptr) {}
 *     TreeNode(int x) : val(x), left(nullptr), right(nullptr) {}
 *     TreeNode(int x, TreeNode *left, TreeNode *right) : val(x), left(left), right(right) {}
 * };
 */
class FindElements {
public:
    unordered_set<int> st;
    FindElements(TreeNode* root) {
        queue<pair<TreeNode *, int>> q;
        q.push({root, 0});
        while(q.empty() == false) {
            auto [root, data] = q.front(); q.pop();
            st.insert(data);
            if(root->left) {
                q.push({root->left, 2*data+1});
            }
            if(root->right) {
                q.push({root->right, 2*data+2});
            }
        }
    }

    bool find(int target) {
        if(st.find(target) == st.end()) {
            return false;
        }
        return true;
    }
};
/*
Appraoch1. store all value in arr and use Binary Search Log(n)
Approach2: In Find() --> write the preprocessing logic --> TLE
Approach3: 14--> 6 --> 2-> 0 | 12 --> 5 --> 2 --> 0
*/
/**
 * Your FindElements object will be instantiated and called as such:
 * FindElements* obj = new FindElements(root);
 * bool param_1 = obj->find(target);
 */