package numberInString;

import state.machine.Event;
import state.machine.State;

/**
 * Created by yael on 02/02/17.
 */
public class InvalidNumber extends State {

    @Override
    public void doAction() {

    }

    @Override
    public String getNextStateId(Event event) {
        char curChar = (char) event.getProperties().get(CharEvent.CHAR_EVENT_KEY);
        if(CharEvent.isStartOfNegative(curChar)){
            return NegativeNumber.class.getName();
        }
        if(CharEvent.isValidChar(curChar)){
            return ValidNumber.class.getName();
        }
        return getIdentifier();
    }
}
