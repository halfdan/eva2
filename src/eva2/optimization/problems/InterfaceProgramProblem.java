package eva2.optimization.problems;

import eva2.optimization.individuals.codings.gp.GPArea;

/**
 * Interface for Program Problems. Solved by Genetic Programming.
 */
public interface InterfaceProgramProblem extends InterfaceOptimizationProblem {

    /**
     * This method allows a GP program to sense the environment, e.g.
     * input values, current time etc
     *
     * @param sensor The identifier for the sensor.
     * @return Sensor value
     */
    public Object getSensorValue(String sensor);

    /**
     * This method allows a GP program to act in the environment
     *
     * @param actuator  The identifier for the actuator.
     * @param parameter The actuator parameter.
     */
    public void setActuatorValue(String actuator, Object parameter);

    /**
     * Return the GPArea associated with the program problem.
     *
     * @return
     */
    public GPArea getArea();
}
