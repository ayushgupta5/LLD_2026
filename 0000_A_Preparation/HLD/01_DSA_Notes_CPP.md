# 🎯 DSA Interview Notes - C++ Solutions
> **Quick Revision Guide** | Interview: Tomorrow!
> **Current Role:** SWE at Grab

---

## 📌 Pattern Quick Reference

| Pattern | Problems | Key Data Structure |
|---------|----------|-------------------|
| **Stack/Monotonic** | #1, #3, #4, #8, #15 | `stack<int>` |
| **Binary Search** | #2, #45, #56 | Two pointers on answer |
| **Graph** | #5, #52, #53, #54 | `vector<vector<int>>` adj |
| **Sliding Window** | #11, #23, #47 | Two pointers + map |
| **Two Pointers** | #13, #19, #29, #31 | left, right |
| **DP** | #14, #16, #18, #27, #37, #42, #43 | `vector<int> dp` |
| **HashMap** | #7, #20, #22, #32 | `unordered_map` |
| **Tree** | #34, #41 | Recursion + DFS |

---

# 1. Min Stack ⭐

**Problem:** Design stack with push, pop, top, getMin all in O(1).

**Approach:** Use two stacks - main stack + min tracking stack.

```cpp
class MinStack {
private:
    stack<int> mainStack;
    stack<int> minStack;
    
public:
    void push(int val) {
        mainStack.push(val);
        if (minStack.empty() || val <= minStack.top()) {
            minStack.push(val);
        } else {
            minStack.push(minStack.top());
        }
    }
    
    void pop() {
        mainStack.pop();
        minStack.pop();
    }
    
    int top() {
        return mainStack.top();
    }
    
    int getMin() {
        return minStack.top();
    }
};
```

**Time:** O(1) all ops | **Space:** O(n)

**Interview Tip:** Mention space optimization - only push to minStack when new minimum.

---

# 2. Binary Search - Koko Eating Bananas Style ⭐⭐

**Problem:** Find minimum speed/capacity to complete task within time limit.

**Key Insight:** Binary search on ANSWER space, not array!

```cpp
class Solution {
public:
    int minEatingSpeed(vector<int>& piles, int h) {
        int left = 1;
        int right = *max_element(piles.begin(), piles.end());
        
        while (left < right) {
            int mid = left + (right - left) / 2;
            
            if (canFinish(piles, mid, h)) {
                right = mid;  // Try smaller speed
            } else {
                left = mid + 1;  // Need more speed
            }
        }
        return left;
    }
    
private:
    bool canFinish(vector<int>& piles, int speed, int h) {
        long long hours = 0;
        for (int pile : piles) {
            hours += (pile + speed - 1) / speed;  // Ceiling division
        }
        return hours <= h;
    }
};
```

**Time:** O(n log m) where m = max(piles) | **Space:** O(1)

**Template Recognition:**
- "Minimum capacity/speed to finish in K time" → Binary Search on Answer
- "Maximum minimum distance" → Binary Search on Answer

---

# 3. Next Greater Element ⭐⭐

**Problem:** For each element, find first greater element to its right.

**Approach:** Monotonic Decreasing Stack (traverse right to left)

```cpp
class Solution {
public:
    vector<int> nextGreaterElement(vector<int>& nums) {
        int n = nums.size();
        vector<int> result(n, -1);
        stack<int> st;  // Stores indices
        
        for (int i = n - 1; i >= 0; i--) {
            // Pop smaller or equal elements
            while (!st.empty() && nums[st.top()] <= nums[i]) {
                st.pop();
            }
            
            if (!st.empty()) {
                result[i] = nums[st.top()];
            }
            st.push(i);
        }
        return result;
    }
};
```

**Time:** O(n) | **Space:** O(n)

---

# 4. Next Greater Element - Circular Array ⭐⭐

**Key Change:** Traverse 2n times, use modulo for circular access.

```cpp
class Solution {
public:
    vector<int> nextGreaterElements(vector<int>& nums) {
        int n = nums.size();
        vector<int> result(n, -1);
        stack<int> st;
        
        // Traverse twice for circular behavior
        for (int i = 2 * n - 1; i >= 0; i--) {
            int idx = i % n;
            
            while (!st.empty() && nums[st.top()] <= nums[idx]) {
                st.pop();
            }
            
            if (i < n && !st.empty()) {
                result[idx] = nums[st.top()];
            }
            st.push(idx);
        }
        return result;
    }
};
```

---

# 5. Course Schedule - Topological Sort ⭐⭐⭐

**Problem:** Can all courses be completed? (Cycle detection)

### BFS - Kahn's Algorithm (Preferred in interviews)
```cpp
class Solution {
public:
    bool canFinish(int numCourses, vector<vector<int>>& prerequisites) {
        vector<vector<int>> graph(numCourses);
        vector<int> indegree(numCourses, 0);
        
        // Build graph
        for (auto& pre : prerequisites) {
            graph[pre[1]].push_back(pre[0]);
            indegree[pre[0]]++;
        }
        
        // Start with 0 indegree nodes
        queue<int> q;
        for (int i = 0; i < numCourses; i++) {
            if (indegree[i] == 0) q.push(i);
        }
        
        int completed = 0;
        while (!q.empty()) {
            int node = q.front(); q.pop();
            completed++;
            
            for (int neighbor : graph[node]) {
                indegree[neighbor]--;
                if (indegree[neighbor] == 0) {
                    q.push(neighbor);
                }
            }
        }
        
        return completed == numCourses;
    }
};
```

### DFS - Cycle Detection (3-color)
```cpp
class Solution {
public:
    bool canFinish(int numCourses, vector<vector<int>>& prerequisites) {
        vector<vector<int>> graph(numCourses);
        for (auto& pre : prerequisites) {
            graph[pre[1]].push_back(pre[0]);
        }
        
        // 0=unvisited, 1=visiting, 2=visited
        vector<int> state(numCourses, 0);
        
        for (int i = 0; i < numCourses; i++) {
            if (hasCycle(graph, i, state)) return false;
        }
        return true;
    }
    
private:
    bool hasCycle(vector<vector<int>>& graph, int node, vector<int>& state) {
        if (state[node] == 1) return true;   // Cycle detected
        if (state[node] == 2) return false;  // Already processed
        
        state[node] = 1;  // Mark visiting
        for (int neighbor : graph[node]) {
            if (hasCycle(graph, neighbor, state)) return true;
        }
        state[node] = 2;  // Mark visited
        return false;
    }
};
```

**Interview Tip:** Know BOTH approaches. BFS gives order directly, DFS is elegant for just cycle detection.

---

# 6. Optimizing Data Processing

**Interview Framework:**
```
"First, I'd profile to identify the bottleneck:
1. I/O bound → Async I/O, buffering, batch processing
2. CPU bound → Parallel processing, algorithm optimization
3. Memory bound → Streaming, chunking, memory-mapped files
4. Repeated work → Caching, memoization"
```

**Common C++ Optimizations:**
```cpp
// 1. Reserve vector capacity
vector<int> v;
v.reserve(n);  // Avoid reallocations

// 2. Use references to avoid copies
for (const auto& item : container) { }

// 3. Move semantics
vector<int> process() {
    vector<int> result;
    return result;  // RVO or move
}

// 4. Use emplace_back instead of push_back
v.emplace_back(args...);
```

---

# 7. Find Max Element in Dictionary ⭐

**Problem:** Track maximum while supporting insert/delete.

```cpp
class MaxTracker {
private:
    unordered_map<string, int> data;
    map<int, unordered_set<string>> valueToKeys;  // Sorted by value
    
public:
    void insert(string key, int value) {
        if (data.count(key)) {
            remove(key);
        }
        data[key] = value;
        valueToKeys[value].insert(key);
    }
    
    void remove(string key) {
        if (!data.count(key)) return;
        int value = data[key];
        valueToKeys[value].erase(key);
        if (valueToKeys[value].empty()) {
            valueToKeys.erase(value);
        }
        data.erase(key);
    }
    
    int getMax() {
        if (valueToKeys.empty()) return -1;
        return valueToKeys.rbegin()->first;  // Last element (max)
    }
};
```

**Time:** O(log n) for all operations

---

# 8. Monotonic Stack Pattern ⭐⭐

**When to Use:**
- Next/Previous Greater/Smaller element
- Stock span problems
- Largest rectangle in histogram

**Template - Next Smaller Element:**
```cpp
vector<int> nextSmaller(vector<int>& nums) {
    int n = nums.size();
    vector<int> result(n, -1);
    stack<int> st;  // Monotonic increasing stack
    
    for (int i = 0; i < n; i++) {
        while (!st.empty() && nums[st.top()] > nums[i]) {
            result[st.top()] = nums[i];
            st.pop();
        }
        st.push(i);
    }
    return result;
}
```

---

# 9. Backspace String Compare ⭐

**Problem:** Compare strings with '#' as backspace.

**O(1) Space - Two Pointers from End:**
```cpp
class Solution {
public:
    bool backspaceCompare(string s, string t) {
        int i = s.length() - 1;
        int j = t.length() - 1;
        
        while (i >= 0 || j >= 0) {
            i = getNextValidIndex(s, i);
            j = getNextValidIndex(t, j);
            
            if (i >= 0 && j >= 0) {
                if (s[i] != t[j]) return false;
            } else if (i >= 0 || j >= 0) {
                return false;
            }
            i--; j--;
        }
        return true;
    }
    
private:
    int getNextValidIndex(string& s, int idx) {
        int skip = 0;
        while (idx >= 0) {
            if (s[idx] == '#') {
                skip++;
                idx--;
            } else if (skip > 0) {
                skip--;
                idx--;
            } else {
                break;
            }
        }
        return idx;
    }
};
```

**Time:** O(n + m) | **Space:** O(1)

---

# 10. Longest Palindrome + Shortest Palindrome ⭐⭐⭐

### Part 1: Longest Palindrome from Characters
```cpp
int longestPalindromeLength(string s1, string s2) {
    string merged = s1 + s2;
    unordered_map<char, int> count;
    for (char c : merged) count[c]++;
    
    int length = 0;
    bool hasOdd = false;
    
    for (auto& [ch, freq] : count) {
        length += (freq / 2) * 2;
        if (freq % 2 == 1) hasOdd = true;
    }
    
    return length + (hasOdd ? 1 : 0);
}
```

### Part 2: Shortest Palindrome (Add chars at front)
```cpp
class Solution {
public:
    string shortestPalindrome(string s) {
        string rev = s;
        reverse(rev.begin(), rev.end());
        string combined = s + "#" + rev;
        
        // KMP failure function
        vector<int> lps(combined.length(), 0);
        for (int i = 1; i < combined.length(); i++) {
            int j = lps[i - 1];
            while (j > 0 && combined[i] != combined[j]) {
                j = lps[j - 1];
            }
            if (combined[i] == combined[j]) j++;
            lps[i] = j;
        }
        
        // Characters to add = reverse of suffix that's not part of palindrome
        return rev.substr(0, s.length() - lps.back()) + s;
    }
};
```

---

# 11. Longest Substring Without Repeating Characters ⭐⭐

```cpp
class Solution {
public:
    int lengthOfLongestSubstring(string s) {
        unordered_map<char, int> charIndex;
        int maxLen = 0;
        int left = 0;
        
        for (int right = 0; right < s.length(); right++) {
            if (charIndex.count(s[right]) && charIndex[s[right]] >= left) {
                left = charIndex[s[right]] + 1;
            }
            charIndex[s[right]] = right;
            maxLen = max(maxLen, right - left + 1);
        }
        
        return maxLen;
    }
};
```

**Time:** O(n) | **Space:** O(min(n, charset))

---

# 12. Minimum Bricks to Break ⭐⭐

**Key Insight:** Find where MOST gaps align → Break FEWEST bricks!

```cpp
class Solution {
public:
    int leastBricks(vector<vector<int>>& wall) {
        unordered_map<int, int> gapCount;
        
        for (auto& row : wall) {
            int position = 0;
            // Don't count the last brick (wall edge)
            for (int i = 0; i < row.size() - 1; i++) {
                position += row[i];
                gapCount[position]++;
            }
        }
        
        int maxGaps = 0;
        for (auto& [pos, count] : gapCount) {
            maxGaps = max(maxGaps, count);
        }
        
        return wall.size() - maxGaps;
    }
};
```

---

# 13. 2Sum, 3Sum, 4Sum ⭐⭐

### 2Sum - O(n) HashMap
```cpp
vector<int> twoSum(vector<int>& nums, int target) {
    unordered_map<int, int> seen;
    for (int i = 0; i < nums.size(); i++) {
        int complement = target - nums[i];
        if (seen.count(complement)) {
            return {seen[complement], i};
        }
        seen[nums[i]] = i;
    }
    return {};
}
```

### 3Sum - O(n²) Two Pointers
```cpp
vector<vector<int>> threeSum(vector<int>& nums) {
    vector<vector<int>> result;
    sort(nums.begin(), nums.end());
    int n = nums.size();
    
    for (int i = 0; i < n - 2; i++) {
        if (i > 0 && nums[i] == nums[i-1]) continue;  // Skip duplicates
        
        int left = i + 1, right = n - 1;
        while (left < right) {
            int sum = nums[i] + nums[left] + nums[right];
            
            if (sum == 0) {
                result.push_back({nums[i], nums[left], nums[right]});
                while (left < right && nums[left] == nums[left+1]) left++;
                while (left < right && nums[right] == nums[right-1]) right--;
                left++; right--;
            } else if (sum < 0) {
                left++;
            } else {
                right--;
            }
        }
    }
    return result;
}
```

### 4Sum - O(n³)
```cpp
vector<vector<int>> fourSum(vector<int>& nums, int target) {
    vector<vector<int>> result;
    if (nums.size() < 4) return result;
    
    sort(nums.begin(), nums.end());
    int n = nums.size();
    
    for (int i = 0; i < n - 3; i++) {
        if (i > 0 && nums[i] == nums[i-1]) continue;
        
        for (int j = i + 1; j < n - 2; j++) {
            if (j > i + 1 && nums[j] == nums[j-1]) continue;
            
            int left = j + 1, right = n - 1;
            while (left < right) {
                long long sum = (long long)nums[i] + nums[j] + nums[left] + nums[right];
                
                if (sum == target) {
                    result.push_back({nums[i], nums[j], nums[left], nums[right]});
                    while (left < right && nums[left] == nums[left+1]) left++;
                    while (left < right && nums[right] == nums[right-1]) right--;
                    left++; right--;
                } else if (sum < target) {
                    left++;
                } else {
                    right--;
                }
            }
        }
    }
    return result;
}
```

---

# 14. Dual Core Processor (Game Theory/DP) ⭐⭐⭐

**Problem:** Optimally assign tasks to minimize total time.

**Approach:** Partition problem - find subset closest to total/2

```cpp
int minTime(vector<int>& tasks) {
    int total = accumulate(tasks.begin(), tasks.end(), 0);
    int target = total / 2;
    
    vector<bool> dp(target + 1, false);
    dp[0] = true;
    
    for (int task : tasks) {
        for (int j = target; j >= task; j--) {
            dp[j] = dp[j] || dp[j - task];
        }
    }
    
    // Find largest achievable sum <= target
    for (int j = target; j >= 0; j--) {
        if (dp[j]) {
            return max(j, total - j);  // Time = max of two cores
        }
    }
    return total;
}
```

---

# 15. Gap to First Smaller Element ⭐⭐

```cpp
vector<int> findGaps(vector<int>& nums) {
    int n = nums.size();
    vector<int> result(n, 0);  // 0 = no smaller element
    stack<int> st;  // Monotonic increasing
    
    for (int i = 0; i < n; i++) {
        while (!st.empty() && nums[st.top()] > nums[i]) {
            int idx = st.top(); st.pop();
            result[idx] = i - idx;  // Distance
        }
        st.push(i);
    }
    return result;
}
```

---

# 16. Climbing Stairs Variant (1, 2, or 3 steps) ⭐

```cpp
int countWays(int n) {
    if (n <= 1) return 1;
    if (n == 2) return 2;
    
    int a = 1, b = 1, c = 2;
    for (int i = 3; i <= n; i++) {
        int temp = a + b + c;
        a = b;
        b = c;
        c = temp;
    }
    return c;
}
```

**Time:** O(n) | **Space:** O(1)

---

# 17. Max - Min Character Frequency ⭐

```cpp
int frequencyDifference(string s) {
    unordered_map<char, int> freq;
    for (char c : s) freq[c]++;
    
    int maxFreq = 0, minFreq = INT_MAX;
    for (auto& [ch, f] : freq) {
        maxFreq = max(maxFreq, f);
        minFreq = min(minFreq, f);
    }
    return maxFreq - minFreq;
}
```

---

# 18. Coin Change ⭐⭐

```cpp
int coinChange(vector<int>& coins, int amount) {
    vector<int> dp(amount + 1, amount + 1);
    dp[0] = 0;
    
    for (int coin : coins) {
        for (int i = coin; i <= amount; i++) {
            dp[i] = min(dp[i], dp[i - coin] + 1);
        }
    }
    
    return dp[amount] > amount ? -1 : dp[amount];
}
```

**Time:** O(amount × coins) | **Space:** O(amount)

---

# 19. Squares of Sorted Array ⭐

```cpp
vector<int> sortedSquares(vector<int>& nums) {
    int n = nums.size();
    vector<int> result(n);
    int left = 0, right = n - 1;
    int pos = n - 1;
    
    while (left <= right) {
        int leftSq = nums[left] * nums[left];
        int rightSq = nums[right] * nums[right];
        
        if (leftSq > rightSq) {
            result[pos--] = leftSq;
            left++;
        } else {
            result[pos--] = rightSq;
            right--;
        }
    }
    return result;
}
```

---

# 20. Top K Frequent Elements ⭐⭐

### Using Heap - O(n log k)
```cpp
vector<int> topKFrequent(vector<int>& nums, int k) {
    unordered_map<int, int> count;
    for (int num : nums) count[num]++;
    
    // Min heap of (frequency, number)
    priority_queue<pair<int,int>, vector<pair<int,int>>, greater<>> minHeap;
    
    for (auto& [num, freq] : count) {
        minHeap.push({freq, num});
        if (minHeap.size() > k) minHeap.pop();
    }
    
    vector<int> result;
    while (!minHeap.empty()) {
        result.push_back(minHeap.top().second);
        minHeap.pop();
    }
    return result;
}
```

### Bucket Sort - O(n)
```cpp
vector<int> topKFrequent_bucket(vector<int>& nums, int k) {
    unordered_map<int, int> count;
    for (int num : nums) count[num]++;
    
    vector<vector<int>> buckets(nums.size() + 1);
    for (auto& [num, freq] : count) {
        buckets[freq].push_back(num);
    }
    
    vector<int> result;
    for (int i = buckets.size() - 1; i >= 0 && result.size() < k; i--) {
        for (int num : buckets[i]) {
            result.push_back(num);
            if (result.size() == k) return result;
        }
    }
    return result;
}
```

---

# 21-24. Basic Array/HashMap/Sliding Window/String Problems

**Interview Patterns Summary:**

```cpp
// SLIDING WINDOW - Fixed Size
int fixedWindow(vector<int>& arr, int k) {
    int sum = accumulate(arr.begin(), arr.begin() + k, 0);
    int maxSum = sum;
    for (int i = k; i < arr.size(); i++) {
        sum += arr[i] - arr[i - k];
        maxSum = max(maxSum, sum);
    }
    return maxSum;
}

// SLIDING WINDOW - Variable Size
int variableWindow(vector<int>& arr, int target) {
    int left = 0, current = 0, result = 0;
    for (int right = 0; right < arr.size(); right++) {
        current += arr[right];
        while (current > target) {
            current -= arr[left++];
        }
        result = max(result, right - left + 1);
    }
    return result;
}

// TWO POINTERS
void twoPointers(vector<int>& arr) {
    int left = 0, right = arr.size() - 1;
    while (left < right) {
        // Process and move pointers
    }
}
```

---

# 25. Min Changes - No Adjacent Same ⭐⭐

```cpp
int minChanges(vector<string>& strings) {
    int changes = 0;
    char prevChar = '\0';
    
    for (string& s : strings) {
        for (char c : s) {
            if (c == prevChar) {
                changes++;
                prevChar = '#';  // Changed to different
            } else {
                prevChar = c;
            }
        }
    }
    return changes;
}
```

---

# 26. Products with Multiple Discounts ⭐⭐

**Approach:** DP or Greedy based on discount rules.

```cpp
// If we can choose which discounts to apply
int minCost(vector<int>& prices, vector<vector<int>>& discounts) {
    int n = prices.size();
    vector<int> dp(n + 1, INT_MAX);
    dp[0] = 0;
    
    for (int i = 0; i < n; i++) {
        if (dp[i] == INT_MAX) continue;
        
        // Buy without discount
        dp[i + 1] = min(dp[i + 1], dp[i] + prices[i]);
        
        // Apply each applicable discount
        for (auto& disc : discounts) {
            // Apply discount logic
        }
    }
    return dp[n];
}
```

---

# 27. Unique Binary Search Trees (Catalan) ⭐⭐

```cpp
int numTrees(int n) {
    vector<int> dp(n + 1, 0);
    dp[0] = dp[1] = 1;
    
    for (int nodes = 2; nodes <= n; nodes++) {
        for (int root = 1; root <= nodes; root++) {
            dp[nodes] += dp[root - 1] * dp[nodes - root];
        }
    }
    return dp[n];
}
```

**Formula:** Catalan(n) = C(2n, n) / (n+1)

---

# 28. 3Sum ⭐⭐
*(Same as #13)*

---

# 29. Valid Palindrome ⭐

```cpp
bool isPalindrome(string s) {
    int left = 0, right = s.length() - 1;
    
    while (left < right) {
        while (left < right && !isalnum(s[left])) left++;
        while (left < right && !isalnum(s[right])) right--;
        
        if (tolower(s[left]) != tolower(s[right])) {
            return false;
        }
        left++; right--;
    }
    return true;
}
```

---

# 30. Palindrome Linked List - O(1) Space ⭐⭐

```cpp
bool isPalindrome(ListNode* head) {
    if (!head || !head->next) return true;
    
    // Find middle
    ListNode *slow = head, *fast = head;
    while (fast->next && fast->next->next) {
        slow = slow->next;
        fast = fast->next->next;
    }
    
    // Reverse second half
    ListNode* secondHalf = reverseList(slow->next);
    
    // Compare
    ListNode* firstHalf = head;
    while (secondHalf) {
        if (firstHalf->val != secondHalf->val) return false;
        firstHalf = firstHalf->next;
        secondHalf = secondHalf->next;
    }
    return true;
}

ListNode* reverseList(ListNode* head) {
    ListNode* prev = nullptr;
    while (head) {
        ListNode* next = head->next;
        head->next = prev;
        prev = head;
        head = next;
    }
    return prev;
}
```

---

# 31. Merge Two Sorted Arrays ⭐

```cpp
void merge(vector<int>& nums1, int m, vector<int>& nums2, int n) {
    int p1 = m - 1, p2 = n - 1, p = m + n - 1;
    
    while (p2 >= 0) {
        if (p1 >= 0 && nums1[p1] > nums2[p2]) {
            nums1[p--] = nums1[p1--];
        } else {
            nums1[p--] = nums2[p2--];
        }
    }
}
```

---

# 32. Implement HashMap ⭐⭐

```cpp
class MyHashMap {
private:
    static const int SIZE = 1000;
    vector<list<pair<int, int>>> buckets;
    
    int hash(int key) {
        return key % SIZE;
    }
    
public:
    MyHashMap() : buckets(SIZE) {}
    
    void put(int key, int value) {
        int idx = hash(key);
        for (auto& [k, v] : buckets[idx]) {
            if (k == key) {
                v = value;
                return;
            }
        }
        buckets[idx].push_back({key, value});
    }
    
    int get(int key) {
        int idx = hash(key);
        for (auto& [k, v] : buckets[idx]) {
            if (k == key) return v;
        }
        return -1;
    }
    
    void remove(int key) {
        int idx = hash(key);
        buckets[idx].remove_if([key](auto& p) { return p.first == key; });
    }
};
```

**Follow-up Topics:** Load factor, rehashing, collision handling (chaining vs open addressing)

---

# 33. Multiply Strings ⭐⭐

```cpp
string multiply(string num1, string num2) {
    if (num1 == "0" || num2 == "0") return "0";
    
    int m = num1.size(), n = num2.size();
    vector<int> result(m + n, 0);
    
    for (int i = m - 1; i >= 0; i--) {
        for (int j = n - 1; j >= 0; j--) {
            int mul = (num1[i] - '0') * (num2[j] - '0');
            int p1 = i + j, p2 = i + j + 1;
            
            int sum = mul + result[p2];
            result[p2] = sum % 10;
            result[p1] += sum / 10;
        }
    }
    
    string str;
    for (int num : result) {
        if (!(str.empty() && num == 0)) {
            str += to_string(num);
        }
    }
    return str.empty() ? "0" : str;
}
```

---

# 34. Max Path Sum - Binary Tree (Leaf to Leaf) ⭐⭐⭐

```cpp
class Solution {
    int maxSum = INT_MIN;
    
public:
    int maxPathSum(TreeNode* root) {
        dfs(root);
        return maxSum;
    }
    
private:
    int dfs(TreeNode* node) {
        if (!node) return 0;
        
        int left = dfs(node->left);
        int right = dfs(node->right);
        
        // If both children exist, consider path through this node
        if (node->left && node->right) {
            maxSum = max(maxSum, left + right + node->val);
            return max(left, right) + node->val;
        }
        
        // Return path continuing to leaf
        return (node->left ? left : right) + node->val;
    }
};
```

---

# 35. Divide Array into 3 Equal Sum Parts ⭐⭐

```cpp
int ways(vector<int>& nums) {
    int total = accumulate(nums.begin(), nums.end(), 0);
    if (total % 3 != 0) return 0;
    
    int target = total / 3;
    int n = nums.size();
    int count = 0, ways = 0;
    int prefixSum = 0;
    
    for (int i = 0; i < n - 1; i++) {
        prefixSum += nums[i];
        
        // If we can end second partition here
        if (prefixSum == 2 * target) {
            ways += count;
        }
        
        // Count where first partition can end
        if (prefixSum == target) {
            count++;
        }
    }
    return ways;
}
```

---

# 36. Stock Buy/Sell - One Transaction ⭐

```cpp
int maxProfit(vector<int>& prices) {
    int minPrice = INT_MAX;
    int maxProfit = 0;
    
    for (int price : prices) {
        minPrice = min(minPrice, price);
        maxProfit = max(maxProfit, price - minPrice);
    }
    return maxProfit;
}
```

---

# 37. Decode Ways ⭐⭐

```cpp
int numDecodings(string s) {
    if (s.empty() || s[0] == '0') return 0;
    
    int n = s.length();
    vector<int> dp(n + 1, 0);
    dp[0] = 1;
    dp[1] = 1;
    
    for (int i = 2; i <= n; i++) {
        // Single digit
        if (s[i-1] != '0') {
            dp[i] += dp[i-1];
        }
        // Two digits
        int twoDigit = stoi(s.substr(i-2, 2));
        if (twoDigit >= 10 && twoDigit <= 26) {
            dp[i] += dp[i-2];
        }
    }
    return dp[n];
}
```

---

# 38. Array to Linked List ⭐

```cpp
ListNode* arrayToList(vector<int>& arr) {
    if (arr.empty()) return nullptr;
    
    ListNode* head = new ListNode(arr[0]);
    ListNode* curr = head;
    
    for (int i = 1; i < arr.size(); i++) {
        curr->next = new ListNode(arr[i]);
        curr = curr->next;
    }
    return head;
}
```

---

# 39. Greedy with Sorting ⭐

**Common Patterns:**
```cpp
// Interval Scheduling - Sort by END time
sort(intervals.begin(), intervals.end(), [](auto& a, auto& b) {
    return a[1] < b[1];
});

// Meeting Rooms - Sort by START time
sort(intervals.begin(), intervals.end());

// Activity Selection
int maxActivities(vector<pair<int,int>>& activities) {
    sort(activities.begin(), activities.end(), [](auto& a, auto& b) {
        return a.second < b.second;
    });
    
    int count = 1, lastEnd = activities[0].second;
    for (int i = 1; i < activities.size(); i++) {
        if (activities[i].first >= lastEnd) {
            count++;
            lastEnd = activities[i].second;
        }
    }
    return count;
}
```

---

# 40. Queue Using Arrays ⭐

```cpp
class Queue {
private:
    vector<int> arr;
    int front, rear, size, capacity;
    
public:
    Queue(int cap) : capacity(cap), front(0), rear(-1), size(0) {
        arr.resize(cap);
    }
    
    void enqueue(int item) {
        if (size == capacity) throw runtime_error("Queue full");
        rear = (rear + 1) % capacity;
        arr[rear] = item;
        size++;
    }
    
    int dequeue() {
        if (size == 0) throw runtime_error("Queue empty");
        int item = arr[front];
        front = (front + 1) % capacity;
        size--;
        return item;
    }
    
    int peek() {
        if (size == 0) return -1;
        return arr[front];
    }
    
    bool isEmpty() { return size == 0; }
};
```

---

# 41. Distance Between Two Nodes in Binary Tree ⭐⭐

```cpp
class Solution {
public:
    int findDistance(TreeNode* root, int p, int q) {
        TreeNode* lca = findLCA(root, p, q);
        return findDist(lca, p, 0) + findDist(lca, q, 0);
    }
    
private:
    TreeNode* findLCA(TreeNode* node, int p, int q) {
        if (!node || node->val == p || node->val == q) return node;
        
        TreeNode* left = findLCA(node->left, p, q);
        TreeNode* right = findLCA(node->right, p, q);
        
        if (left && right) return node;
        return left ? left : right;
    }
    
    int findDist(TreeNode* node, int target, int dist) {
        if (!node) return -1;
        if (node->val == target) return dist;
        
        int left = findDist(node->left, target, dist + 1);
        if (left != -1) return left;
        return findDist(node->right, target, dist + 1);
    }
};
```

---

# 42. Maximal Square ⭐⭐

```cpp
int maximalSquare(vector<vector<char>>& matrix) {
    if (matrix.empty()) return 0;
    
    int m = matrix.size(), n = matrix[0].size();
    vector<vector<int>> dp(m + 1, vector<int>(n + 1, 0));
    int maxSide = 0;
    
    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            if (matrix[i-1][j-1] == '1') {
                dp[i][j] = min({dp[i-1][j], dp[i][j-1], dp[i-1][j-1]}) + 1;
                maxSide = max(maxSide, dp[i][j]);
            }
        }
    }
    return maxSide * maxSide;
}
```

---

# 43. Egg Dropping ⭐⭐⭐

```cpp
int eggDrop(int eggs, int floors) {
    vector<vector<int>> dp(eggs + 1, vector<int>(floors + 1, 0));
    
    // Base cases
    for (int f = 1; f <= floors; f++) dp[1][f] = f;
    for (int e = 1; e <= eggs; e++) dp[e][1] = 1;
    
    for (int e = 2; e <= eggs; e++) {
        for (int f = 2; f <= floors; f++) {
            dp[e][f] = INT_MAX;
            
            // Binary search optimization
            int lo = 1, hi = f;
            while (lo <= hi) {
                int mid = (lo + hi) / 2;
                int breakCase = dp[e-1][mid-1];
                int noBreak = dp[e][f-mid];
                int worst = 1 + max(breakCase, noBreak);
                
                dp[e][f] = min(dp[e][f], worst);
                
                if (breakCase < noBreak) lo = mid + 1;
                else hi = mid - 1;
            }
        }
    }
    return dp[eggs][floors];
}
```

**Time:** O(e × f × log f) with binary search optimization

---

# 44. Maximum Subarray (Kadane's) ⭐

```cpp
int maxSubArray(vector<int>& nums) {
    int maxSum = nums[0];
    int currentSum = nums[0];
    
    for (int i = 1; i < nums.size(); i++) {
        currentSum = max(nums[i], currentSum + nums[i]);
        maxSum = max(maxSum, currentSum);
    }
    return maxSum;
}
```

---

# 45. Ship Capacity (Binary Search) ⭐⭐

```cpp
int shipWithinDays(vector<int>& weights, int days) {
    int left = *max_element(weights.begin(), weights.end());
    int right = accumulate(weights.begin(), weights.end(), 0);
    
    while (left < right) {
        int mid = left + (right - left) / 2;
        
        if (canShip(weights, mid, days)) {
            right = mid;
        } else {
            left = mid + 1;
        }
    }
    return left;
}

bool canShip(vector<int>& weights, int capacity, int days) {
    int currentLoad = 0, daysNeeded = 1;
    for (int w : weights) {
        if (currentLoad + w > capacity) {
            daysNeeded++;
            currentLoad = 0;
        }
        currentLoad += w;
    }
    return daysNeeded <= days;
}
```

---

# 46. Max Profit - Multiple Transactions ⭐

```cpp
int maxProfit(vector<int>& prices) {
    int profit = 0;
    for (int i = 1; i < prices.size(); i++) {
        if (prices[i] > prices[i-1]) {
            profit += prices[i] - prices[i-1];
        }
    }
    return profit;
}
```

---

# 47. Minimum Window Substring ⭐⭐⭐

```cpp
string minWindow(string s, string t) {
    if (t.empty()) return "";
    
    unordered_map<char, int> tCount, window;
    for (char c : t) tCount[c]++;
    
    int required = tCount.size();
    int formed = 0;
    int left = 0;
    int minLen = INT_MAX, minStart = 0;
    
    for (int right = 0; right < s.length(); right++) {
        char c = s[right];
        window[c]++;
        
        if (tCount.count(c) && window[c] == tCount[c]) {
            formed++;
        }
        
        while (formed == required) {
            if (right - left + 1 < minLen) {
                minLen = right - left + 1;
                minStart = left;
            }
            
            char leftChar = s[left];
            window[leftChar]--;
            if (tCount.count(leftChar) && window[leftChar] < tCount[leftChar]) {
                formed--;
            }
            left++;
        }
    }
    
    return minLen == INT_MAX ? "" : s.substr(minStart, minLen);
}
```

---

# 48. Task Scheduler ⭐⭐

```cpp
int leastInterval(vector<char>& tasks, int n) {
    unordered_map<char, int> counts;
    for (char t : tasks) counts[t]++;
    
    int maxCount = 0, maxCountTasks = 0;
    for (auto& [task, count] : counts) {
        if (count > maxCount) {
            maxCount = count;
            maxCountTasks = 1;
        } else if (count == maxCount) {
            maxCountTasks++;
        }
    }
    
    // (maxCount - 1) chunks of size (n + 1) + tasks with maxCount
    int result = (maxCount - 1) * (n + 1) + maxCountTasks;
    
    return max(result, (int)tasks.size());
}
```

---

# 49. Maximum Planes Prevented ⭐⭐

```cpp
int maxPrevented(vector<pair<int, int>>& planes) {
    // planes = {position, speed}
    // landing_time = position / speed
    vector<double> landingTimes;
    for (auto& [pos, speed] : planes) {
        landingTimes.push_back((double)pos / speed);
    }
    sort(landingTimes.begin(), landingTimes.end());
    
    int count = 0;
    double currentTime = 0;
    
    for (double landTime : landingTimes) {
        if (currentTime <= landTime) {
            count++;
            currentTime = landTime + 1;  // Time to intercept
        }
    }
    return count;
}
```

---

# 50. Unique Paths with Obstacles ⭐⭐

```cpp
int uniquePathsWithObstacles(vector<vector<int>>& grid) {
    int m = grid.size(), n = grid[0].size();
    if (grid[0][0] == 1) return 0;
    
    vector<vector<long long>> dp(m, vector<long long>(n, 0));
    dp[0][0] = 1;
    
    for (int i = 0; i < m; i++) {
        for (int j = 0; j < n; j++) {
            if (grid[i][j] == 1) {
                dp[i][j] = 0;
            } else {
                if (i > 0) dp[i][j] += dp[i-1][j];
                if (j > 0) dp[i][j] += dp[i][j-1];
            }
        }
    }
    return dp[m-1][n-1];
}
```

---

# 51. Promise Chaining (JavaScript) ⭐

```javascript
// Sequential
async function sequential() {
    const r1 = await fetch(url1);
    const r2 = await fetch(url2);
    return [r1, r2];
}

// Parallel
async function parallel() {
    const [r1, r2] = await Promise.all([fetch(url1), fetch(url2)]);
    return [r1, r2];
}

// Chaining
fetch(url)
    .then(res => res.json())
    .then(data => process(data))
    .catch(err => console.error(err));
```

---

# 52. Cycle Detection ⭐⭐

### Undirected - Union Find
```cpp
class UnionFind {
    vector<int> parent, rank;
public:
    UnionFind(int n) : parent(n), rank(n, 0) {
        iota(parent.begin(), parent.end(), 0);
    }
    
    int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]);
        return parent[x];
    }
    
    bool unite(int x, int y) {
        int px = find(x), py = find(y);
        if (px == py) return true;  // Cycle!
        if (rank[px] < rank[py]) swap(px, py);
        parent[py] = px;
        if (rank[px] == rank[py]) rank[px]++;
        return false;
    }
};

bool hasCycle(int n, vector<vector<int>>& edges) {
    UnionFind uf(n);
    for (auto& e : edges) {
        if (uf.unite(e[0], e[1])) return true;
    }
    return false;
}
```

### Directed - DFS with Colors
```cpp
bool hasCycleDirected(int n, vector<vector<int>>& edges) {
    vector<vector<int>> graph(n);
    for (auto& e : edges) graph[e[0]].push_back(e[1]);
    
    vector<int> color(n, 0);  // 0=white, 1=gray, 2=black
    
    function<bool(int)> dfs = [&](int node) {
        color[node] = 1;
        for (int neighbor : graph[node]) {
            if (color[neighbor] == 1) return true;
            if (color[neighbor] == 0 && dfs(neighbor)) return true;
        }
        color[node] = 2;
        return false;
    };
    
    for (int i = 0; i < n; i++) {
        if (color[i] == 0 && dfs(i)) return true;
    }
    return false;
}
```

---

# 53. Topological Sort ⭐⭐
*(Same as #5)*

---

# 54. BFS/DFS Templates ⭐⭐

```cpp
// BFS
void bfs(vector<vector<int>>& graph, int start) {
    vector<bool> visited(graph.size(), false);
    queue<int> q;
    q.push(start);
    visited[start] = true;
    
    while (!q.empty()) {
        int node = q.front(); q.pop();
        for (int neighbor : graph[node]) {
            if (!visited[neighbor]) {
                visited[neighbor] = true;
                q.push(neighbor);
            }
        }
    }
}

// DFS
void dfs(vector<vector<int>>& graph, int node, vector<bool>& visited) {
    visited[node] = true;
    for (int neighbor : graph[node]) {
        if (!visited[neighbor]) {
            dfs(graph, neighbor, visited);
        }
    }
}
```

---

# 55. RandomizedSet (O(1) Operations) ⭐⭐

```cpp
class RandomizedSet {
private:
    unordered_map<int, int> valToIndex;
    vector<int> values;
    
public:
    bool insert(int val) {
        if (valToIndex.count(val)) return false;
        valToIndex[val] = values.size();
        values.push_back(val);
        return true;
    }
    
    bool remove(int val) {
        if (!valToIndex.count(val)) return false;
        
        int idx = valToIndex[val];
        int last = values.back();
        
        values[idx] = last;
        valToIndex[last] = idx;
        
        values.pop_back();
        valToIndex.erase(val);
        return true;
    }
    
    int getRandom() {
        return values[rand() % values.size()];
    }
};
```

---

# 56. Binary Search - Max of Minimum ⭐⭐

```cpp
int maxOfMinimum(vector<int>& arr, int k) {
    int left = *min_element(arr.begin(), arr.end());
    int right = *max_element(arr.begin(), arr.end());
    
    while (left < right) {
        int mid = left + (right - left + 1) / 2;  // Upper mid
        
        if (isValid(arr, mid, k)) {
            left = mid;
        } else {
            right = mid - 1;
        }
    }
    return left;
}
```

---

# 57. API Call Problem ⭐

```cpp
// Using libcurl in C++
#include <curl/curl.h>
#include <nlohmann/json.hpp>

size_t WriteCallback(void* contents, size_t size, size_t nmemb, string* userp) {
    userp->append((char*)contents, size * nmemb);
    return size * nmemb;
}

nlohmann::json fetchAndParse(const string& url) {
    CURL* curl = curl_easy_init();
    string response;
    
    if (curl) {
        curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, &response);
        
        CURLcode res = curl_easy_perform(curl);
        curl_easy_cleanup(curl);
        
        if (res != CURLE_OK) {
            throw runtime_error("Request failed");
        }
    }
    
    return nlohmann::json::parse(response);
}
```

---

# 🎯 Quick Revision Checklist

## Must-Know Complexities
| Operation | Time |
|-----------|------|
| `unordered_map` access | O(1) avg |
| `map` access | O(log n) |
| `priority_queue` push/pop | O(log n) |
| `sort()` | O(n log n) |
| Binary Search | O(log n) |

## C++ STL Cheat Sheet
```cpp
// Priority Queue (Max Heap)
priority_queue<int> maxHeap;

// Min Heap
priority_queue<int, vector<int>, greater<int>> minHeap;

// Custom comparator
priority_queue<pair<int,int>, vector<pair<int,int>>, 
    function<bool(pair<int,int>&, pair<int,int>&)>> pq(
    [](auto& a, auto& b) { return a.first > b.first; }
);

// Lambda sort
sort(v.begin(), v.end(), [](auto& a, auto& b) {
    return a.second < b.second;
});
```

---

**Good luck with your interview! 🚀**
