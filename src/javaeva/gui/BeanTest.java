package javaeva.gui;
/*
 * Title:        JavaEvA
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      University of Tuebingen, Computer Architecture
 * @author Holger Ulmer, Felix Streichert, Hannes Planatscher
 * @version:  $Revision: 202 $
 *            $Date: 2007-10-25 16:12:49 +0200 (Thu, 25 Oct 2007) $
 *            $Author: mkron $
 */
/*==========================================================================*
 * IMPORTS
 *==========================================================================*/
import java.beans.*;
import java.lang.reflect.Method;
//import javaeva.server.oa.es.mutation.*;
import javaeva.tools.*;
import javaeva.server.EvAServer;
/*
 *  ==========================================================================*
 *  CLASS DECLARATION
 *  ==========================================================================
 */
public class BeanTest {
  public static boolean TRACE = false;

  public static int  step = 0;
  public static String check(String s) {

     s=s.replace('$','_');
     s=s.replace(';','_');
//   String ret = null;
//   try {
//    RE r = new RE("\\[");
//    ret = r.subst(s,"");
//    //ret.substring();
//    //ret
//    } catch (Exception e) {e.getMessage();};
//    System.out.println("s="+s+"  ret"+ret);
    if (s.equals("[D")) return "Double_Array";
    if (s.startsWith("[D")) return s.substring(2);
     if (s.startsWith("[L")) return s.substring(2);

    return s;
  }

    public static boolean equalProperties(Object Target_1, Object Target_2) {
        if (Target_1 == null || Target_2 == null) {
            System.out.println("");
            return false;
        }
        System.out.println("equalProperties: " + Target_1.getClass().getName() + " " + Target_2.getClass().getName());
        if (Target_1.getClass().getName().equals(Target_2.getClass().getName()) == false) {
            System.out.println("");
            return false;
        }
        // compare each of the properties !!
        BeanInfo Info_1 = null;
        BeanInfo Info_2 = null;
        PropertyDescriptor[] Properties_1 = null;
        PropertyDescriptor[] Properties_2 = null;
        MethodDescriptor[] Methods_1 = null;
        MethodDescriptor[] Methods_2 = null;
        try {

            Info_1 = Introspector.getBeanInfo(Target_1.getClass());
            Info_2 = Introspector.getBeanInfo(Target_2.getClass());
            Properties_1 = Info_1.getPropertyDescriptors();
            Properties_2 = Info_2.getPropertyDescriptors();
            Methods_1 = Info_1.getMethodDescriptors();
        } catch (IntrospectionException ex) {
            System.out.println("BeanTest: Couldn't introspect !!!!!!!!!");
            return false;
        }
        boolean BeansInside = false;
        boolean BeansEqual = true;
        for (int i = 0; i < Properties_1.length; i++) {
            if (Properties_1[i].isHidden() || Properties_1[i].isExpert()) {
                continue;
            }
            //String name = Properties_1[i].getDisplayName(); //System.out.println("name = "+name );
            //Class type = Properties_1[i].getPropertyType(); //System.out.println("type = "+type.getName() );
            Method getter_1 = Properties_1[i].getReadMethod();
            Method getter_2 = Properties_2[i].getReadMethod();
            Method setter_1 = Properties_1[i].getWriteMethod();
            // Only display read/write properties.
            if (getter_1 == null || setter_1 == null) {
                continue;
            }
            System.out.println("getter_1 = " + getter_1.getName() + " getter_2 = " + getter_2.getName());
            //System.out.println("type = "+type.getName() );
            Object args_1[] = {};
            Object args_2[] = {};
            //System.out.println("m_Target"+m_Target.toString());
            try {
                Object value_1 = getter_1.invoke(Target_1, args_1);
                Object value_2 = getter_2.invoke(Target_2, args_2);
                BeansInside = true;
                if (BeanTest.equalProperties(value_1, value_2) == false) {
                    BeansEqual = false;
                }
            } catch (Exception e) {
                System.out.println(" BeanTest.equalProperties " + e.getMessage());
            }
        }
        if (BeansInside == true) {
            return BeansEqual;
        }
        // here we have Integer or Double ...
        if (Target_1 instanceof Integer ||
                Target_1 instanceof Boolean ||
                Target_1 instanceof Float ||
                Target_1 instanceof Double ||
                Target_1 instanceof Long ||
                Target_1 instanceof String) {
            return Target_1.equals(Target_2);
        }

        System.out.println(" Attention no match !!!");
        return true;
    }


    /**
     *@param  Target  Description of the Parameter
     *@return         Description of the Return Value
     */
    public static String toString(Object Target) {
        String ret = "";
        // object itself
        try {
            Method[] methods = Target.getClass().getDeclaredMethods();
            for (int ii = 0; ii < methods.length; ii++) {
                if (methods[ii].getName().equals("toString") == true) {
                    ret = (String) methods[ii].invoke(Target, (Object[])null);
                    //System.out.println("calling to String off: "+Target.getClass().getName()+"=="+s);
                }
            }
        } catch (Exception e) {
            //System.out.println(" ERROR +"+ e.getMessage());
        }
        // then the properties
        BeanInfo Info = null;
        PropertyDescriptor[] Properties = null;
        MethodDescriptor[] Methods = null;
        try {
            Info = Introspector.getBeanInfo(Target.getClass());
            Properties = Info.getPropertyDescriptors();
            Methods = Info.getMethodDescriptors();
        } catch (IntrospectionException ex) {
            System.out.println("BeanTest: Couldn't introspect");
            return ret;
        }
        for (int i = 0; i < Methods.length; i++) {
            String name = Methods[i].getDisplayName();
            Method meth = Methods[i].getMethod();
        }
        for (int i = 0; i < Properties.length; i++) {
            if (Properties[i].isHidden() || Properties[i].isExpert()) {
                continue;
            }
            String name = Properties[i].getDisplayName();
            //System.out.println("name = "+name );
            Class type = Properties[i].getPropertyType();
            //System.out.println("type = "+type.getName() );
            Method getter = Properties[i].getReadMethod();
            Method setter = Properties[i].getWriteMethod();
            // Only display read/write properties.
            if (getter == null || setter == null) {
                continue;
            }
            //System.out.println("name = "+name );
            //System.out.println("type = "+type.getName() );
            Object args[] = {};
            //System.out.println("m_Target"+m_Target.toString());

            try {
                Object value = getter.invoke(Target, args);
                ret = ret + toString(value);
            } catch (Exception e) {
                //System.out.println("BeanTest ERROR +"+ e.getMessage());
                return ret;
            }
        }
        return ret;
    }


    /**
     *@param  Target  Description of the Parameter
     */
    public static void showInfo(Object Target) {
        System.out.println("Inspecting " + Target.getClass().getName());
        // object itself
        try {
            if (Target instanceof java.lang.Integer) {
                System.out.println(" Prop = Integer" + Target.toString());
            }
            if (Target instanceof java.lang.Boolean) {
                System.out.println(" Prop = Boolean" + Target.toString());
            }
            if (Target instanceof java.lang.Long) {
                System.out.println(" Prop = Long" + Target.toString());
            }
            if (Target instanceof java.lang.Double) {
                System.out.println(" Prop = Long" + Target.toString());
            }
        } catch (Exception e) {
            //System.out.println(" ERROR +"+ e.getMessage());
        }
        // then the properties
        BeanInfo Info = null;
        PropertyDescriptor[] Properties = null;
        MethodDescriptor[] Methods = null;
        try {
            Info = Introspector.getBeanInfo(Target.getClass());
            Properties = Info.getPropertyDescriptors();
            Methods = Info.getMethodDescriptors();
        } catch (IntrospectionException ex) {
            System.err.println("BeanTest: Couldn't introspect");
            return;
        }
        for (int i = 0; i < Methods.length; i++) {
            String name = Methods[i].getDisplayName();
            Method meth = Methods[i].getMethod();
        }
        for (int i = 0; i < Properties.length; i++) {
            if (Properties[i].isHidden() || Properties[i].isExpert()) {
                continue;
            }
            String name = Properties[i].getDisplayName();
            //System.out.println("name = "+name );
            Class type = Properties[i].getPropertyType();
            //System.out.println("type = "+type.getName() );
            Method getter = Properties[i].getReadMethod();
            Method setter = Properties[i].getWriteMethod();
            // Only display read/write properties.
            if (getter == null || setter == null) {
                continue;
            }
            //System.out.println("name = "+name );
            //System.out.println("type = "+type.getName() );
            Object args[] = {};
            //System.out.println("m_Target"+m_Target.toString());
            try {
                Object value = getter.invoke(Target, args);
                System.out.println("Inspecting name = " + name);
                if (value instanceof Integer) {
                    Object args2[] = {new Integer(999)};
                    setter.invoke(Target, args2);
                }
                showInfo(value);
            } catch (Exception e) {
                System.out.println("BeanTest ERROR +" + e.getMessage());
            }
        }
    }


    /**
     *@param  args  The command line arguments
     */
//    public static void main(String[] args) {
//      Tag t= new Tag();
//      System.out.println("name ========"+t.getClass().getName());
//        System.setProperty("java.security.policy","server.policy");
//        ESPopulation x = new ESPopulation();
//        x.add(new ESIndividual());
//        x.add(new ESIndividual());
//        x.add(new ESIndividual());
//        x.add(new ESIndividual());
//        x.add(new ESIndividual());
//
//        ESPopulation y = new ESPopulation();
//        ESIndividual es = new ESIndividual();
//        MutationCMA ca = new MutationCMA();
//        es.setMutation(ca);
//        ca.setConstraints(true);
//        y.setIndividualTemplate(es);
//        System.out.println("***********");
//        //BeanTest.showInfo(x);
//        EvAServer xxx = new EvAServer(false,false);
//        //ObjectTOXML test = new ObjectTOXML();
////        Element el = test.getXML(xxx,xxx.getClass(),false);
////        Document doc = new Document(el);
////        HTEFile f = HTEFile.getInstance();
////        f.writeXMLFile(doc);
////        System.out.println("***********");
////        try{
////        XMLEncoder e = new XMLEncoder(
////                          new BufferedOutputStream(
////                              new FileOutputStream("Test.xml")));
////       e.writeObject(x);
////       e.close();
////       } catch (Exception ee ) {}
//
//        //StandardESPopulation x = BeanTest.getRandomInstance(Class.forName("javaeva.server.oa.es.StandardESPopulation"));
//        //System.out.println("equal ??" + BeanTest.equalProperties(x, y));
//    }
}

class mySecurityManager extends SecurityManager {
 public void checkMemberAccess( Class c, int which ) {
System.out.println("KAKA") ; }// This is called only for
                     // getDeclaredFields() and not for Field.get( )

 public void checkPackageAccess( String p ) {}
 }
