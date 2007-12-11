package javaeva.tools;

import java.io.Serializable;

/**
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 255 $
 *            $Date: 2007-11-15 14:58:12 +0100 (Thu, 15 Nov 2007) $
 *            $Author: mkron $
 */

/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.net.InetAddress;

import java.util.ArrayList;


/*==========================================================================*
 * CLASS DECLARATION
 *==========================================================================*/

/**
 *
 */
public class LogProxy implements InvocationHandler
{
    //~ Static fields/initializers /////////////////////////////////////////////

    public static final boolean TRACE = false;

    //~ Instance fields ////////////////////////////////////////////////////////

    private Object m_Object;
    private String m_ObjectName;
    private double m_timeges = 0;
    private long m_counter = 0;
    private long m_timestart = 0;

    //~ Constructors ///////////////////////////////////////////////////////////

    /**
     *
     */
    public LogProxy(Object obj)
    {
        m_Object = obj;
        m_ObjectName = obj.getClass().getName();
        m_timestart = System.currentTimeMillis();
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    /**
     *
     */
    public static Object newInstance(Object obj)
    {
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(),
            obj.getClass().getInterfaces(), new LogProxy(obj));
    }

    /**
     *
     */
    public Object invoke(Object proxy, Method m, Object[] args)
    throws Throwable
    {
    	long start = System.currentTimeMillis();
    	++m_counter;

    	Object ret = null;

    	try
    	{
    		//if (TRACE)System.out.println("Before invoke:" +m.getName());
    		long t = System.currentTimeMillis();
    		ret = m.invoke(m_Object, args);

    		long t2 = System.currentTimeMillis();
    		t = t2 - t;
    		m_timeges = m_timeges + t;

    		double x = m_timeges / ((double) (t2 - m_timestart));
    		System.out.println("x=" + x + "timeges" + m_timeges);
    	}
    	catch (InvocationTargetException e)
    	{
    		System.out.println("LogProxy: InvocationTargetException" +
    				e.getMessage());
    	}
    	catch (Exception e)
    	{
    		System.out.println("Exception" + e.getMessage());
    	}
    	finally
    	{
    		//long finish = System.currentTimeMillis();

    		//System.out.println("Calling :"+m.getName()+" of "+m_ObjectName+ " time :"+(finish-start));
    	}
    	if (ret == m_Object)
    	{
    		return this;
    	}

    	return ret;

    }

}

