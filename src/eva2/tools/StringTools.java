package eva2.tools;

import java.util.StringTokenizer;

public class StringTools {

    /**
     * Returns a HTML formated String, in which each line is at most lineBreak
     * symbols long.
     *
     * @param string
     * @param lineBreak
     * @return
     */
    public static String toHTML(String string, int lineBreak) {
        StringTokenizer st = new StringTokenizer(string, " ");
        if (!st.hasMoreTokens()) return "<html><body></body></html>";
        StringBuffer sbuf = new StringBuffer(st.nextToken());
        
        int length = sbuf.length();
        while (st.hasMoreElements()) {
            if (length >= lineBreak) {
                sbuf.append("<br>");
                length = 0;
            } else sbuf.append(" ");
            String tmp = st.nextToken();
            length += tmp.length() + 1;
            sbuf.append(tmp);
        }
        sbuf.insert(0, "<html><body>");
        sbuf.append("</body></html>");
        return sbuf.toString();
    }
    
    public static void main(String[] args) {
    	System.out.println(toHTML("Hallo-asdfsadfsafdsadfo, dies ist ein doller test text!", 15));
    	System.out.println(toHTML("Set the interval of data output for intermediate verbosity (in generations).", 15));
    	System.out.println(toHTML("Set the interval of data output for intermediate verbosity (in generations).", 25));
    	System.out.println(toHTML("Set the interval of data output for intermediate verbosity (in generations).", 30));
    }
}
