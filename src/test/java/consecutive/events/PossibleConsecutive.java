package consecutive.events;

import state.machine.State;

/**
 * Created by yael
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
    public String getNextStateId(String eventIdentifier) {
        if (prevEvent == null || !prevEvent.equals(eventIdentifier)) {
            prevEvent = eventIdentifier;
            count = (prevEvent == null) ? 0 : 1;
        } else {
            count = count +1;
        }

        if (count == MAX_CONSECUTIVE) {
            return Consecutive.class.getName();
        } else {
            return getIdentifier();
        }
    }

    @Override
    public void doAction() {

    }
}
