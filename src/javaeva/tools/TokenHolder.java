package javaeva.tools;

/**
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 10 $
 *            $Date: 2006-01-18 11:02:22 +0100 (Wed, 18 Jan 2006) $
 *            $Author: streiche $
 */

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.io.BufferedReader;
import java.io.InputStreamReader;


/**
 *
 */
public class TokenHolder extends Thread
{
    //~ Instance fields ////////////////////////////////////////////////////////

    private String m_account;
    private String m_passwd = "";
    private boolean m_wait = true;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     *
     */
    public TokenHolder(String acc, String passw)
    {
        m_account = acc;

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        //System.out.println("passwd:");
        m_passwd = passw;

        //    try {
        //      m_passwd = in.readLine();
        //    } catch (Exception e) {
        //      System.out.println(""+e.getMessage());
        //    }
        start();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     *
     */
    public String getExecOutput(String command)
    {
        String Out = new String();

        try
        {
            BufferedReader in = null;
            Process pro = null;
            //System.out.println("Calling the command:" + command);
            pro = Runtime.getRuntime().exec(command);
            in = new BufferedReader(new InputStreamReader(pro.getInputStream()));

            String line = null;

            while ((line = in.readLine()) != null)
            {
                System.out.println(line);
                Out = Out + line;
            }
        }
         catch (Exception e)
        {
            //System.out.println("Error in calling the command:" + e.getMessage());
        }

        return Out;
    }

    /**
     *
     */
    public static void main(String[] x)
    {
        TokenHolder holder = new TokenHolder("ulmerh", "bal");
    }

    /**
     *
     */
    public void run()
    {
        String s = getExecOutput("tokens");
        //System.out.println("-->tokens " + s);
        s = getExecOutput("klog -principal " + m_account + " -password " +
                m_passwd);

        //System.out.println("-->klog "+s);
        s = getExecOutput("tokens");

        //System.out.println("-->tokens "+s);
        m_wait = true;

        while (m_wait == true)
        {
            //System.out.println(System.currentTimeMillis());

            try
            {
                double min = 360;
                sleep(((long) (1000 * 60 * min)));
            }
             catch (Exception e)
            {
                //System.out.println("" + e.getMessage());
                e.printStackTrace();
            }

            s = getExecOutput("klog -principal " + m_account + " -password " +
                    m_passwd);

            //System.out.println("-->klog "+s);
            s = getExecOutput("tokens");
            //System.out.println("-->tokens " + s);
        }

        //System.out.println("Tokenholder finished !!");
    }

    /**
     *
     */
    public void stoprun()
    {
        m_wait = false;
    }
}
///////////////////////////////////////////////////////////////////////////////
//  END OF FILE.
///////////////////////////////////////////////////////////////////////////////
