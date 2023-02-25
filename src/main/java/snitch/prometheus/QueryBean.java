package snitch.prometheus;

import snitch.utils.HttpUtils;
import snitch.utils.QueryUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
        public String id;

        public Query(String id, String value, Type type) {
            this.id = id;
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

    public void getQueryData(String token, String url) throws IOException {

        BufferedWriter file = null;

        File dir = new File("tmp/" + this.getId());

        if (!dir.exists()) {
            dir.mkdir();
        }

        for(Query q: this.queryList){
            file  = new BufferedWriter(new FileWriter("tmp/" + this.getId() + "/" + q.id + ".json", false));
            file.write(
                    HttpUtils.sendGET(url + "/api/v1/query?query="+q.value, token)
            );
            file.close();
        }

    }

    public boolean isTriggered(String token, String url) throws IOException {

        String jsonData = HttpUtils.sendGET(url + "/api/v1/query?query="+this.getTriggerQuery(), token);

        ArrayList<Integer> tmp = QueryUtils.getResultFromJsonData(jsonData);

        for(int q : tmp){
            if(q == 0)
                return true;
        }

        return false;
    }
}
