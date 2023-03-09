package snitch.prometheus.beans;

import java.util.ArrayList;

public class QueryResult {

    public QueryBean.Query query;
    public String podName;
    public ArrayList<Float> values;
    public ArrayList<Long> timestamps;

    public QueryResult(QueryBean.Query query, String podName, ArrayList<Float> values, ArrayList<Long> timestamps) {
        this.query = query;
        this.podName = podName;
        this.values = values;
        this.timestamps = timestamps;
    }
}
