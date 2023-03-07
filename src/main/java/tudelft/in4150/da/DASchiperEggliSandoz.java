package tudelft.in4150.da;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Algorithms main class that implements the RMI interface and provides additonal functionality for bootstraping
 * processes and servers.
 */
public class DASchiperEggliSandoz extends UnicastRemoteObject implements DASchiperEggliSandozRMI, Runnable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LogManager.getLogger(DASchiperEggliSandoz.class);
    private int id;
    private int port;
    private VectorClock vectorClock;
    private final Map<Integer, Message> messageBuffer;
    private final Map<Integer, VectorClock> localBuffer;

    /**
     * Constructor of a single process with an identifier and a port to bind to.
     *
     * @param pid
     * @param port
     * @throws RemoteException
     */
    public DASchiperEggliSandoz(int pid, int port) throws RemoteException {

        this.id = pid;
        this.port = port;
        localBuffer = new HashMap<Integer, VectorClock>();
        messageBuffer = new HashMap<Integer, Message>();

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", port);
            LOGGER.debug("Binding process " + id + " to port " + port);
            registry.bind("process-" + id, this);
        } catch (RemoteException e) {
            LOGGER.error("Remote exception when binding process " + id);
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            LOGGER.error(id + "already bound to registry on port " + port);
            e.printStackTrace();
        }
    }

    /**
     * Initialize the RMI registry.
     *
     * @param port Port on which RMI registry is created.
     */
    public static void initRegistry(int port) {
        // Setup RMI registry
        try {
            java.rmi.registry.LocateRegistry.createRegistry(port);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // Setup security manager.
//        if (System.getSecurityManager() == null) {
//            System.setSecurityManager(new SecurityManager());
//        }
    }

    /**
     * Creates an array of processes to be used for message exchanging.
     *
     * @param numProcesses
     * @param port
     * @return DASchiperEggliSandoz[]
     */
    public static DASchiperEggliSandoz[] createProcesses(int numProcesses, int port) {
        DASchiperEggliSandoz[] processes = new DASchiperEggliSandoz[numProcesses];

        for (int i = 0; i < numProcesses; i++) {
            try {
                LOGGER.debug("Starting thread for process " + (i + 1));
                processes[i] = new DASchiperEggliSandoz(i + 1, port);
                processes[i].vectorClock = new VectorClock(numProcesses);
            } catch (RemoteException e) {
                LOGGER.error("Remote exception creating RMI instance.");
                e.printStackTrace();
            }
        }

        return processes;
    }

    /**
     * Send a message to a receiver over the RMI interface, with or without delay.
     *
     * @param receiver
     * @param message
     * @param delay
     * @throws RemoteException
     */
    public synchronized void send(int receiver, Message message, int delay) throws RemoteException {
        Registry registry = LocateRegistry.getRegistry(port);

        try {
            DASchiperEggliSandozRMI stub = (DASchiperEggliSandozRMI) registry.lookup("process-" + receiver);


            vectorClock.incClock(id);
            message.setTimestamp(vectorClock);
            message.setBuffer(localBuffer);

            if (delay > 0) {
                LOGGER.info(this.id + " sending message " + message + " to " + receiver + " with delay "
                    + delay + "ms");

                // Make copy of message before creating thread to avoid current thread overwriting the contents.
                Message messageCopy = new Message(message);
                Thread thread = new Thread(() -> run(stub, id, messageCopy, delay));
                thread.start();
            } else {
                LOGGER.info(this.id + " sending message to " + receiver + " " + message);
                stub.receive(id, message);
            }

            // Construct pair of id and timestamp to add to own local localBuffer.
            // Using a copy of VectorClock as it is passed by reference into hashmap.
            VectorClock bufferTimestamp = new VectorClock(vectorClock);
            addBuffer(receiver, bufferTimestamp);
        } catch (NotBoundException e) {
            LOGGER.error("Unable to locate process " + receiver);
            e.printStackTrace();
        }
    }

    /**
     * Add a timestamp and associated process identifier to a processes' local buffer.
     *
     * @param processID
     * @param bufferTimestamp
     */
    private synchronized void addBuffer(int processID, VectorClock bufferTimestamp) {

        if (localBuffer.containsKey(processID)) {
            VectorClock element = localBuffer.get(processID);
            element.setMax(bufferTimestamp); // element is used by ref, hence no need to place it into hasmap again.
        } else {
            localBuffer.put(processID, bufferTimestamp);
        }
    }

    /**
     * Implementing the RMI interface function of receiving a message from another process.
     *
     * @param sender
     * @param message
     * @throws RemoteException
     */
    public synchronized void receive(int sender, Message message) throws RemoteException {
        LOGGER.info(this.id + " received message " + message.toString() + " from " + sender);

        if (deliveryCondition(message)) {
            deliver(sender, message);
            checkMessageBuffer();
        } else {
            LOGGER.info("Delivery condition not met, adding message to localBuffer.");
            messageBuffer.put(sender, message);
        }
    }

    private synchronized void checkMessageBuffer() {
        LOGGER.info("Checking message buffer.");
        for (Map.Entry<Integer, Message> buffer : messageBuffer.entrySet()) {
            Message message = buffer.getValue();
            if (deliveryCondition(message)) {
                deliver(buffer.getKey(), message);
                messageBuffer.remove(buffer.getKey());
            }
        }
    }

    /**
     * Delivery condition met if there does not exist a vector clock of the receiving process in the message
     * localBuffer, or there exists a vector clock of the receiving process in the message localBuffer and its local
     * localBuffer >= the clock in the message localBuffer.
     *
     * @param message
     * @return
     */
    private synchronized boolean deliveryCondition(Message message) {
        if (!message.getBuffer().containsKey(id)
            || vectorClock.greaterEqual(message.getBuffer().get(id))) {
            return true;
        }
        return false;
    }

    /**
     * Deliver the actual message by adding the buffer from the message into own and adjusting own VectorClock.
     *
     * @param message
     */
    private synchronized void deliver(int sender, Message message) {
        LOGGER.info("Delivery condition met, delivering message from sender " + sender + " to " + id);

        for (Map.Entry<Integer, VectorClock> buffer : message.getBuffer().entrySet()) {
            if (buffer.getKey() != id) {
                addBuffer(buffer.getKey(), buffer.getValue());
            }
        }

        vectorClock = new VectorClock(message.getTimestamp());
        vectorClock.incClock(id); // Increase own clock since message was delivered.
    }

    /**
     * Helper function to retrieve processes' identifier.
     *
     * @return int
     */
    public synchronized int getId() {
        return this.id;
    }

    @Override
    public void run() {
        LOGGER.debug("Starting thread");
    }

    /**
     * Runnable function for thread to deliver delayed message by sleeping delay time before calling rmi function.
     *
     * @param stub
     * @param sender
     * @param message
     * @param delay
     */
    public void run(DASchiperEggliSandozRMI stub, int sender, Message message, int delay) {
        LOGGER.debug("Starting Runnable");

        try {
            Thread.sleep(delay);
            stub.receive(sender, message);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted thread during delay simulation.");
            e.printStackTrace();
        } catch (RemoteException e) {
            LOGGER.error("Remote exception when sending message " + message);
            e.printStackTrace();
        }
    }

    /**
     * Helper function to retrieve the messageBuffer for unit testing.
     *
     * @return
     */
    public Map<Integer, Message> getMessageBuffer() {
        return messageBuffer;
    }
}
