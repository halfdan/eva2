package eva2.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * <p>Title: EvA2</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class URLGetter {
    public URLGetter() {

    }

    public static String getData(String urlstr) {
        System.getProperties().put("proxySet", "true");
        System.getProperties().put("proxyHost",
                "www-cache.informatik.uni-tuebingen.de");
        System.getProperties().put("proxyPort", "3128");

        StringBuilder res = new StringBuilder();
        try {
            URL url = new URL(urlstr);
            URLConnection conn = url.openConnection();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                res.append(line + "\n");
            }

            rd.close();

        } catch (Exception ex) {
            System.out.println("URLGETTER ERROR: " + ex);
        }

        return res.toString();
    }

    public static void main(String[] args) {
        URLGetter URLGetter1 = new URLGetter();
    }

}
