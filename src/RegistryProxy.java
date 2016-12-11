import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by marro on 09/12/2016.
 */
public class RegistryProxy extends UnicastRemoteObject implements RegistryProxyInterface {

    private Registry reg;

    public RegistryProxy(Registry registry) throws RemoteException{
        super();
        this.reg = registry;
    }

    @Override
    public void proxyBind(String id, Remote obj) throws RemoteException {
        reg.rebind(id, obj);
    }

    @Override
    public void proxyUnbind(String id) throws RemoteException, NotBoundException {
        reg.unbind(id);
    }
}
