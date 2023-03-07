package tudelft.in4150.da;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Message class to construct messages to be passed between processes.
 *
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<Integer, VectorClock> buffer;
    private VectorClock timestamp;

    /**
     * Construct a message.
     *
     * @param numProcesses // Number of total processes needed for VectorClock creation
     */
    public Message(int numProcesses) {
        buffer = new HashMap<Integer, VectorClock>();
        timestamp = new VectorClock(numProcesses);
    }

    /**
     * Copy constructor.
     *
     * @param message
     */
    public Message(Message message) {
        buffer = new HashMap<Integer, VectorClock>();
        buffer.putAll(message.buffer);
        timestamp = new VectorClock(message.timestamp);
    }

    /**
     * Set the timestamp of the message from the provided VectorClock.
     *
     * @param timestamp
     */
    public void setTimestamp(VectorClock timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Copy the provided buffer from a process into the message buffer.
     *
     * @param buffer
     */
    public void setBuffer(Map<Integer, VectorClock> buffer) {
        this.buffer = buffer;
    }

    /**
     * Overriding parent String format for better readibility.
     *
     * @return String
     */
    @Override
    public String toString() {
        if (buffer == null) {
            return "{" + "- , " +  timestamp + "}";
        }

        return "{" + buffer + ", " + timestamp + "}";
    }

    /**
     * Provide the timestamp of the message.
     *
     * @return VectorClock
     */
    public VectorClock getTimestamp() {
        return timestamp;
    }

    /**
     * Helper function for unit testing to retrieve messageBuffer and check if messages are indeed buffered.
     *
     * @return Map<Integer, VectorClock>
     */
    public Map<Integer, VectorClock> getBuffer() {
        return buffer;
    }
}
