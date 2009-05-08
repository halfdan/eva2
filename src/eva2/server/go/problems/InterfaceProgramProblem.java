package eva2.server.go.problems;

import eva2.server.go.individuals.codings.gp.GPArea;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.04.2003
 * Time: 14:43:03
 * To change this template use Options | File Templates.
 */
public interface InterfaceProgramProblem {

    /** This method allows a GP program to sense the environment, e.g.
     * input values, current time etc
     * @param sensor    The identifier for the sensor.
     * @return Sensor value
     */
    public Object getSensorValue(String sensor);

    /** This method allows a GP program to act in the environment
     * @param actuator      The identifier for the actuator.
     * @param parameter     The actuator parameter.
     */
    public void setActuatorValue(String actuator, Object parameter);
    
    /**
     * Return the GPArea associated with the program problem.
     * @return
     */
    public GPArea getArea();
}
