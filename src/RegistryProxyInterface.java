import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/*
This interface details the remote methods of the RegistryProxy object, allowing it to serve its function
 */
public interface RegistryProxyInterface extends Remote {

    void proxyBind(String id, Remote obj) throws RemoteException;
    void proxyUnbind(String id) throws RemoteException, NotBoundException;
}
