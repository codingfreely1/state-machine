package numberInString;

import state.machine.Event;
import state.machine.State;

/**
 * Created by yael on 02/02/17.
 */
public class ValidNumber extends State {

    @Override
    public void doAction() {

    }

    @Override
    public String getNextStateId(Event event) {
        char curChar = (char) event.getProperties().get(CharEvent.CHAR_EVENT_KEY);
        if(curChar == '.'){
            return Fraction.class.getName();
        }
        if(CharEvent.isValidChar(curChar)){
            return getIdentifier();
        }
        return NumberExists.class.getName();
    }
}
