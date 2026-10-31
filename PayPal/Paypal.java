
/*
given array is unsorted
sort it
List:={1 2 3 4 5 6}
ans={1 3 5 6 4 2}
1. sort the array
2.
n=len of array
ans(n)

index=0;
sort(arr.begin(), arr.end()
for(i=0;i<len;i++) {
    if(arr[i]&1) { // odd
        ans[index++]=arr[i];
    }
}
int evenIndexstart=index
for(i=0;i<len;i++) {
    if(arr[i]%2==0) {//even
       ans[index++]=arr[i];
    }
}
reverse(arr.begin()+evenIndexstart, arr.end())


campaing --> lot of people coming
how many visitor came till now

Table --> FreqTable

campainID
CountFreq
CreatedAT
updatedAT
createdBy
updatedBy
campingName


consistency --> strong +

client --> lb--<> ApI GW --> ingestion SVC --> Master DB --> slaveDB

Write:-
update FreqTable
set CountFreq = (select CountFreq where campainID={campainID}) + 1
where campainID={campainID}


write Path
client --> lb--> ApI GW --> ingestion SVC --> queue --> coreSVC --> master DB --> SlaveDB

master DB is down (SPOF) --> DB Replication


read Path
client --> lb--> ApI GW --> ingestion SVC <--> Redis (TTL= 1 Min) <--> slave DB1+slaveDB2 + slaveDB3

*/

public class Paypal {
}
