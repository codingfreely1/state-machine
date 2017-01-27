package state.machine;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yael on 26/01/17.
 */
public abstract class State {

    private Map<String, String> transitions;

    public State() {
        this.transitions = new HashMap<>();
    }

    public void addTransition(String event, String state){
        this.transitions.put(event, state);
    }

    public abstract void doAction();

    /**
     * getNextState
     * @return next state or null if no transition.
     */
    public String getNextState(String eventIdentifier){
        return transitions.get(eventIdentifier);
    }
}
