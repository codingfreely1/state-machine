package numberInString;

import state.machine.Event;
import state.machine.State;

/**
 * Created by yael on 02/02/17.
 */
public class NumberExists extends State{

    @Override
    public void doAction() {

    }

    @Override
    public String getNextStateId(Event event) {
        return getIdentifier();//final state
    }
}
