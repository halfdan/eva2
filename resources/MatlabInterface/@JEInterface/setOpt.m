function int = setOpt(int, optName, optVal)
% Set a single optimset value within the JI instance.
% Arguments: 
%           int: the JEInterface instance
%           optName: name of the option to change, e.g. 'MaxFunEvals'
%           optVal: new value

opts = optimset(int.opts, optName, optVal);
int.opts = opts;