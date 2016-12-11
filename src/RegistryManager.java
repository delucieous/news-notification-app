import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Created by marro on 09/12/2016.
 */
public class RegistryManager {

    static Registry reg;

    public static void main(String[] args) {
        try {
            reg = LocateRegistry.createRegistry(1099);
            RegistryProxy proxy = new RegistryProxy(reg);
            reg.rebind("regProxy", proxy);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
