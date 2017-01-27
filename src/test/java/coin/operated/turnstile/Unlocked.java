package coin.operated.turnstile;

import state.machine.State;

/**
 * Created by yael on 26/01/17.
 */
public class Unlocked extends State {

    @Override
    public void doAction() {

    }

    public static String getIdentifier(){
        return Unlocked.class.getName();
    }
}
