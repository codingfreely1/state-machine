package state.machine;

import exceptions.InvalidState;
import exceptions.StateNotFoundException;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
/**
 * Created by yael
 */
public class StateMachine {

    private State initialState;
    private String curStateId;
    private transient ExecutorService executor;
    private transient List<FutureTask<Boolean>> futureTaskList;
    private int maxSecondsPerAction = 5;
    private Map<String, State> stateMap;
    private transient PersistentWorker<StateMachine> persistentWorker;
    private boolean isPersistent = false;
    private String uuid;

    //This constructor is required for the deserialization to work properly
    private StateMachine() {
        this.executor = Executors.newFixedThreadPool(1);
        this.futureTaskList =  new ArrayList<>();
    }

    public StateMachine(State initialState, State[] states, boolean isPersistent) {
        this();
        this.initialState = initialState;
        this.curStateId = initialState.getIdentifier();
        this.uuid = UUID.randomUUID().toString();
        this.isPersistent = isPersistent;
        this.persistentWorker = new PersistentWorker<>(uuid);
        if(this.isPersistent){
            persistentWorker.serialize(this);
        }
        initStateMap(states);
    }

    private void initStateMap(State[] states){
        this.stateMap = new HashMap<>();
        for (State s : states) {
            this.stateMap.put(s.getIdentifier(), s);
        }
    }

    private void setPersistentWorker(PersistentWorker<StateMachine> persistentWorker) {
        this.persistentWorker = persistentWorker;
    }

    private void removeCompletedTasks(){
        futureTaskList.removeIf(FutureTask::isDone);
    }

    /**
     * Creates a state machine from the last state saved.
     * @param uuid identifier for the saved instance.
     * @return null if resume failed.
     */
    public static StateMachine resume(String uuid) {
        PersistentWorker<StateMachine> persistentWorker = new PersistentWorker<>(uuid);
        StateMachine stateMachine = persistentWorker.deserialize(StateMachine.class);
        if(stateMachine != null){
            stateMachine.setPersistentWorker(persistentWorker);
        }
        return stateMachine;
    }

    public void postEvent(Event event) {
        removeCompletedTasks();
        FutureTask<Boolean> futureTask = new FutureTask<>(() -> {
            String nextStateId = stateMap.get(curStateId).getNextStateId(event);
            if (nextStateId != null) {
                State nextState =  stateMap.get(nextStateId);
                if(nextState == null){
                    throw new StateNotFoundException(nextStateId + " is not defined in the state machine");
                }
                nextState.doAction();
                curStateId = nextStateId;
            }
            if(isPersistent){
                persistentWorker.serialize(this);
            }
            return true;
        });

        futureTaskList.add(futureTask);
        executor.submit(futureTask);

        if(isPersistent){ //we do not continue until new state is saved.
            try {
                getTaskResult(futureTask);
            } catch (InvalidState invalidState) {
                invalidState.printStackTrace();
            }
        }
    }

    public State getInitialState() {
        return initialState;
    }

    public String getCurState() throws InvalidState {
        removeCompletedTasks();
        for(FutureTask<Boolean> task : futureTaskList) {
            getTaskResult(task);
        }
        return stateMap.get(curStateId).getIdentifier();
    }

    private void getTaskResult(FutureTask<Boolean> task) throws InvalidState {
        try {
            task.get(maxSecondsPerAction, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new InvalidState("Action of state was not executed successfully." + e.getMessage());
        }
    }

    /**
     * will reset if state machine is done processing all events.
     * @return
     */
    public boolean resetToInitialState(){
        futureTaskList.removeIf(FutureTask::isDone);
        removeCompletedTasks();
        if(!futureTaskList.isEmpty()){
            return false;
        }
        this.curStateId = initialState.getIdentifier();
        deleteFile(uuid);
        return true;
    }

    private void deleteFile(String fileId){
        final File folder = new File(System.getProperty("user.dir"));
        final File[] files = folder.listFiles((dir, name) -> name.equals(fileId + ".json"));
        assert files != null;
        for ( final File file : files ) {
            if ( !file.delete() ) {
                System.err.println( "Can't remove " + file.getAbsolutePath() );
            }
        }
    }

    public String getUuid() {
        return uuid;
    }

    public int getMaxSecondsPerAction() {
        return maxSecondsPerAction;
    }

    public void setMaxSecondsPerAction(int maxSecondsPerAction) {
        this.maxSecondsPerAction = maxSecondsPerAction;
    }

    public boolean isPersistent() {
        return isPersistent;
    }

    public Map<String, State> getStateMap() {
        return stateMap;
    }

    public void setStateMap(Map<String, State> stateMap) {
        this.stateMap = stateMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StateMachine that = (StateMachine) o;

        if (maxSecondsPerAction != that.maxSecondsPerAction) return false;
        if (isPersistent != that.isPersistent) return false;
        if (!initialState.equals(that.initialState)) return false;
        if (!curStateId.equals(that.curStateId)) return false;
        if (!stateMap.equals(that.stateMap)) return false;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        int result = initialState.hashCode();
        result = 31 * result + curStateId.hashCode();
        result = 31 * result + maxSecondsPerAction;
        result = 31 * result + stateMap.hashCode();
        result = 31 * result + (isPersistent ? 1 : 0);
        result = 31 * result + uuid.hashCode();
        return result;
    }
}
