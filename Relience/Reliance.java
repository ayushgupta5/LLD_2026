/*
Your distributed tracing samples 1% of requests (uniform sampling). For 1M req/sec, you have 10K traces/sec --
manageable.

Recently, a customer reported intermittent slowness. Engineering tried to find traces of the slow requests:
none. The slow requests were rare (~1 per 10K); sampling missed them.

You realize: the most interesting requests (slow, error) are the rarest; the sampling missed them. Investigation
required reproducing the issue with manual tracing.

Uniform sampling misses outliers. Better: sample slow / failed / interesting more heavily.

Design the sampling.

cate1: select query

fail1_bucket:-(e3, e6, e8)

fail2_bucket:-(e23, e45, e67)

fail_event_type1='drawdown_failure'

fail_event_type2='repayment_failure'

fail_event_type3='waive_off_failure'

success_event_1
success_event_2
success_event_3

we can put 50% these failure logs in s3 and we will capture the success logs in as random


client --> API_Gate_Way --> service --> queue --> hierchy_Service_Maintainer(sampling service) --> cassandra (NoSQL) -->Data Lake (Audit Purpose) +
Grafana Metrics

*/

// Strategy Pattern
class sample {
    int sampleID;
    String event;

}
// Strategy Pattern for Sampling based on events (Event1, Event2...)
interface SamplingStrategy {
    void sampling(String events);
}
class Event1 implements SamplingStrategy {

    @Override
    public void sampling(String events) {
        System.out.println(events);
    }
}
class Event2 implements SamplingStrategy {

    @Override
    public void sampling(String events) {
        System.out.println(events);
    }
}
class Event3 implements SamplingStrategy {

    @Override
    public void sampling(String events) {
        System.out.println(events);
    }
}
class SamplingProcessor {
    private SamplingStrategy samplingStrategy;
    void setSamplingStrategy(SamplingStrategy samplingStrategy) {
        this.samplingStrategy = samplingStrategy;
    }
    void sampling (String events) {
        samplingStrategy.sampling(events);
    }
}

public class Reliance {
}
