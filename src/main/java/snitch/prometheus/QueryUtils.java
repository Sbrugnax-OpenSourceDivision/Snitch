package snitch.prometheus;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import snitch.prometheus.beans.QueryResult;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class QueryUtils {

    // TODO getResultFromJson deve prendere in input il querybean associato
    public static ArrayList<QueryResult> getResultFromJson(String filename, ) {

        ArrayList<QueryResult> result = new ArrayList<>();

        try{
            JsonObject jobj = new Gson().fromJson(new FileReader(filename), JsonObject.class);

            parseJson(result, jobj);
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

    private static void parseJson(ArrayList<QueryResult> result, JsonObject jobj) {
        for(JsonElement query:jobj.getAsJsonObject("data").getAsJsonArray("result")){

            String name = query.getAsJsonObject().getAsJsonObject("metric")
                    .get("pod")
                    .getAsString();

            JsonArray tmp = query.getAsJsonObject()
                    .getAsJsonArray("values");

            ArrayList<Float> timestamps = new ArrayList<>();
            ArrayList<Float> values = new ArrayList<>();

            for(JsonElement entry:tmp){

                timestamps.add(entry.getAsJsonArray().get(0)
                        .getAsFloat());
                values.add(entry.getAsJsonArray().get(1)
                        .getAsFloat());
            }

            result.add(new QueryResult(name, values, timestamps));
        }
    }

}
