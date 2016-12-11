import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotificationSinkInterface extends Remote {

    boolean notifyOfEvent(Notification<? extends NotifiableEvent> notification) throws RemoteException;
    String getID() throws RemoteException;
}
