import coin.operated.turnstile.CoinEvent;
import coin.operated.turnstile.Locked;
import coin.operated.turnstile.PushEvent;
import coin.operated.turnstile.Unlocked;
import consecutive.events.*;
import exceptions.InvalidState;
import org.junit.Assert;
import org.junit.Test;
import state.machine.State;
import state.machine.StateMachine;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yael on 26/01/17.
 */
public class StateMachineTest {

    @Test
    public void test(){
        State locked = new Locked();
        State unlocked = new Unlocked();

        String coinEventId = CoinEvent.class.getName();
        String pushEventId = PushEvent.class.getName();

        String lockedId = Locked.getIdentifier();
        String unlockedId = Unlocked.getIdentifier();

        locked.addTransition(coinEventId, unlockedId);
        locked.addTransition(pushEventId, lockedId);

        unlocked.addTransition(coinEventId, unlockedId);
        unlocked.addTransition(pushEventId, lockedId);

        Map<String, State> statesMap = new HashMap<>();
        statesMap.put(lockedId, locked);
        statesMap.put(unlockedId, unlocked);

        StateMachine stateMachine = new StateMachine(locked, statesMap);

        stateMachine.postEvent(new CoinEvent());
        try {
            Assert.assertEquals(unlocked, stateMachine.getCurState());
            stateMachine.postEvent(new PushEvent());
            Assert.assertEquals(locked, stateMachine.getCurState());
        } catch (InvalidState invalidState) {
            invalidState.printStackTrace();
        }
    }

    @Test
    public void testConsecutive(){
        State possibleConsecutive = new PossibleConsecutive();
        State consecutive = new Consecutive();
        State noConsecutive = new NoConsecutive();

        Map<String, State> statesMap = new HashMap<>();
        statesMap.put(PossibleConsecutive.getIdentifier(), possibleConsecutive);
        statesMap.put(Consecutive.getIdentifier(), consecutive);
        statesMap.put(NoConsecutive.getIdentifier(), noConsecutive);

        StateMachine stateMachine = new StateMachine(possibleConsecutive, statesMap);

        try {
            stateMachine.postEvent(new EventOne());
            Assert.assertEquals(possibleConsecutive, stateMachine.getCurState());
            stateMachine.postEvent(new EventOne());
            Assert.assertEquals(possibleConsecutive, stateMachine.getCurState());
            stateMachine.postEvent(new EventTwo());
            Assert.assertEquals(possibleConsecutive, stateMachine.getCurState());
            stateMachine.postEvent(new EventTwo());
            stateMachine.postEvent(new EventTwo());
            Assert.assertEquals(consecutive, stateMachine.getCurState());
            stateMachine.postEvent(new EventTwo());
            Assert.assertEquals(noConsecutive, stateMachine.getCurState());
            stateMachine.postEvent(new EventOne());
            Assert.assertEquals(noConsecutive, stateMachine.getCurState());
            stateMachine.postEvent(new EventOne());
            stateMachine.postEvent(new EventOne());
            Assert.assertEquals(noConsecutive, stateMachine.getCurState());
        } catch (InvalidState invalidState) {
            invalidState.printStackTrace();
        }
    }
}
