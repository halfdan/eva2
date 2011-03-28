function desc = getDesc(int, ID)
% For an integer ID, return the String description of the indicated optimizer
% with member descriptions. In case ID is of a different type, it is attempted
% to retrieve a String description for that object directly.

import eva2.gui.BeanInspector;
import eva2.server.modules.GOParameters;
import eva2.OptimizerFactory;

if isnumeric(ID)
  params = OptimizerFactory.getParams(ID, int.mp);
  desc =  BeanInspector.getDescription(params.getOptimizer, false);
else
  desc = BeanInspector.getDescription(ID, false);
end


