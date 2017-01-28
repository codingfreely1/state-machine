import coin.operated.turnstile.CoinEvent;
import coin.operated.turnstile.Locked;
import coin.operated.turnstile.PushEvent;
import coin.operated.turnstile.Unlocked;
import consecutive.events.*;
import exceptions.InvalidState;
import org.junit.Assert;
import org.junit.Test;
import state.machine.*;

import java.io.File;

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
            Assert.assertEquals(unlocked, stateMachine.getCurState());
            stateMachine.postEvent(new PushEvent());
            Assert.assertEquals(locked, stateMachine.getCurState());
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

    private StateMachine getConsecutiveStateMachine(State possibleConsecutive, State consecutive, State noConsecutive, boolean isPersistent) {
        State[] states = {possibleConsecutive, consecutive, noConsecutive};
        return new StateMachine(possibleConsecutive, states, isPersistent);
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
            Assert.assertEquals(consecutive, recovered.getCurState());
        } catch (InvalidState invalidState) {
            invalidState.printStackTrace();
        }
        deleteFile(stateMachine.getUuid());
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
        deleteFile(stateMachine.getUuid());
    }

    private void deleteFile(String fileId){
        final File folder = new File(System.getProperty("user.dir"));
        final File[] files = folder.listFiles((dir, name) -> name.equals(fileId + ".json"));
        assert files != null;
        for ( final File file : files ) {
            if ( !file.delete() ) {
                System.err.println( "Can't remove " + file.getAbsolutePath() );
            }
        }
    }
}
