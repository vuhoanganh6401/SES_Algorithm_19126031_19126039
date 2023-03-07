package tudelft.in4150.da;

import java.io.Serializable;
import java.util.Arrays;

/**
 * VectorClock class which is used as the Vector Clock for each of the processes and the timestamps in messages.
 */
public class VectorClock implements Serializable {
    private static final long serialVersionUID = 1L;
    private int[] clocks;

    /**
     * VectorClock constructor, initialize clock with 0s on VectroClock construction.
     *
     * @param processes
     */
    public VectorClock(int processes) {
        clocks = new int[processes];
        Arrays.fill(clocks, 0);
    }

    /**
     * VectorClock copy constructor.
     *
     * @param clock
     */
    public VectorClock(VectorClock clock) {
        clocks = new int[clock.clocks.length];
        for (int i = 0; i < clock.clocks.length; i++) {
            this.clocks[i] = clock.clocks[i];
        }
    }

    /**
     * Format VectorClock to printable String output.
     *
     * @return String
     */
    @Override
    public String toString() {
        String clock = Arrays.toString(clocks);
        String formattedClock = '(' + clock.substring(1, clock.length() - 1) + ')';
        return formattedClock;
    }

    /**
     * Increment own VectorClock in event of delivering or sending message.
     *
     * @param id
     */
    public void incClock(int id) {
        clocks[id - 1]++;
    }

    /**
     * Check if own VectorClock is greater or equal to the timestamp.
     *
     * @param timestamp
     * @return boolean
     */
    public boolean greaterEqual(VectorClock timestamp) {
        for (int i = 0; i < clocks.length; i++) {
            if (clocks[i] < timestamp.clocks[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compare own VectorClock with timestamp and set own clock to maximum values of both clocks.
     *
     * @param bufferTimestamp
     */
    public void setMax(VectorClock bufferTimestamp) {
        for (int i = 0; i < clocks.length; i++) {
            if (clocks[i] < bufferTimestamp.clocks[i]) {
                clocks[i] = bufferTimestamp.clocks[i];
            }
        }
    }
}
