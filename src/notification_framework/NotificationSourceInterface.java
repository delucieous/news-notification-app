package notification_framework;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by marro on 06/12/2016.
 */
public interface NotificationSourceInterface extends Remote {

    void register(NotificationSinkInterface sink) throws RemoteException;
    void unregister(String id) throws RemoteException;
    Topic getTopic() throws RemoteException;
}
