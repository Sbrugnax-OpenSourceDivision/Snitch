package snitch.prometheus;

import snitch.utils.HttpUtils;

import java.io.IOException;

public class QueryBean {

    private String queryValue;
    private String id;
    private String queryName;

    public QueryBean(String queryName,String id, String queryValue) {
        this.queryName = queryName;
        this.queryValue = queryValue;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQueryValue() {
        return queryValue;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    @Override
    public String toString() {
        return "Query: "+queryValue+'\n';
    }


    public void setQueryValue(String queryValue) {
        this.queryValue = queryValue;
    }

    public String execQuery(String token) throws IOException {
        // TODO Parametrizzare url di prometheus
        return HttpUtils.sendGET("https://prometheus-k8s-openshift-monitoring.apps.elclown.lab.local/api/v1/query?query="+this.getQueryValue(), token);
    }
}
