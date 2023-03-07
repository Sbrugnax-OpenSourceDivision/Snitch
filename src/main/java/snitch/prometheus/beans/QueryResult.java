package snitch.prometheus.beans;

import java.util.ArrayList;

public class QueryResult {

    public QueryBean.Query query;
    public String podName;
    public ArrayList<Float> values;
    public ArrayList<Float> timestamps;

    public QueryResult(QueryBean.Query query, String podName, ArrayList<Float> values, ArrayList<Float> timestamps) {
        this.query = query;
        this.podName = podName;
        this.values = values;
        this.timestamps = timestamps;
    }
}
