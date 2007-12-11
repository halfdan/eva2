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
import java.io.Serializable;

import java.lang.reflect.Method;

import java.util.ArrayList;


/*
 *  ==========================================================================*
 *  CLASS DECLARATION
 *  ==========================================================================
 */
public class EVAThread extends ArrayList implements Serializable
{
    //~ Static fields/initializers /////////////////////////////////////////////

    private static int m_numberofMAXThreads = 20;

    //~ Instance fields ////////////////////////////////////////////////////////

    private ArrayList m_ThreadContainer = new ArrayList();
    private int m_counter = 0;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     *
     */
    public EVAThread()
    {
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     *
     * @return
     */
    public static int getnumberofMAXThreads()
    {
        return m_numberofMAXThreads;
    }

    /**
     *
     */
    public static void setnumberofMAXThreads(int number)
    {
        m_numberofMAXThreads = number;
        XThread.setnumberofThreads(number);
    }

    /**
     *
     */
    public synchronized boolean isAlive()
    {
        //System.out.println(" isAlive m_ThreadContainer.size()"+m_ThreadContainer.size());
        for (int i = 0; i < m_ThreadContainer.size(); i++)
        {
            boolean alive = ((Thread) m_ThreadContainer.get(i)).isAlive();

            if (alive == true)
            {
                return true;
            }
        }

        return false;
    }

    /**
     *
     */
    public synchronized void doit(Object x, String Method, Object[] Para)
    {
        //System.out.println("calling doit "+Method+" of " + x.hashCode());
        m_ThreadContainer.add(XThread.getXThread(x, Method, Para));
    }

    /**
     *
     */
    public synchronized void doit(Object x, Method m, Object[] Para)
    {
        //System.out.println("calling doit "+m.getName()+ " of " + x.hashCode());
        m_ThreadContainer.add(XThread.getXThread(x, m, Para));
    }

    /**
     *
     */
    public static void main(String[] args)
    {
        //    long t = System.currentTimeMillis();
        //    Something ss = new Something(1000);
        //    ss.doit();
        //    t = System.currentTimeMillis() - t;
        //    System.out.println("test time:" + t);
        //Problem problem = new Problem_f1();
        for (int a = 1; a < 10; a++)
        {
            EVAThread multi = new EVAThread();

            for (int i = 0; i < a; i++)
            {
                multi.add(new Something(1000));
            }

            long multitime = System.currentTimeMillis();

            for (int i = 0; i < a; i++)
            {
                multi.doit(multi.get(i), "doit", new Object[]{});
            }

            multi.waitplease();
            multitime = System.currentTimeMillis() - multitime;
            System.out.print("multi time:" + multitime);

            long solotime = System.currentTimeMillis();

            for (int i = 0; i < multi.size(); i++)
            {
                //((Something) multi.get(i)).doitpara(1000);
                ((Something) multi.get(i)).doit();
            }

            solotime = System.currentTimeMillis() - solotime;
            System.out.print(" solo time:" + solotime);

            double frac = ((double) solotime) / ((double) multitime);
            System.out.println(" a=" + a + " frac = " + frac);
            multi.clear();
            multi = null;
        }
    }

    public synchronized void waitplease()
    {
        //System.out.println(" waitplease m_ThreadContainer.size()"+m_ThreadContainer.size());
        for (int i = 0; i < m_ThreadContainer.size(); i++)
        {
            try
            {
                ((Thread) m_ThreadContainer.get(i)).join();
                m_ThreadContainer.remove(i); // testhu
            }
             catch (InterruptedException e)
            {
                e.printStackTrace();
                System.out.println("Error");
            }
        }
    }
}

/**
 *
 */
final class XThread extends Thread implements Serializable
{
    //~ Static fields/initializers /////////////////////////////////////////////

    private static int m_instances; // volatile
    private static int m_MAXinstances = 5;

    //~ Instance fields ////////////////////////////////////////////////////////

    private volatile Method m_method;
    private Object m_x;
    private volatile Object[] m_para;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     *
     */
    private XThread(Object x, Method m, Object[] Para)
    {
        m_x = x;
        m_para = Para;
        m_method = m;
        start();
    }

    /**
     *
     */
    private XThread(Object x, String Method, Object[] Para)
    {
        m_x = x;
        m_para = Para;

        try
        {
            Method[] methods = x.getClass().getDeclaredMethods();

            for (int i = 0; i < methods.length; i++)
            {
                if (methods[i].getName().equals(Method) == true)
                {
                    m_method = methods[i];

                    break;
                }
            }
        }
         catch (Exception e)
        {
            System.out.println(" ERROR in XTHREAD +" + e.getMessage());
        }

        start();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     *
     */
    public static XThread getXThread(Object x, Method m, Object[] Para)
    {
        while (m_instances > m_MAXinstances)
        {
            //System.out.println("waiting "+m_instances+ " on "+x.hashCode()+ " m "+m.getName()+" m_MAXinstances " +m_MAXinstances);
            try
            {
                Thread.sleep(5);
            }
             catch (Exception e)
            {
                System.out.println("Error in sleep of XThread");
            }
        }

        m_instances++;

        return new XThread(x, m, Para);
    }

    /**
     *
     */
    public static XThread getXThread(Object x, String Method, Object[] Para)
    {
        while (m_instances > m_MAXinstances)
        {
            //System.out.println("waiting "+m_instances);
            try
            {
                Thread.sleep(5);
            }
             catch (Exception e)
            {
                System.out.println("Error in sleep of XThread");
            }
        }

        m_instances++;

        return new XThread(x, Method, Para);
    }

    /**
     *
     */
    public static void setnumberofThreads(int number)
    {
        //System.out.println("setnumberofThreads "+number );
        m_MAXinstances = number;
    }

    /**
     *
     */
    public void run()
    {
        if (m_method != null)
        {
            //this.setPriority(Thread.MAX_PRIORITY);
            try
            {
                //        System.out.println("calling !!!!!!!!"+m_method.getName()+ " on " +m_x.hashCode()+" i = "+m_instances);
                m_method.invoke(m_x, m_para);
            }
             catch (Exception e)
            {
                System.out.println("ERROR +" + e.getMessage());
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("Method == null !!!!! in EVAThread ");
        }

        m_instances--;

        //    System.out.println("finished !!!!!!!!"+m_method.getName()+ " on " +m_x.hashCode()+ " i = "+m_instances);
    }
}

/**
 *
 */
interface SomethingInterface
{
    //~ Methods ////////////////////////////////////////////////////////////////

    public void doit();

    public void doitpara(int ii);

    public void nothing();
}

/**
 *
 */
final class Something implements SomethingInterface
{
    //~ Instance fields ////////////////////////////////////////////////////////

    private int m_index;

    //~ Constructors ///////////////////////////////////////////////////////////

    public Something(int i)
    {
        m_index = i;
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     *
     */
    public void doit()
    {
        //System.out.println("--> doit begin");
        double ges = 0;

        for (double i = 0; i < 1000000; i++)
        {
            //System.out.println("Index = "+m_index+" i="+i);
            double x = Math.sin(i) * i;
            double y = (Math.sin(i) * x * x) / i;
            double c = (Math.sin(i) * x * x) / i * x * x * x * x;
            c = c + Math.sin(x) + Math.sin(y) + Math.sin(c);
            ges = ges + x + x;
        }

        //System.out.println("<-- doit end -->"+ges);
    }

    public void doitpara(int ii)
    {
        //System.out.println("--> begin doitpara");
        for (double i = 0; i < (1000 * ii); i++)
        {
            //System.out.println("Index = "+m_index+" i="+i);
            double x = Math.sin(i) * i * i;
        }

        //System.out.println("<-- end doitpara");
    }

    public void nothing()
    {
        //System.out.println("nothing");
    }
}
///////////////////////////////////////////////////////////////////////////////
//  END OF FILE.
///////////////////////////////////////////////////////////////////////////////
