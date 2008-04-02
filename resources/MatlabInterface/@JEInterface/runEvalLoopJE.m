function int=runEvalLoopJE(int, optOrPostProc, optType, outputFilePrefix, steps, sigmaClust, nBest)
% Internal method starting a JavaEvA optimization loop.
% Calling this directly may interfere with optimization.

% optOrPostProc: 1 for optimize, 2 for postProcess
% optType, outputFilePrefix are parameters for optimize, dont care when
% postprocessing.
% steps, sigmaClust and nBest are parameters for postProcess, dont care
% when optimizing. nBest may be -1 to show all.

global stopOptimization

if ~isempty(int.mediator)
    int.mediator.quit
    int.mediator='';
end

% set up a mediator and inform JE
int.mediator = javaeva.server.go.problems.MatlabEvalMediator;
int.mp.setMediator(int.mediator);

% start the JE thread
if (optOrPostProc == 1)
    stopText='Stop JavaEvA optimization';
    int.mp.optimize(optType, outputFilePrefix, int.optParams, int.optParamValues);
else % post processing
    stopText='Stop JavaEvA post processing';
    int.mp.requestPostProcessing(steps, sigmaClust, nBest);
end

% handle the case when the optimization has been called from a users script
% and not from the toolboxes parameter estimation function (which has an
% own stop button). we decide this by checking if the global variable
% stopOptimization is empty. if it is then it is not the toolbox calling
% and we create an own button to stop it.
if isempty(stopOptimization),
    % create a cancel button box (only in the case that
    h=figure('Position',[100 600 250 80], 'MenuBar', 'none', 'Name', 'JavaEvA optimization running...', 'NumberTitle','off');
    uicontrol(h,'Style', 'pushbutton', 'String', 'Cancel', 'Position', [25 25 60 30], 'Callback', 'global stopOptimization; stopOptimization=1;');
    uicontrol(h,'Style', 'text', 'String', stopText, 'Position', [100 25 120 30]);
    drawnow;
    % set it to 0 now
    stopOptimization = 0;
    % set flag for non toolbox optimization
    nontoolboxopt = 1;
else
    % its an estimation using the toolbox' parameter estimation thing
    nontoolboxopt = 0;
end

stopOnce=1;

% repeat the mediator thread and eval call until finished
while (~int.mediator.isFinished())
    int.mediator.run;
    if (~int.mediator.isFinished())
        x = int.mediator.getQuestion();
        if (isempty(int.args))
            res = feval(int.f, x);
        else
            res = feval(int.f, x, int.args);
        end
        int.mediator.setAnswer(res);
        drawnow;
        if ((stopOptimization==1) && (stopOnce==1))
            disp('User interrupt requested ...');
            stopOptimize(int);
            stopOnce=0;
        end
    end
end

% write back results
int=setResultJE(int, int.mediator.getSolution());
int=setResultArrayJE(int, int.mediator.getSolutionSet());

int.mediator.quit; % just in case
int.mediator='';

% handle the case when the optimization has been called from a users script
% and not from the toolboxes parameter estimation function (which has an
% own stop button). we decide this by checking nontoolboxopt
if nontoolboxopt == 1,
    close(h);
    clear global stopOptimization
end