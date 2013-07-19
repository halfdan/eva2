package eva2.optimization.tools;


import eva2.optimization.individuals.AbstractEAIndividual;
import eva2.optimization.individuals.ESIndividualDoubleData;
import eva2.optimization.operator.archiving.ArchivingAllDominating;
import eva2.optimization.population.Population;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: streiche
 * Date: 13.09.2005
 * Time: 18:51:10
 * To change this template use File | Settings | File Templates.
 */
public class DeNovofilter {

    Population pop;

    public DeNovofilter() {

    }

    public void start() {
        this.loadData();
        ArchivingAllDominating arch = new ArchivingAllDominating();
        arch.addElementsToArchive(pop);
        Population a = pop.getArchive();
        System.out.println("Population " + pop.size());
        System.out.println("Archive    " + a.size());
        for (int i = 0; i < a.size(); i++) {
            String s = "";
            double[] d = ((AbstractEAIndividual)a.get(i)).getFitness();
            for (int j = 0; j < d.length; j++) {
                s += ""+d[j];
                if (j < d.length-1) {
                    s += "; ";
                }
            }
            System.out.println(""+s);
        }
    }
    private void loadData() {
        pop = new Population();
        AbstractEAIndividual indy;
        BufferedReader reader= null;
        try {
            reader = new BufferedReader(new FileReader("TEST.txt"));
        } catch (java.io.FileNotFoundException e) {
            System.out.println("Could not find TEST.txt");
            return;
        }
        String      currentLine;
        try {
            ArrayList tmpA = new ArrayList();
            while((currentLine=reader.readLine())!=null && currentLine.length()!=0) {
                currentLine = currentLine.trim();
                String[] tmpS = currentLine.split(" ");
                double[] tmpD = new double[tmpS.length-2];
                for (int i = 2; i < tmpS.length; i++) {
                    tmpD[i-2] = new Double(tmpS[i]).doubleValue();
                }
                indy = new ESIndividualDoubleData();
                indy.setFitness(tmpD);
                pop.add(indy);
            }
            reader.close();
        } catch (java.io.IOException e) {
            System.out.println("Java.io.IOExeption: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        DeNovofilter d = new DeNovofilter();
        d.start();
    }
}
