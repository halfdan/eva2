function int = stopOptimize(int)
% Stop a running optimization

%disp('in Stop!');
int.mp.stopOptimize;
%if (~isempty(int.mediator))
%    int.mediator.quit; % just in case
%    int.mediator='';
%end