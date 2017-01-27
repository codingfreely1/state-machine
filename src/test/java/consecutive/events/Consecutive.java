package consecutive.events;

import state.machine.Event;
import state.machine.State;

/**
 * Created by yael on 27/01/17.
 */
public class Consecutive extends State {

    @Override
    public String getNextState(String eventIdentifier) {
        return NoConsecutive.getIdentifier();
    }

    @Override
    public void doAction() {
        System.out.println("warning: consecutive condition reached");
    }

    public static String getIdentifier() {
        return Consecutive.class.getName();
    }
}
