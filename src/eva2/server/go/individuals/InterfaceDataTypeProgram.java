package eva2.server.go.individuals;

import eva2.server.go.individuals.codings.gp.InterfaceProgram;

/** This interface gives access to a program phenotype and except 
 * for problemspecific operators should only be used by the
 * optimization problem.
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 04.04.2003
 * Time: 16:28:29
 * To change this template use Options | File Templates.
 */
public interface InterfaceDataTypeProgram {

    /** This method allows you to request a certain amount of double data
     * @param length    The lenght of the double[] that is to be optimized
     */
    public void setProgramDataLength (int length);

    /** This method allows you to read the program stored as Koza style node tree
     * @return AbstractGPNode representing the binary data.
     */
    public InterfaceProgram[] getProgramData();

    /** This method allows you to read the Program data without
     * an update from the genotype
     * @return InterfaceProgram[] representing the Program.
     */
    public InterfaceProgram[] getProgramDataWithoutUpdate();

    /** This method allows you to set the program.
     * @param program    The new program.
     */
    public void SetProgramData(InterfaceProgram[] program);

    /** This method allows you to set the program.
     * @param program    The new program.
     */
    public void SetProgramDataLamarkian(InterfaceProgram[] program);
    
    /** This method allows you to set the function area
     * @param area  The area contains functions and terminals
     */
    public void SetFunctionArea(Object[] area);

    /** This method allows you to get the function area
     * @return  The area contains functions and terminals
     */
    public Object[] getFunctionArea();
}
