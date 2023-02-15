package snitch.prometheus;

import java.util.ArrayList;

public class QueryResult {

    public String podName;
    public ArrayList<Float> values;
    public ArrayList<Float> timestamps;

    public QueryResult(String podName, ArrayList<Float> values, ArrayList<Float> timestamps) {
        this.podName = podName;
        this.values = values;
        this.timestamps = timestamps;
    }
}
