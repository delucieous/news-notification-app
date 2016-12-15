import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/*
This class is responsible for starting the registry and providing access to it via the RegistryProxy remote object
 */
public class RegistryManager {

    private static Registry reg;

    public static void main(String[] args) {
        try {
            reg = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            RegistryProxy proxy = new RegistryProxy(reg);
            reg.rebind("regProxy", proxy);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
