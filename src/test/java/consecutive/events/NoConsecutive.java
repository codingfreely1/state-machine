package consecutive.events;

import state.machine.Event;
import state.machine.State;

/**
 * Created by yael on 27/01/17.
 */
public class NoConsecutive extends State {

    @Override
    public void doAction() {

    }

    public static String getIdentifier() {
        return NoConsecutive.class.getName();
    }
}
