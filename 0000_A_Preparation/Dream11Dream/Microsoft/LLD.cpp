#include <bits/stdc++.h>
using namespace std;

/*
LRU
cap
get(K);
put(k, v, ttl);

cap=3
(3,5)
(4,5)
get(3,5) --> 5
(1,2)
(4,7)
get(7, 11) --> -1
(8,9)

head                                          tail
(-1, -1) --> (4,7)->(1,2)-->(8,9)-->->(1,2)-->-->-->(-1, -1)
Node *addNode
temp=(8, 9)
head->next=addNode;
temp->prev=addNode;
addNode->next=temp;
addNode->prev=head


addNode(k, v);
deleteNode(Node* nodeVal);
get(k) {
//present in map or not
delete from mp; --> O(1)
delete from linkedList --> O(1)
addNode --> O(1)
add in map --> O(1)
}
put(k, v) {
addNode();
make enytry in hashMap
}
Put(k,v) -> O(1)
get--> O(1)
*/



class LRU {
    public:
    class Node {
        public:
        int key, val, ttl;
        Node *prev, *next;
        Node(int KEY, int VAL, int TTL) {
            key=KEY;
            val=VAL;
            ttl=TTL;
            prev=NULL;
            next=NULL;
        }
    };
    unordered_map<int, Node*> mp;
    unordered_map<int, vector<Node*>> ttlMap;
    Node *head=new Node(-1, -1, 100);
    Node *tail=new Node(-1, -1, 100);
    int cap;
    LRU(int capacity) {
        head->next=tail;
        tail->prev=head;
        cap=capacity;
    }
    // O(1)
    void deleteNode(Node *delNode) {
        Node *deletePrev=delNode->prev;
        Node *deleteNext=delNode->next;
        deletePrev->next=deleteNext;
        deleteNext->prev=deletePrev;
    }

    void addNode(Node *addNode) {
        Node *temp=head->next;
        head->next=addNode;
        temp->prev=addNode;
        addNode->next=temp;
        addNode->prev=head;
    }
    //O(1)
    int get(int key) {
        if(mp.find(key) != mp.end()) {
            Node* resNode=mp[key];
            int res=resNode->val;
            // delete it from the older order
            // move this to latest one
            mp.erase(key);
            deleteNode(resNode);
            // ttl validation
            addNode(resNode);
            mp[key]=resNode;
            return res;
        }
        return -1;
    }
    //O(1)
    void put(int key, int val, int ttl) {
        // already exist or not in out Data structure
        if(mp.find(key) != mp.end()) {
            Node *existingNode=mp[key];
            mp.erase(key);
            deleteNode(existingNode);
        }
        //capacity exceeded
        if(mp.size()==cap) {
            Node *OlderNode=tail->prev;
            int olderKey=OlderNode->key;
            mp.erase(olderKey);
            deleteNode(OlderNode);
        }
        Node *newNode=new Node(key, val, ttl);
        addNode(newNode);
        mp[key] = newNode;
    }
    //O(cap)
    void checkTTL(int ttl) {
        vector<Node*> listNode=ttlMap[ttl];
        for(auto node: listNode) {
            Node *existingNode=node;
            int key=existingNode->key;
            mp.erase(key);
            deleteNode(existingNode);
        }
    }
};



