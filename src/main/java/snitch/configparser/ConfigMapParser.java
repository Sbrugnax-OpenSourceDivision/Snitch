package snitch.configparser;

import io.quarkus.runtime.Startup;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class ConfigMapParser {

    public Set<File> configFiles;
    private HashMap<String,ArrayList<HashMap<String,Object>>> tmp;

    private ArrayList<HashMap<String,Object>> queryList;

    private ArrayList<String> targetMails;

    public ConfigMapParser(){

        try{
            this.readFiles();
            this.parseQueryFile();
            this.parseEmails();
        }catch (Exception e){
            System.out.println("Errore lettura files");
        }
    }

    private void readFiles(){

        String s = Paths.get("").toAbsolutePath().toString();
        System.out.println("Current absolute path is: " + s);
        File files = new File("/snitch/config");
        if(!files.exists()) {
            files = new File("kubernetes/snitch/config");
        }
        configFiles = Stream.of(files.listFiles())
                .filter(file -> !file.isDirectory())
                .collect(Collectors.toSet());
    }

    private void parseQueryFile(){

        try {

            Yaml yaml = new Yaml();
            File file = new File("/snitch/config/prometheus_query");
            if(!file.exists()) {
                file = new File("kubernetes/snitch/config/prometheus_query");
            }
            this.tmp=yaml.load(new FileInputStream(file));

            this.queryList = this.tmp.get("queryList");
        } catch (FileNotFoundException e) {
            System.out.println("Yaml parser Error");
        }
    }

    private void parseEmails(){

        try{
            File file = new File("/snitch/config/target_mails");
            if(!file.exists()) {
                file = new File("kubernetes/snitch/config/target_mails");
            }

            this.targetMails = new ArrayList<>();
            String line;

            BufferedReader reader = new BufferedReader(new FileReader(file));
            while((line = reader.readLine()) != null){
                this.targetMails.add(line.trim());
            }

            reader.close();
            System.out.println("Target mails:\n"+this.targetMails);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Produces(MediaType.TEXT_PLAIN)
    public ArrayList<HashMap<String, Object>> getQueryList(){
        return queryList;
    }

    public ArrayList<String> getTargetMails() {
        return targetMails;
    }

}
