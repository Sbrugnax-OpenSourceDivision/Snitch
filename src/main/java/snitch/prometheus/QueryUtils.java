package snitch.prometheus;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import snitch.prometheus.beans.QueryBean;
import snitch.prometheus.beans.QueryResult;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class QueryUtils {

    public static HashMap<String, ArrayList<QueryResult>> getResultFromJson(QueryBean queryBean) {

        HashMap<String, ArrayList<QueryResult>> result = new HashMap<>();

        try{

            for(QueryBean.Query q: queryBean.getQueryList()){

                ArrayList<QueryResult> element = new ArrayList<>();
                JsonObject jobj = new Gson().fromJson(
                        new FileReader("tmp/" + queryBean.getId() + "/" + q.id + ".json"), JsonObject.class);

                for(JsonElement query:jobj.getAsJsonObject("data").getAsJsonArray("result")){

                    String name = query.getAsJsonObject().getAsJsonObject("metric")
                            .get("pod")
                            .getAsString();

                    JsonArray tmp;
                    if(q.type.equals(QueryBean.Type.table)){
                        tmp = query.getAsJsonObject()
                                .getAsJsonArray("value");
                    }
                    else{
                        tmp = query.getAsJsonObject()
                                .getAsJsonArray("values");
                    }

                    ArrayList<Long> timestamps = new ArrayList<>();
                    ArrayList<Float> values = new ArrayList<>();

                    if(q.type.equals(QueryBean.Type.table)){
                        timestamps.add(tmp.get(0)
                                .getAsLong());
                        values.add(tmp.get(1)
                                .getAsFloat());
                    }
                    else{
                        for(JsonElement entry:tmp){

                            timestamps.add(entry.getAsJsonArray().get(0)
                                    .getAsLong());
                            values.add(entry.getAsJsonArray().get(1)
                                    .getAsFloat());
                        }
                    }

                    element.add(new QueryResult(q, name, values, timestamps));
                }

                result.put(queryBean.getId() + " - " + q.id, element);
            }

        }
        catch (FileNotFoundException e){
            System.out.println("File non trovato");
        }

        return result;
    }

    public static ArrayList<Integer> getResultFromJsonData(String jsonData) {

        ArrayList<Integer> result = new ArrayList<>();

        JsonObject jobj = new Gson().fromJson(jsonData, JsonObject.class);

        for(JsonElement query:jobj.getAsJsonObject("data").getAsJsonArray("result")){

            JsonArray tmp = query.getAsJsonObject().getAsJsonArray("value");
            result.add(tmp.get(1).getAsInt());
        }

        return result;
    }


}
