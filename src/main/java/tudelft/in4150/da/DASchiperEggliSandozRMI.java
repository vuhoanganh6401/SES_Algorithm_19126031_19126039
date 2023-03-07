package tudelft.in4150.da;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for Remote Method Invocation (RMI) using the Schiper-Eggli-Sandoz algorithm
 * for point to point causal message ordering.
 *
 * @throws RemoteException
 */
public interface DASchiperEggliSandozRMI extends Remote {

    void receive(int receiver, Message message) throws RemoteException;
}
