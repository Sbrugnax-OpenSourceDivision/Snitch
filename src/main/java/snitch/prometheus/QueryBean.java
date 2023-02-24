package snitch.prometheus;

import snitch.utils.HttpUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QueryBean {
    public enum Type{
        plot,
        table
    }

    public static class Query{
        public String value;
        public Type type;

        public Query(String value, Type type) {
            this.value = value;
            this.type = type;
        }
    }

    private ArrayList<Query> queryList;

    public QueryBean(ArrayList<Query> queryList, String id, String queryName, String triggerQuery) {
        this.queryList = queryList;
        this.id = id;
        this.queryName = queryName;
        this.triggerQuery = triggerQuery;
    }

    private String id;
    private String queryName;
    private String triggerQuery;

    public String getTriggerQuery() {
        return triggerQuery;
    }

    public void setTriggerQuery(String triggerQuery) {
        this.triggerQuery = triggerQuery;
    }

    public QueryBean(String queryName, String id, ArrayList<Query> queryList) {
        this.queryName = queryName;
        this.queryList = queryList;
        this.id = id;
    }

    public ArrayList<Query> getQueryList() {
        return queryList;
    }

    public void setQueryList(ArrayList<Query> queryList) {
        this.queryList = queryList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String execQuery(String token, String url) throws IOException {
        // TODO Refactor completo metodo di esecuzione query
        return HttpUtils.sendGET(url + "/api/v1/query?query="+this.getQueryList(), token);
    }
}
