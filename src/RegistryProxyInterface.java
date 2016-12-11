import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by marro on 09/12/2016.
 */
public interface RegistryProxyInterface extends Remote {

    void proxyBind(String id, Remote obj) throws RemoteException;
    void proxyUnbind(String id) throws RemoteException, NotBoundException;
}
