package consecutive.events;

import state.machine.Event;
import state.machine.State;

/**
 * Created by yael
 */
public class Consecutive extends State {

    @Override
    public String getNextStateId(Event event) {
        return NoConsecutive.class.getName();
    }

    @Override
    public void doAction() {
        System.out.println("warning: consecutive condition reached");
    }

}
