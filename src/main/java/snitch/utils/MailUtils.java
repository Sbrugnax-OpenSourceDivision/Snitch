package snitch.utils;

import io.quarkus.mailer.Mail;
import snitch.prometheus.QueryResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MailUtils {

    public static Mail buildMailFromData(ArrayList<QueryResult> data, ArrayList<String> target) {

        Mail tmp = new Mail();
        tmp.setTo(target);
        tmp.setSubject("Snitch recap");
        tmp.setText("O' snicciu ha letto u poddu\naiuto");

        return tmp;
    }

    public static Mail buildMailComposed(List<String> files, ArrayList<String> target) {

        Mail tmp = new Mail();
        tmp.setTo(target);
        tmp.setSubject("Snitch recap");
        tmp.setText("O' snicciu ha letto u poddu\naiuto");


        //TODO Creare grafici per ogni file, utilizzando QueryUtils
        for (String file : files) {
            tmp.addAttachment(file, new File(file), "application/pdf");
        }

        return tmp;
    }

}
