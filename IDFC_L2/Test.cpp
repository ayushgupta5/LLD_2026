
/*
read the file -->
"My Name is Ayush Gupta. I am from Varanasi" --> 2

"My Name is Ayush Gupta." --> 1

"My Name is Ayush Gupta.\n
I am from Varanasi" --> 2
*/

int solve() {
    string str;
    getline(cin, str); // taking string str as an input
    int group=1, sizeOfStr = str.size(); // intialize cnt variable as 1 and size of str
    for(int i=0;i<sizeOfStr;i++) { // iterate the whole string
        if(str[i]=='.' and str[i+1] == ' ') { // checking with combo ('.' & ' ')
            group++;
        }

        if(str[i]=='\' and str[i+1] == 'n') { // checking with combo ('\' & 'n')
            group++;
        }
    }
    return group;
}
// TC - O(n) : SC - O(1)

string text = parseFile(fileObject);


// pattern (strategy Pattern)


// Payment Gateway --> UPI, CASH, CREDIT Card
// Strategy : simpleText, PDF, DOC, JSON

interface ReadStrategy {
    void parseFile(fileObject);
}
class simpleText implements ReadStrategy {
    void parseFile(fileObject) {
        // Implementation for Simple text
        for(int i=0;i<sizeOfStr;i++) { // iterate the whole string
                if(str[i]=='.' and str[i+1] == ' ') { // checking with combo ('.' & ' ')
                    group++;
                }

                if(str[i]=='\' and str[i+1] == 'n') { // checking with combo ('\' & 'n')
                    group++;
                }
            }
            return group;
    }
}

class PDF implements ReadStrategy {
    void parseFile(fileObject) {
        // Implementation for PDF text
        cout<<"Parsing the object for the PDF"<<endl;
    }
}
class DOC implements ReadStrategy {
    void parseFile(fileObject) {
        // Implementation for DOC text
        cout<<"Parsing the object for the DOC"<<endl;
    }
}
class JSON implements ReadStrategy {
    void parseFile(fileObject) {
        // Implementation for JSON text
        cout<<"Parsing the object for the JSON"<<endl;
    }
}
class readFileProcessor {
    private ReadStrategy readStrategy;
    void setReadStrategy(ReadStrategy readStrategy) {
        this.readStrategy = readStrategy // text, PDF, DOC etc etc
    }
    void parseFile(fileObject) {
        readStrategy.parseFile(fileObject);
    }
}


upload API

FE --> upload file (KBs, MBs, GBs)

client(FE) --> API GW(Rate Limiter) --> LB --> upload_service --> s3 bucket
                                                1. Validation
                                                2. create a session for upload
                                                3. generate the URL for that file
                                                4. DB --> MySQL (id, fileName, URL, metadata(json), created_at, updated_at, created_by, updated_by)

API
POST/uploadfile

1. validatation
2. upload and generate the URL
3. prepare the metadata and  store in DB (MYSQL)
4. success response


client(FE) --> API GW(Rate Limiter) --> LB --> batchify_Service --> queue --> upload_service --> s3 bucket(actual raw file) --> DB (metadata)
                                                Batch (100-200)
                                                configuarble


Microservice -->
Booking Microservice
Payment Microservice


2 Phase commit
3 Phase Commit

SAGA (ChorioGraphy and Orchestartor (Brain))

Booking -->queue --> Driver -->queue--> Payment

Orchestartor (Brain)

exp_service -->queue-->Booking
            -->queue-->Driver
            -->queue-->Payment

