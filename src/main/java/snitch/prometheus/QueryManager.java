package snitch.prometheus;

import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Template;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import snitch.configparser.ConfigMapParser;
import snitch.prometheus.beans.QueryBean;
import snitch.prometheus.beans.QueryResult;
import snitch.http.HttpUtils;
import snitch.mails.MailUtils;
import snitch.mails.PdfUtils;

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

    @ConfigProperty(name = "snitch.send_mail_on_trigger")
    boolean sendOnTrigger;
    @ConfigProperty(name = "snitch.query_fetch_interval")
    String fetchInterval;

    @ConfigProperty(name = "openshift.prometheus_url")
    String prometheusUrl;

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
            String prometheusQuery = HttpUtils.sendGET(prometheusUrl + "/api/v1/query?query=" + query_value, token);
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

        this.queryList = new ArrayList<QueryBean>();

        String id = null;
        String trigger = null;
        ArrayList<QueryBean.Query> querys = null;
        String name = null;

        for (HashMap<String, Object> query : configMapParser.getQueryList()) {

            name = (String) query.get("name");
            id = (String)query.get("id");
            trigger = URLEncoder.encode((String) query.get("trigger"), StandardCharsets.UTF_8);
            querys = new ArrayList<>();

            ArrayList<HashMap<String,String>> tmp =(ArrayList<HashMap<String,String>>) query.get("query");
            for(HashMap<String,String> q:tmp){
                querys.add(new QueryBean.Query(
                        q.get("id"),
                        URLEncoder.encode(q.get("value"), StandardCharsets.UTF_8),
                        QueryBean.Type.valueOf(q.get("type"))
                ));
            }

            this.queryList.add(new QueryBean(
                    querys,
                    id,
                    name,
                    trigger
            ));
        }

        System.out.println("Fetch interval: " + fetchInterval);
    }

    @Scheduled(every = "${snitch.query_fetch_interval}", delayed = "10s")
    public void fetchAll() throws IOException {
        System.out.println("--FETCHING DATA--");

        try {

            File dir = new File("tmp/");

            if (!dir.exists()) {
                dir.mkdir();
            }

            boolean triggered = false;

            for (QueryBean queryBean : this.queryList) {

                queryBean.getQueryData(token, prometheusUrl);

                if(sendOnTrigger && !queryBean.isTriggered(token, prometheusUrl)){
                    triggered = true;
                }

            }

            buildAllPdf();

            if(triggered)
                sendMails();
        } catch (UnknownHostException e) {
            System.out.println("Impossibile connettersi al server Prometheus");
        } catch (Exception e) {
            System.out.println("Esploso il json");
        }

    }

    private void buildAllPdf() throws FileNotFoundException {

        HashMap<String, ArrayList<QueryResult>> data;

        for(QueryBean q: this.queryList){
            data = QueryUtils.getResultFromJson(q);

            PdfUtils.buildPdf(q, data, new FileOutputStream("tmp/" + q.getId() + ".pdf"));
        }
    }

    @GET
    @Path("/mails")
    @Produces(MediaType.TEXT_PLAIN)
    public String sendMails() {

        mailer.send(MailUtils.buildMailComposed(this.queryList.stream().map(
                queryBean -> "tmp/" + queryBean.getId() + ".pdf"
                ).toList()
                , configMapParser.getTargetMails()));

        return "mail sent";
    }

}
