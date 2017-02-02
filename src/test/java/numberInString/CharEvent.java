package numberInString;

import state.machine.Event;

/**
 * Created by yael on 02/02/17.
 */
public class CharEvent extends Event {

    public static String CHAR_EVENT_KEY = "curChar";

    public CharEvent(char curChar) {
        super();
        this.properties.put(CHAR_EVENT_KEY, curChar);
    }

    public static boolean isValidChar(char c){
        return ((c-'0')>=0) && ((c-'9') <=9);
    }

    public static boolean isStartOfNegative(char c){
        return c == '-';
    }

}
