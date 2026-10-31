/*
( + { + [
(){}[]
(((({}{}{}{}))))

[]

we need to figure out that brackets are valid or not??

(((({}}{{}{}{}))))

{[()]} --> Valid

{[(])} --> Not Valid

((({{}})))

{[(])}

(({{}}))) --> False --> st.empty()==true --> return false

((({{}})) --> false

stack --> ( return st.empty()==true

*/


#include <bits/stdc++.h>
using namespace std;
bool validParanthesis(string &str) {
    stack<char> st;
    for(auto ch: str) {
        if(ch=='(' || ch=='{' || ch=='[') {
            st.push(ch);
        }
        else {
            if(st.empty() == true) {
                return false;
            }
            else if((ch=='}' and st.top() != '{') || (ch==']' and st.top() != '[') || (ch==')' and st.top() != '(')) {
                return false;
            }
            st.pop();
        }
    }
    return st.empty()==true;
}



/*
'', "", " ' /{{}}/ ' "

unordered_map<char, char> mp;
mp['(']=')'
mp['{']='}'
mp['(']=')'
mp[']=''';
mp['/']='\'

int main()
{
    string str="{[()]}";
    cout<<validParanthesis(str)<<endl;
}
*/




// TC - O(n)
// SC - O(n) --> ((((((((((( --> Insert all the open brackets into stack thats why in worst case SC - O(n) : n: len of string
//public class MS_2026 {
//}



/*
{
    "abc": "abc",
    "bcd": {
        "a": "a"
        }

}
}
{{} --> we need one more }
st--> { --> u need to have one closeing bracket as well

{{}}}

st --> since stack is empty and we have not iterate all the json body --> it Means we are missing any opening paraenthesis

Invalid
open > close
open<close

open=0, close=0;
if ch=='{' --> open++;
else if ch=='}' --> close++;


{{{ --> n=3

solve('{', 1, 0, n);


*/

void solve(string curr, int open, int close, int n, vector<string> &ans) {
    if(open+close == 2*n && open == close) {
        // print
        // store in ds
        ans.push_back(curr);
    }

    if(open < close) {
        solve(curr+'{', open+1, close, n);
    }
    if(close < open) {
        solve(curr+'}', open, close+1, n);
    }
}


1. prompt
2.
Role:  You are a senior tech acrchitechture
Context:  read
Result:


Kafka:-

user --> 500

pagination way
        page number-28
batchsize=20

row=28*20 --> 28*20+20 (560-580)

cache --> TTL value 6 hours