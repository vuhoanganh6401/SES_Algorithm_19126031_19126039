package tudelft.in4150.da;

import java.rmi.RemoteException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * First unit tests for Schiper-Eggli-Sandoz algorithm implementation.
 * NOTE: process arrays start at index 0 but containsKey() starts at index 1 in assert tests.
 */
@SuppressWarnings("checkstyle:magicnumber")
class MainTest {
    private static final Logger LOGGER = LogManager.getLogger(DASchiperEggliSandoz.class);
    private static final int PORT = 1098;

    @BeforeAll
    private static void setup() {
        System.setProperty("java.security.policy", "java.policy");
        DASchiperEggliSandoz.initRegistry(PORT);
    }

    /**
     * Simple test with 2 processes (P1 and P2).
     * P1 sends m1 -> P2 with delay of 2000ms.
     * P1 sends m2 -> P2 with no delay.
     */
    @Test
    public void simpleTest() {
        final int numProcesses = 2;
        final int delay = 2000;

        DASchiperEggliSandoz[] processes = DASchiperEggliSandoz.createProcesses(numProcesses, PORT);

        try {
            processes[0].send(processes[1].getId(), new Message(numProcesses), delay);
            processes[0].send(processes[1].getId(), new Message(numProcesses), 0);

            // P2 should buffer m2 as its missing m1, hence key of P1 exists in messageBuffer.
            assertTrue(processes[1].getMessageBuffer().containsKey(1));
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
