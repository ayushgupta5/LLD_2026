#include <iostream>
#include <string>
#include <cassert>

using namespace std;

string sumBaseB(string a, string b, int base) {
    string result = "";
    int carry = 0;
    int i = a.length() - 1, j = b.length() - 1;
    
    while (i >= 0 || j >= 0 || carry) {
        int sum = carry;
        if (i >= 0) sum += (a[i--] - '0');
        if (j >= 0) sum += (b[j--] - '0');
        
        result = char('0' + sum % base) + result;
        carry = sum / base;
    }
    
    return result;
}

string convertBase2To6(string binary) {
    string ans = "0";
    
    for (char bit : binary) {
        // ans = ans * 2 (in base 6)
        ans = sumBaseB(ans, ans, 6);
        
        // Add current bit (0 or 1) in base 6
        if (bit == '1') {
            ans = sumBaseB(ans, "1", 6);
        }
    }
    
    return ans;
}

int main() {
    // Basic tests for convertBase2To6
    struct Test { string bin; string expected; } tests[] = {
        // {"0", "0"},
        // {"1", "1"},
        // {"10", "2"},      // 2 -> base6: 2
        // {"1010", "14"},   // 10 -> base6: 14
        // {"1111", "23"},   // 15 -> base6: 23
        {"100110", "102"} // 38 -> base6: 102
    };

    for (const auto &t : tests) {
        string out = convertBase2To6(t.bin);
        cout << "binary=" << t.bin << " -> base6=" << out << '\n';
        assert(out == t.expected);
    }

    cout << "All tests passed.\n";
    return 0;
}