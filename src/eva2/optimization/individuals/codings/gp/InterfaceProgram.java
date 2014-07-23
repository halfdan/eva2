package eva2.optimization.individuals.codings.gp;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 13.06.2003
 * Time: 18:25:13
 * To change this template use Options | File Templates.
 */
public interface InterfaceProgram {

    /**
     * This method will evaluate the program. It is allowed to interact with the enviroment
     * using the getSensorValue and setActuatorValue methods.
     *
     * @param environment
     */
    public Object evaluate(eva2.problems.InterfaceProgramProblem environment);

    /**
     * This method returns a string representation of the current program.
     *
     * @return string
     */
    public String getStringRepresentation();
}
