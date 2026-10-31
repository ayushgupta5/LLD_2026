Given the root of a binary tree, find the Lowest common ancestor between two given nodes

root = [3, 5, 1, 6, 2, 0, 8, null, null, 7, 4]
node1=5, node2=1

        3
      5    1
   6   2  0   8
      7  4


root=3
check wherether node1 or node2 is there or not
if yes we will return root;

TreeNode *solve(TreeNode *root, TreeNode *node1, TreeNode *node2) {
	if (root== NULL || root->val == node1->val || root->val == node2->val) return root;
	TreeNode *leftVal = solve(TreeNode->left, node1, node2);
	TreeNode *rightVal = solve(TreeNode->right, node1, node2);

	if(leftVal != NULL) return leftVal;
	if(rightVal != NULL) return rightVal;
	return root;
}

root=3, node1=6, node2=8 (left)

root=5, node1=6, node2=8 (left=6, right=NULL)

root=2, node1=6, node2=8 (left=NULL, right=NULL)

root=4, node1=6, node2=8 (left=NULL, right=NULL)



root=3, node1=6, node2=8 (right)

root=1, node1=6, node2=8 (left=NULL, right=No Null)

root=8, node1=6, node2=8 (left=NULL, right=Not Null)

TC : O(n)
SC : O(Height Of Tree)


------------------------------------------------------------------------------------------------------------------------------
Given an array containing both positive and negative integers, find the length of the longest subarray with the sum of all elements equal to zero.
arr[n] {-ve, +ve}

{6, -2, 2, -8, 1, 7, 4, -10}
Length of longest subarray is 8



a, b, c, d, e, f, g, h i, j, k, l

   x                 x
      y                      y
 z           z

int solve(vector<int> &arr) {
	int sum=0;
	unordered_map<int, int> mp;
	mp[0]=-1;
	for(int i=0;i<arr.size();i++) {
		sum += arr[i];
		// check if this sum came before in the for loop iteration
		if(mp.find(sum) != mp.end())  {
			int currIndex=i;
			int prevIndex=mp[sum];
			int value = currIndex-prevIndex;
			ans=max(ans, value);
		}
		else mp[sum]=i;
	}
	return ans;
}

TC - O(n)
SC - O(n)
n= length of the array

DRY Run:-
 0   1  2  3   4  5  6   7
{6, -2, 2, -8, 1, 7, 4, -10}

0  6  sum = 6 mp[6, 0]
1  -2 sum = 4 mp[(6, 0)(4, 1)]
2  2  sum = 6 mp[(6, 0) (4, 1)] ans=2
3  -8 sum = -2 mp[(6, 0) (4, 1), (-2, 3)] ans=2
4   1 sum = -1 mp[(6, 0) (4, 1), (-2, 3), (-1, 4)] ans=2
5   7 sum = 6 mp[(6, 0) (4, 1), (-6, 3), (-5, 4), (2, 5)] ans=5
6   4 sum = 10 mp[(6, 0) (4, 1), (-6, 3), (-5, 4), (2, 5), (10, 6)] ans=5
7  -10 sum = 0 mp[(6, 0) (4, 1), (-6, 3), (-5, 4), (2, 5), (10, 6), (0, 7), (0, -1)] ans=7-(-1) =8


[0,1,-1]


0 0 sum = 0 mp{(0, -1)} ans=0-(-1)=1
1 1 sum = 1 mp[1, 1] ans=1
2 -1 sum = 0 ans=max(ans, 3)=3

[2, -2, 2, -2]

mp[(0, -1)]
0 2  sum = 2 mp[(0, -1), (2, 0)] ans=0
1 -2 sum = 0 mp[(0, -1), (2, 0)] ans=2
2 2 sum = 2 mp[(0, -1), (2, 0)] ans=2
3 -2 sum 0 mp[(0, -1), (2, 0)] ans=3-(-1)=4
































































