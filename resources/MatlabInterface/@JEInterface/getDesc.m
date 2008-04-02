function desc = getDesc(int, ID)
% Return the String description of an indicated optimizer
% with member descriptions.

import javaeva.gui.BeanInspector;
import javaeva.server.modules.GOParameters;
import javaeva.OptimizerFactory;

params = OptimizerFactory.getParams(ID, int.mp);
desc =  BeanInspector.getDescription(params.getOptimizer, false);

