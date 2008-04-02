function int = setResultArrayJE(int, arrData)
% Interface function to be called by JavaEvA 2.

% Write back a whole solution set

int.finished = 1;
int.msg=int.mp.getInfoString;
int.funCalls=int.mp.getFunctionCalls;
int.resultArr = arrData;