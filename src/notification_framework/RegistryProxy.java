package notification_framework;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/*
This remote object allows any machine to bind objects to the RMI Registry, even if they are not local to it
 */
public class RegistryProxy extends UnicastRemoteObject implements RegistryProxyInterface {

    private Registry reg;

    public RegistryProxy(Registry registry) throws RemoteException{
        super();
        this.reg = registry;
    }

    //Bind the given object to the registry with the given ID
    @Override
    public void proxyBind(String id, Remote obj) throws RemoteException {
        reg.rebind(id, obj);
    }

    //Unbind the object with the given ID from the registry
    @Override
    public void proxyUnbind(String id) throws RemoteException, NotBoundException {
        reg.unbind(id);
    }
}
