package tudelft.in4150.da;

import java.rmi.RemoteException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Third tests for Schiper-Eggli-Sandoz algorithm implementation.
 * NOTE: process arrays start at index 0 but containsKey() starts at index 1 in assert tests.
 */
@SuppressWarnings("checkstyle:magicnumber")
public class ThirdTest {
    private static final Logger LOGGER = LogManager.getLogger(DASchiperEggliSandoz.class);
    private static final int PORT = 1098;

    @BeforeAll
    private static void setup() {
        System.setProperty("java.security.policy", "java.policy");
        DASchiperEggliSandoz.initRegistry(PORT);
    }

    /**
     * More advanced test with 4 processes (P1, P2, P3, and P4).
     * P1 sends m1 -> P4 with 2000ms delay.
     * P1 sends m2 -> P3 with 2000ms delay.
     * P1 sends m3 -> P2 with no delay.
     * P2 sends m4 -> P4 with no delay.
     * P2 sends m5 -> P3 with no delay.
     */
    @Test
    public void testAdvanced() {
        final int numProcesses = 4;
        final int delay = 2000;

        DASchiperEggliSandoz[] processes = DASchiperEggliSandoz.createProcesses(numProcesses, PORT);

        try {
            processes[0].send(processes[3].getId(), new Message(numProcesses), delay);
            processes[0].send(processes[2].getId(), new Message(numProcesses), delay);
            processes[0].send(processes[1].getId(), new Message(numProcesses), 0);
            processes[1].send(processes[3].getId(), new Message(numProcesses), 0);

            // P4 should buffer m4 as its missing m1, hence key of P2 exists in messageBuffer of P4.
            assertTrue(processes[3].getMessageBuffer().containsKey(2));

            processes[1].send(processes[2].getId(), new Message(numProcesses), 0);

            // P3 should buffer m5 as its missing m2, hence key of P2 exists in messageBuffer of P3.
            assertTrue(processes[2].getMessageBuffer().containsKey(2));

            Thread.sleep(delay);

            // After delay, recheck P3 messageBuffer to ensure it deliverd m5 messages after receiving m2.
            assertFalse(processes[2].getMessageBuffer().containsKey(2));


        } catch (RemoteException | InterruptedException e) {
            LOGGER.error("Exception sending messages.");
            e.printStackTrace();
        }
    }
}
