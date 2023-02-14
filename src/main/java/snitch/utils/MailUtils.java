package snitch.utils;
import io.quarkus.mailer.Mail;
import java.util.ArrayList;

public class MailUtils {

    public static Mail buildMail(String path, ArrayList<String> target){

        Mail tmp = new Mail();
        tmp.setTo(target);
        tmp.setSubject("Snitch recap");
        tmp.setText("O' snicciu ha letto u poddu\naiuto");

        return tmp;
    }

}
