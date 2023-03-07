package tudelft.in4150.da;

import java.rmi.RemoteException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Second tests for Schiper-Eggli-Sandoz algorithm implementation.
 * NOTE: process arrays start at index 0 but containsKey() starts at index 1 in assert tests.
 */
@SuppressWarnings("checkstyle:magicnumber")
public class SecondTest {
    private static final Logger LOGGER = LogManager.getLogger(DASchiperEggliSandoz.class);
    private static final int PORT = 1098;

    @BeforeAll
    private static void setup() {
        System.setProperty("java.security.policy", "java.policy");
        DASchiperEggliSandoz.initRegistry(PORT);
    }

    /**
     * More advanced test with 3 processes (P1, P2, and P3).
     * P1 sends m1 -> P3 with 2000ms delay.
     * P1 sends m2 -> P2 with no delay.
     * P2 sends m3 -> P3 with no delay.
     */
    @Test
    public void test() {
        final int numProcesses = 3;
        final int delay = 2000;

        DASchiperEggliSandoz[] processes = DASchiperEggliSandoz.createProcesses(numProcesses, PORT);

        try {
            processes[0].send(processes[2].getId(), new Message(numProcesses), delay);
            processes[0].send(processes[1].getId(), new Message(numProcesses), 0);
            processes[1].send(processes[2].getId(), new Message(numProcesses), 0);

            // P3 should buffer m3 as its missing m1, hence key of P2 exists in messageBuffer of P3.
            assertTrue(processes[2].getMessageBuffer().containsKey(2));

        } catch (RemoteException e) {
            LOGGER.error("Remote exception sending messages.");
            e.printStackTrace();
        }

        // Sleep until all delays are finished to quit program.
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupt exception.");
            e.printStackTrace();
        }
    }
}
