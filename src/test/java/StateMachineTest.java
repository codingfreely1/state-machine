import coin.operated.turnstile.CoinEvent;
import coin.operated.turnstile.Locked;
import coin.operated.turnstile.PushEvent;
import coin.operated.turnstile.Unlocked;
import consecutive.events.*;
import exceptions.InvalidState;
import numberInString.*;
import org.junit.Assert;
import org.junit.Test;
import state.machine.*;


/**
 * Created by yael
 */
public class StateMachineTest {

    @Test
    public void test(){
        State locked = new Locked();
        State unlocked = new Unlocked();

        StateMachine stateMachine = getCoinPushStateMachine(locked, unlocked);
        stateMachine.postEvent(new CoinEvent());
        try {
            Assert.assertEquals(unlocked.getIdentifier(), stateMachine.getCurState());
            stateMachine.postEvent(new PushEvent());
            Assert.assertEquals(locked.getIdentifier(), stateMachine.getCurState());
        } catch (InvalidState invalidState) {
            invalidState.printStackTrace();
        }
    }

    private StateMachine getCoinPushStateMachine(State locked, State unlocked){
        String coinEventId = CoinEvent.class.getName();
        String pushEventId = PushEvent.class.getName();

        String lockedId = locked.getIdentifier();
        String unlockedId = unlocked.getIdentifier();

        locked.addTransition(coinEventId, unlockedId);
        locked.addTransition(pushEventId, lockedId);

        unlocked.addTransition(coinEventId, unlockedId);
        unlocked.addTransition(pushEventId, lockedId);

        State[] states = {locked, unlocked};

        return new StateMachine(locked, states, false);
    }

    @Test
    public void testConsecutive(){
        State possibleConsecutive = new PossibleConsecutive();
        State consecutive = new Consecutive();
        State noConsecutive = new NoConsecutive();

        StateMachine stateMachine = getConsecutiveStateMachine(possibleConsecutive, consecutive, noConsecutive, false);

        try {
            stateMachine.postEvent(new EventOne());
            Assert.assertEquals(possibleConsecutive.getIdentifier(), stateMachine.getCurState());
            stateMachine.postEvent(new EventOne());
            Assert.assertEquals(possibleConsecutive.getIdentifier(), stateMachine.getCurState());
            stateMachine.postEvent(new EventTwo());
            Assert.assertEquals(possibleConsecutive.getIdentifier(), stateMachine.getCurState());
            stateMachine.postEvent(new EventTwo());
            stateMachine.postEvent(new EventTwo());
            Assert.assertEquals(consecutive.getIdentifier(), stateMachine.getCurState());
            stateMachine.postEvent(new EventTwo());
            Assert.assertEquals(noConsecutive.getIdentifier(), stateMachine.getCurState());
            stateMachine.postEvent(new EventOne());
            Assert.assertEquals(noConsecutive.getIdentifier(), stateMachine.getCurState());
            stateMachine.postEvent(new EventOne());
            stateMachine.postEvent(new EventOne());
            Assert.assertEquals(noConsecutive.getIdentifier(), stateMachine.getCurState());
        } catch (InvalidState invalidState) {
            invalidState.printStackTrace();
        }
    }

    private StateMachine getConsecutiveStateMachine(State possibleConsecutive, State consecutive, State noConsecutive, boolean isPersistent) {
        State[] states = {possibleConsecutive, consecutive, noConsecutive};
        return new StateMachine(possibleConsecutive, states, isPersistent);
    }

    @Test
    public void testNumberInString(){
        State numberExists = new NumberExists();
        State invalid = new InvalidNumber();
        State fraction = new Fraction();
        State validNumber = new ValidNumber();
        State negativeNumber = new NegativeNumber();
        StateMachine stateMachine = getNumberInStringMachine(numberExists, invalid, fraction, negativeNumber,validNumber);

        testString(stateMachine, "1", numberExists.getIdentifier());
        stateMachine.resetToInitialState();
        testString(stateMachine, "-1", numberExists.getIdentifier());
        stateMachine.resetToInitialState();
        testString(stateMachine, "-1.3", numberExists.getIdentifier());
        stateMachine.resetToInitialState();
        testString(stateMachine, "abcd-1.3#", numberExists.getIdentifier());
        stateMachine.resetToInitialState();
        testString(stateMachine, "abcd", invalid.getIdentifier());
        stateMachine.resetToInitialState();
        testString(stateMachine, "", invalid.getIdentifier());
        stateMachine.resetToInitialState();
        testString(stateMachine, "   a3fjld4d", numberExists.getIdentifier());
        stateMachine.resetToInitialState();
    }

    private void testString(StateMachine stateMachine, String s, String expectedState){
        for (int i = 0; i < s.length(); i++) {
            stateMachine.postEvent(new CharEvent(s.charAt(i)));
        }
        stateMachine.postEvent(new CharEvent('\0'));
        try {
            Assert.assertEquals(expectedState, stateMachine.getCurState());
        } catch (InvalidState invalidState) {
            invalidState.printStackTrace();
        }
    }

    private StateMachine getNumberInStringMachine(State numberExists, State invalid, State fraction , State negativeNumber, State validNubmer){
        State[] states = {numberExists, invalid, fraction, negativeNumber, validNubmer};
        return new StateMachine( invalid, states, false);
    }

    @Test
    public void testPersistentMachine() {
        State possibleConsecutive = new PossibleConsecutive();
        State consecutive = new Consecutive();
        State noConsecutive = new NoConsecutive();

        StateMachine stateMachine = getConsecutiveStateMachine(possibleConsecutive,consecutive, noConsecutive, true);
        String uuid = stateMachine.getUuid();

        stateMachine.postEvent(new EventOne());
        try {
            StateMachine recovered = StateMachine.resume(uuid);
            recovered.postEvent(new EventOne());
            recovered.postEvent(new EventOne());
            Assert.assertEquals(consecutive.getIdentifier(), recovered.getCurState());
        } catch (InvalidState invalidState) {
            invalidState.printStackTrace();
        }
        stateMachine.resetToInitialState();//will clear the files
    }

    @Test
    public void testPersistentWorker() {
        State possibleConsecutive = new PossibleConsecutive();
        State consecutive = new Consecutive();
        State noConsecutive = new NoConsecutive();

        StateMachine stateMachine = getConsecutiveStateMachine(possibleConsecutive, consecutive, noConsecutive, true);
        PersistentWorker<StateMachine> persistentWorker = new PersistentWorker<>(stateMachine.getUuid());
        persistentWorker.serialize(stateMachine);

        StateMachine recovered = persistentWorker.deserialize(StateMachine.class);
        Assert.assertEquals(stateMachine, recovered);
        recovered.resetToInitialState();
        stateMachine.resetToInitialState();//will clear the files
    }
}
