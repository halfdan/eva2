clear all
clear classes

% adapt the path settings!
addpath '/home/mkron/workspace/JE2Base/resources/MatlabInterface'
javaaddpath '/home/mkron/workspace/JE2Base/build'
addpath 'C:\Dokumente und Einstellungen\mkron\workspace\JE2Base\resources\MatlabInterface'
javaaddpath 'C:\Dokumente und Einstellungen\mkron\workspace\JE2Base\build'

% real valued case
R=[-5 -5 -5; 5 5 5];
JI=JEInterface(@testfun, 'double', R, R, 1, 'Display', 'iter', 'TolX', 0, 'TolFun', 0);
JI=optimize(JI, 4);
[sol, solFit]=getResult(JI);
finalPop=getMultipleResults(JI);

% binary case
R=20;
JI=JEInterface(@testfun, 'binary', R, R, 4, 'Display', 'iter');
JI=optimize(JI, 3);
[sol, fit]=getResult(JI);
finalPop=getMultipleResults(JI);

% integer case with specific initialization range
initR=[-15 -15 -15 -15 -15; -5 -5 -5 -5 -5];
R=[-15 -15 -15 -15 -15; 15 15 15 15 15];
JI=JEInterface(@testfun, 'int', R, initR, 5, 'Display', 'iter');
JI=optimize(JI, 3);
[sol, fit]=getResult(JI);
finalPop=getMultipleResults(JI);

