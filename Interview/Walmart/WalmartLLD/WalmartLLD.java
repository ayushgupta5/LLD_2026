/*
#Functional Requirement:-
Design a time-based KV store

 cap = 3
 put(k, v, time)
 put(1, 1, 10) --> (1,1,10)
 put(3, 3, 12) --> (3,3,12)-->(1,1,10)
 put(4, 4, 15) --> (4,4,15)--> (3,3,10)-->(1,1,10)
 put(1, 1, 20) --> (1,1,20)-->(4,4,15)--> (3,3,10)--> (1,1,10)

 get(k, v)
 get(1, 1)--> (1,1,10)--> (1,1,20)

 class Node{
 public:
 int key, val;
 Node *prev, *next, *child;
 Node(int newKey, int newVal) {

 }

 }
 Put(k, v, time) --> O(1)
 map<int, Node*> mp;
 mp[key]=node;

 get(key) --> O(1)
 map<int, vector<pair<int, int>> mp;
 mp[key1]=() --> array[] -> (val1, t1)--> (val2, t2) --> (val3, t3) --> Return array[array.size()-1] --> O(1)

 gey(key, time) --> O(Log(n))

  mp[key]=() --> array[] -> (val1, t1)--> (val2, t2) --> (val3, t3) --> Return array[array.size()-1] --> O(1)

 // O(n) --> O(Log(array_Len))
 for(auto element: array) {
    int val=element[0];
    int timestapmp=element[1];
    if(timeStamp == time) {
        return val;
    }
 }

 int binarySearch(vector<pair<int, int>> &arr) {
    int lo=0, hi=arr.size()-1;
    while(lo<=hi) {
        int mid=(lo+hi)>>1;

    }
 }


 put() | set() --> O(1)
 put(key, value, time)
 mp[key].push_back({value, time});

 (key) --> (val1, t1)--> (val2, t2) --> (val3, t3)

 (4,4,15)<-->(3,3,12)<-->(1,1,10)
                            |
                         (1,1,20)
                                                     |
                         (1,1,30)
                                                     |
                         (1,1,40)



Real Life Product
MySql DB
Indexing at TimeStamp Value
Read Replica with Master DB
Write Operation will happen on Master DB and Read Operation will happen on Sl;ave (Replica) DB
More Trade Off: Redis Cache

WE can Use Redis Lock --> we can avoid the Race Condition

clint --> LB --> IngestionService --> Queue --> CoreService --> DB(Put, Get) (Write Operation will happen on Master DB)
                                                            --> DB(Put, Get)  (Read Operation will happen on Slave DB)



*/
public class WalmartLLD {
}
