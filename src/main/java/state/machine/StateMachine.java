package state.machine;

import com.sun.org.apache.xpath.internal.operations.Bool;
import exceptions.InvalidState;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
/**
 * Created by yael on 26/01/17.
 */
public class StateMachine {

    private State initialState;
    private State curState;
    private ExecutorService executor;
    private List<FutureTask<Boolean>> futureTaskList;
    private int maxSecondsPerAction = 5;
    Map<String, State> stateMap;

    public StateMachine(State initialState, Map<String, State> statesMap) {
        this.initialState = initialState;
        this.curState = initialState;
        this.executor = Executors.newFixedThreadPool(1);
        this.futureTaskList = new ArrayList<>();
        this.stateMap = statesMap;
    }

    public void setMaxSecondsPerAction(int maxSecondsPerAction) {
        this.maxSecondsPerAction = maxSecondsPerAction;
    }

    public void reset(){
        this.curState = initialState;
        this.futureTaskList = new ArrayList<>();
    }

    /**
     *
     * @param event
     */
    public void postEvent(Event event) {
        removeCompletedTasks();
        FutureTask<Boolean> futureTask = new FutureTask<>(() -> {
            String nextStateId = curState.getNextState(event.getIdentifier());
            if (nextStateId != null) {
                State nextState =  stateMap.get(nextStateId);
                Assert.assertNotNull(nextState);
                nextState.doAction();
                curState = nextState;
            } //else do nothing. stay in same state do not do action again. ??
            return true;
        });
        futureTaskList.add(futureTask);
        executor.submit(futureTask);
    }

    /**
     *
     * @return
     * @throws InvalidState
     */
    public State getCurState() throws InvalidState {
        removeCompletedTasks();
        for(FutureTask<Boolean> task : futureTaskList) {
            try {
                task.get(maxSecondsPerAction, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new InvalidState("Action of state was not executed successfully");
            }
        }
        return curState;
    }

    private void removeCompletedTasks(){
        futureTaskList.removeIf(FutureTask::isDone);
    }
}
