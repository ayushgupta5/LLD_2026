/*
Given an array of integers,
return the maximum sum for a non-empty subarray (contiguous elements)
with at most one element deletion. In other words,
you want to choose a subarray and optionally
delete one element from it so that there is still at least one element
left and the sum of the remaining elements is maximum possible.
       0 1 2 3
arr = [1,-2,0,3]
ans = 4

miniElemntIndex==1
int solve(vector<int> &arr) {
    int i, n=arr.size(), miniElemntIndex=-1, mini=1e9;
    for(i=0;i<n;i++) {
        if(mini>arr[i]) {
            mini=arr[i];
            miniElemntIndex=i;
        }
    }
    // Kadane's Algo
    for(int i=0;i<n;i++) {
        if(i == miniElemntIndex) continue;
        // kadane's Logic here
    }

}
int kadane(arr, n) {
    for(int i=0;i<n;i++) {
        if(i == miniElemntIndex) continue;
        // kadane's Logic here
    }
}

// Approach2:

   0 1 2 3 4 5
// 1 1 -10 2 2 arr
// 1 2 -8, 2 4 noDeletePrefix
// 0 1  2 4 6

*/
#include <bits/stdc++.h>
using namespace std;

int solve(vector<int> &arr) {
    int i, n=arr.size(), ans = -1e9;
    vector<int> noDeletePrefix(n), oneDeletePrefix(n);
    noDeletePrefix[0] = arr[0],
    oneDeletePrefix[0] = 0;
    for(int i=1;i<n;i++) {
        noDeletePrefix[i] = max(arr[i], noDeletePrefix[i-1] + arr[i]);
        oneDeletePrefix[i] = max(oneDeletePrefix[i-1] + arr[i], noDeletePrefix[i-1]);
        ans=max({ans, noDeletePrefix[i], oneDeletePrefix[i]});
    }
    return ans;
}
int main()
{
    vector<int> arr={1,1,-10,2,2};
    vector<int> arr2={1, -2, 0, 3};
    cout<<solve(arr)<<endl;
    cout<<solve(arr2)<<endl;
    return 0;
}
// TC - O(n)
// SC - O(n)


--------------------------------------------------------------------------------------------------------
/******************************************************************************
Given an array of non-negative integers, A,
of length N, you are initially positioned at the first index of the array.

Each element in the array represents your maximum jump length at that position.

Return the minimum number of jumps required to reach the last index.
 0  1  2  3  4
[2, 3, 1, 1, 4] ans=2;
[2, 1, 1] ans = 1

0 (2)--> 1(3) 2(1) --> 1(3) --> 4(4) ans=2 (Path 1)
// Path(2)
2(1) --> 3(1) ans = 2
3(1) --> 4(4) ans = 3


*******************************************************************************/
// 0 1 2 3 4
// 2 3 1 1 4
#include <bits/stdc++.h>
using namespace std;

int solve(vector<int> &arr) {
    int i, n=arr.size();
    if(arr[0]==0) {
        return -1;
    }
    int maxR=arr[0], jump=1, step=arr[0];

    for(i=1;i<n;i++) {
        if(i==n-1) {
            return jump;
        }
        maxR=max(maxR, i+arr[i]);
        step--;
        if(step==0) {
            jump++;
            // validation where if currIndex crossing the maxReach value
            if(i>=maxR) {
                return -1;
            }
            step=maxR-i;
        }

    }
    return -1;
}
// TC - O(n);
// SC - O(1)


int main()
{
    vector<int> arr={2,3,1,1,4}; // 2
     vector<int> arr2={2,1,1}; // 1
     vector<int> arr3={3, 4, 1, 1, 1}; // 2
     vector<int> arr4={1, 0, 2, 3, 4}; // -1

/*
0  1  2  3  4
1, 0, 2, 3, 4
1. 1. 4. 6. 8

*/


    cout<<solve(arr)<<endl;
    cout<<solve(arr2)<<endl;
    cout<<solve(arr3)<<endl;
    cout<<solve(arr4)<<endl;

    return 0;
}

// TC - O(len * steps);
// SC - O(len);

/******************************************************************************
Given an array of non-negative integers, A,
of length N, you are initially positioned at the first index of the array.

Each element in the array represents your maximum jump length at that position.

Return the minimum number of jumps required to reach the last index.
 0  1  2  3  4
[2, 3, 1, 1, 4] ans=2;
[2, 1, 1] ans = 1

0 (2)--> 1(3) 2(1) --> 1(3) --> 4(4) ans=2 (Path 1)
// Path(2)
2(1) --> 3(1) ans = 2
3(1) --> 4(4) ans = 3


*******************************************************************************/

#include <bits/stdc++.h>
using namespace std;

int helper(int index, int n, vector<int> &arr, vector<int> &dp) {
    if(index >= n) {
        return INT_MIN;
    }
    if(index == n-1) {
        return 0;
    }
    if(dp[index] != -1) {
        return dp[index];
    }
    int steps=arr[index];
    int ans=INT_MAX;
    for(int step=1;step<=steps;step++) {
        int val = 1 + helper(index+step, n, arr, dp);
        ans =min(ans, val);
    }
    return dp[index] = ans;
}
int solve(vector<int> &arr)  {
    int n=arr.size();
    vector<int> dp(n, -1);
    return helper(0, n, arr, dp);
}

int main()
{
    vector<int> arr={2,3,1,1,4};
     vector<int> arr2={2,1,1};
    cout<<solve(arr)<<endl;
    cout<<solve(arr2)<<endl;

    return 0;
}

// TC - O(len * steps);
// SC - O(len)