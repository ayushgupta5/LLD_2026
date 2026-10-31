
#include <bits/stdc++.h>
using namespace std;

/*

cab abc bca


abc bca cab
abc+bca=abca
abca + cab --> abcab

a=abc b=bca
1 c. b
2 bc bc --> maxIndex=2
3 abc bca

ans = a+b.substr(maxIndex)
        abc+a--> abca

a=abca b=cab
abca+b=abcab
for(int i=1;i<=min())

0 1 2
0 2 1
1 2 0
1 0 2
2 1 0
2 0 1

bc
ca
ab

abcab

abc bca cab
arr={a, b, c}
sort(arr)
arr={b a c}

abc + bca --> abca

abca + cab -->abcab



*/

string mergeString(string &a, string &b) {
    int lena=a.size(), lenb=b.size();
    int len = min(lena, lenb), maxIndex;
    for(int i=1;i<=len;i++) {
        if(a.substr(a.size()-i) == b.substr(0, i)) {
            maxIndex=i;
        }
    }
    return a+b.substr(maxIndex);
}

string helper(string &a, string &b, string &c) {
    string temp=mergeString(a, b);
    temp=mergeString(temp, c);
    return temp;
}
string solve(string &a, string &b, string &c) {
    string ans=helper(a, b, c);
    if(ans > helper(a, c, b)) {
        ans=helper(a,c,b);
    }
    if(ans > helper(b, a, c)) {
        ans=helper(b, a, c);
    }
    if(ans > helper(b, c, a)) {
        ans=helper(b, c, a);
    }
    if(ans > helper(c, a, b)) {
        ans=helper(c, a, b);
    }
    if(ans > helper(c, b, a)) {
        ans=helper(c, b, a);
    }
    return ans;

}
int main() {
    string a="abc", b="bca", c="cab";
    cout<<solve(a, b, c)<<endl;

    a="abc", b="bca", c="aaa";
    cout<<solve(a, b, c)<<endl;

    a="ab", b="ba", c="aba";
    cout<<solve(a, b, c)<<endl;

}
