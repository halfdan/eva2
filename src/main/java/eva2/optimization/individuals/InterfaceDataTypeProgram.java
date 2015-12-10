package eva2.optimization.individuals;

import eva2.optimization.individuals.codings.gp.InterfaceProgram;

/**
 * This interface gives access to a program phenotype and except
 * for problemspecific operators should only be used by the
 * optimization problem.
 */
public interface InterfaceDataTypeProgram {

    /**
     * This method allows you to request a certain amount of double data
     *
     * @param length The lenght of the double[] that is to be optimized
     */
    void setProgramDataLength(int length);

    /**
     * This method allows you to read the program stored as Koza style node tree
     *
     * @return AbstractGPNode representing the binary data.
     */
    InterfaceProgram[] getProgramData();

    /**
     * This method allows you to read the Program data without
     * an update from the genotype
     *
     * @return InterfaceProgram[] representing the Program.
     */
    InterfaceProgram[] getProgramDataWithoutUpdate();

    /**
     * This method allows you to set the program.
     *
     * @param program The new program.
     */
    void SetProgramPhenotype(InterfaceProgram[] program);

    /**
     * This method allows you to set the program.
     *
     * @param program The new program.
     */
    void SetProgramGenotype(InterfaceProgram[] program);

    /**
     * This method allows you to set the function area
     *
     * @param area The area contains functions and terminals
     */
    void SetFunctionArea(Object[] area);

    /**
     * This method allows you to get the function area
     *
     * @return The area contains functions and terminals
     */
    Object[] getFunctionArea();
}
