package consecutive.events;

import state.machine.State;

/**
 * Created by yael on 27/01/17.
 */
public class PossibleConsecutive extends State {
    private final int MAX_CONSECUTIVE = 3;
    private int count;
    private String prevEvent;

    public PossibleConsecutive() {
        this.count = 0;
        this.prevEvent = null;
    }

    @Override
    public String getNextState(String eventIdentifier) {
        if(prevEvent == null || !prevEvent.equals(eventIdentifier)){
            prevEvent = eventIdentifier;
            count = (prevEvent == null) ? 0 : 1;
        } else {
            count++;
        }
        if(count == MAX_CONSECUTIVE){
            return Consecutive.getIdentifier();
        } else {
            return getIdentifier();
        }
    }

    @Override
    public void doAction() {

    }

    public static String getIdentifier(){
        return PossibleConsecutive.class.getName();
    }
}
