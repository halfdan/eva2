package javaeva.server.go.tools;

import javaeva.server.go.individuals.GAIndividualBinaryData;
import javaeva.server.go.operators.crossover.CrossoverGANPoint;
import javaeva.server.go.operators.crossover.CrossoverGAUniform;
import javaeva.server.go.operators.mutation.MutateGADefault;
import javaeva.server.go.operators.mutation.MutateGAStandard;
import javaeva.server.go.problems.F1Problem;

import java.io.*;
import java.beans.XMLEncoder;
import java.beans.XMLDecoder;

public class Test3 {

    public static void main(String[] args) {
        try {
            XMLEncoder e;
            GAIndividualBinaryData o = new GAIndividualBinaryData();
            MutateGAStandard m = new MutateGAStandard();
            m.setNumberOfMutations(2);
            o.setMutationOperator(m);
            o.setMutationProbability(0.13);
            CrossoverGANPoint t = new CrossoverGANPoint();
            t.setNumberOfCrossovers(1);
            o.setCrossoverOperator(new CrossoverGAUniform());
            o.setCrossoverProbability(0.23);
            o.init(new F1Problem());
            double[] d = new double[2];
            d[0] = 2.34;
            d[1] = 4.5656;
            o.SetFitness(d);
            e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("GAIndividualBinaryData.xml")));
            e.writeObject(o);
            e.flush();
            e.close();
            GAIndividualBinaryData p;
            XMLDecoder decoder = new XMLDecoder(new FileInputStream("GAIndividualBinaryData.xml"));
            p = (GAIndividualBinaryData) decoder.readObject();
            System.out.println(o.getStringRepresentation());
            System.out.println(p.getStringRepresentation());
            decoder.close();

        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }
    /** Serialize a Java object to XML. All attributes that have getter and
     * setter methods will be serialized to elements.
     * @param serializable Java object that implements Serializable.
     * @param outputStream Output stream to write XML to.
     */
    public static void serialize(Serializable serializable, OutputStream outputStream) throws Exception {
        //todo: accept vector of serializables ...
        if (outputStream != null) {
            XMLEncoder encoder = new XMLEncoder(outputStream);
            encoder.writeObject(serializable);
            encoder.close();
        }
    } //serialize()

    /**
     * Deserialize a Java object from XML that was serialized via the
     * serialize method.
     * @param inputStream Input stream to read XML from.
     * @return Serializable Java object from XML.
     * @throws Exception
     * @see de.icomps.sgml.xml#serialize(Serializable, OutputStream)
     */
    public static Serializable deserialize(InputStream inputStream) throws Exception {
        Serializable result = null;
        //todo: return vector of serializables ...
        if (inputStream != null) {
            XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(inputStream));
            result = (Serializable) decoder.readObject();
            decoder.close();
        }
        return result;
    }//deserialize()
}