package snitch.prometheus;

import snitch.utils.HttpUtils;

import java.io.IOException;

public class QueryBean {

    private String queryValue;

    public QueryBean(String queryValue) {
        this.queryValue = queryValue;
    }

    public String getQueryValue() {
        return queryValue;
    }


    @Override
    public String toString() {
        return "Query: "+queryValue+'\n';
    }


    public void setQueryValue(String queryValue) {
        this.queryValue = queryValue;
    }

    public String execQuery(String token) throws IOException {

        return HttpUtils.sendGET("https://prometheus-k8s-openshift-monitoring.apps.elclown.lab.local/api/v1/query?query="+this.getQueryValue(), token);
    }
}
