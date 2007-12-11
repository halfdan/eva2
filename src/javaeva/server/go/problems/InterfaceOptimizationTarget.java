package javaeva.server.go.problems;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 14.01.2005
 * Time: 17:06:10
 * To change this template use File | Settings | File Templates.
 */
public interface InterfaceOptimizationTarget {

    public Object clone();

    /** This method allows you to retrieve the name of the optimization target
     * @return The name
     */
    public String getName();
}
