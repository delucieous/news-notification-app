import java.rmi.Remote;
import java.rmi.RemoteException;

/*
Defines the methods of NotificationSink which are accessible to remote calls.
 */
public interface NotificationSinkInterface extends Remote {

    boolean notifyOfEvent(Notification<? extends NotifiableEvent> notification) throws RemoteException;
    String getID() throws RemoteException;
}
