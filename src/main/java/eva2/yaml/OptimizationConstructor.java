package eva2.yaml;

import eva2.optimization.operator.distancemetric.InterfaceDistanceMetric;
import eva2.optimization.population.Population;
import eva2.optimization.population.PopulationInitMethod;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by fabian on 07/12/15.
 */
public class OptimizationConstructor extends Constructor {
    public OptimizationConstructor() {
        this.yamlConstructors.put(new Tag("!population"), new ConstructPopulation());
    }

    private class ConstructPopulation extends AbstractConstruct {

        @Override
        public Object construct(Node node) {
            Population pop = new Population();
            Map<Object, Object> values = constructMapping((MappingNode)node);

            pop.setInitAround((Double) values.get("initAround"));
            pop.setInitMethod(PopulationInitMethod.valueOf((String) values.get("initMethod")));

            ArrayList<Double> initPos = (ArrayList<Double>) values.get("initPos");
            pop.setInitPos(initPos.stream().mapToDouble(Double::doubleValue).toArray());

            pop.setPopMetric((InterfaceDistanceMetric) values.get("popMetric"));
            //pop.setSeedCardinality((Pair<Integer, Integer>) values.get("seedCardinality"));
            pop.setTargetSize((Integer) values.get("targetSize"));
            return pop;
        }
    }
}
