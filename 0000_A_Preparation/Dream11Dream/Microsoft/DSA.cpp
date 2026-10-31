#include <bits/stdc++.h>
using namespace std;

/*
0 0 0 0 0 0
1 0 0 1 0 0
0 0 1 1 1 0
0 0 0 1 1 1
0 0 1 0 0 0

(1, 0)
(1,3)
(2,2)
(2,3)
(2,4)
(3,3)
(3,4)
(3,5)
(4,2)


no of island = 3

0 0 0 0 0 0
1 0 0 0 0 0
0 0 0 1 0 0
0 0 0 0 0 0
0 0 0 0 0 0

*/

bool isValid(int i, int j, int row, int col, vector<vector<int>> &grid, vector<vector<bool>> &vis) {
    if(i<0 || i>=row || j<0 || j>=col || grid[i][j]==0  || vis[i][j] == true) {
        return false;
    }
    return true;
}

void dfs(int i, int j, int row, int col, vector<vector<int>> &grid, vector<vector<bool>> &vis) {
    if(isValid(i, j, row, col, grid, vis) == false) {
        return;
    }
    // make visited array as visited
    vis[i][j]=true;
    //up
    dfs(i-1, j, row, col, grid, vis);
    //down
    dfs(i+1, j, row, col, grid, vis);
    //left
    dfs(i, j-1, row, col, grid, vis);
    // right
    dfs(i, j+1, row, col, grid, vis);
}

int solve(vector<vector<int>> &grid) {
    int row=grid.size(), col=grid[0].size(), ans=0;
    vector<vector<bool>> vis(row, vector<bool> (col, 0));
    for(int i=0;i<row;i++) {
        for(int j=0;j<col;j++) {
            if(grid[i][j] == 1 and vis[i][j]==false) {
                dfs(i, j, row, col, grid, vis);
                ans++; //1
            }
        }
    }
    return ans;
}

int main() {


    vector<vector<int>> grid(5, vector<int> (6, 0));

    for(auto row: grid) {
        for(auto col: row) {
            cout<<col<<" ";
        }
        cout<<endl;
    }
    /*
    (1, 0)
(1,3)
(2,2)
(2,3)
(2,4)
(3,3)
(3,4)
(3,5)
(4,2)
    */
    grid[1][0]=1;
    grid[1][3]=1;
    grid[2][2]=1;
    grid[2][3]=1;
    grid[2][4]=1;
    grid[3][3]=1;
    grid[3][4]=1;
    grid[3][5]=1;
    grid[4][2]=1;

    cout<<endl;

    for(auto row: grid) {
        for(auto col: row) {
            cout<<col<<" ";
        }
        cout<<endl;
    }

    cout<<solve(grid)<<endl;

}
