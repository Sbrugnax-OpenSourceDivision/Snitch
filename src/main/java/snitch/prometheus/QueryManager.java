package snitch.prometheus;

import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Template;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import snitch.configparser.ConfigMapParser;
import snitch.utils.HttpUtils;
import snitch.utils.MailUtils;
import snitch.utils.PdfUtils;
import snitch.utils.QueryUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Path("/query")
@Singleton
public class QueryManager {

    @Inject
    Template query;
    private String response;
    @Inject
    ConfigMapParser configMapParser;
    private ArrayList<QueryBean> queryList;
    @ConfigProperty(name = "openshift.bearertoken")
    String token;
    @ConfigProperty(name = "snitch.query_fetch_interval")
    String fetchInterval;

    @Inject
    Mailer mailer;

    public QueryManager() {
        response = "";
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getPage() {
        response = "";
        return Response.ok(query.data("response", response).render()).build();
    }

    @GET
    @Path("/exec")
    @Produces(MediaType.TEXT_HTML)
    public Response getQuery(@QueryParam("query") String query_value) {

        try {
            String prometheusQuery = HttpUtils.sendGET("https://prometheus-k8s-openshift-monitoring.apps.elclown.lab.local/api/v1/query?query=" + query_value, token);
            if (prometheusQuery.isEmpty()) {
                response = "Help";
            } else {
                response = prometheusQuery;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Response.ok(query.data("response", response).render()).build();
    }

    @Scheduled(every = "1h", delayed = "5s")
    public void buildQueryList() {
        ArrayList<HashMap<String, HashMap<String, String>>> tmp = configMapParser.getQueryList();

        this.queryList = new ArrayList<QueryBean>();

        for (HashMap<String, HashMap<String, String>> query : tmp) {
            String queryValue = URLEncoder.encode(query.get("query").get("value"), StandardCharsets.UTF_8);
            this.queryList.add(new QueryBean(query.get("query").get("name"), query.get("query").get("id"), queryValue));
        }

        System.out.println("Fetch interval: " + fetchInterval + "\n" + this.queryList);
    }

    @Scheduled(every = "${snitch.query_fetch_interval}", delayed = "10s")
    public void fetchAll() throws IOException {
        System.out.println("--FETCHING DATA--");

        try {

            File dir = new File("tmp/");

            if (!dir.exists()) {
                dir.mkdir();
            }

            for (QueryBean queryBean : queryList) {
                BufferedWriter file = new BufferedWriter(new FileWriter("tmp/" + queryBean.getId() + ".json", false));
                file.write(queryBean.execQuery(token));
                file.close();
            }
        } catch (UnknownHostException e) {
            System.out.println("Impossibile connettersi al server Prometheus");
        } catch (Exception e) {
            System.out.println("Esploso il json");
        }

    }

    @GET
    @Path("/mails")
    @Produces(MediaType.TEXT_PLAIN)
    public String sendMails() throws FileNotFoundException {

        List<String> files_json = this.queryList.stream()
                .map(queryBean -> "tmp/" + queryBean.getId() + ".json").toList();

        for (String file : files_json) {
            ArrayList<QueryResult> data = QueryUtils.getResultFromJson(file);
            //build pdf
            PdfUtils.buildPdf(data, new FileOutputStream(file.replace(".json", ".pdf")));
        }

        List<String> files_pdf = files_json.stream().map(
                file -> file.replace(".json", ".pdf")).toList();

        mailer.send(MailUtils.buildMailComposed(files_pdf
                , configMapParser.getTargetMails()));


        return "mail sent";
    }

}
