import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by marro on 09/12/2016.
 */
public class NotificationFramework {

    public static String hostname;

    private NotificationFramework() {}

    public static void initialize(String hostname) {
        NotificationFramework.hostname = "rmi://" + hostname + "/";
    }

    public static ArrayList<Topic> getSources() throws ConnectException{
        ArrayList<Topic> topics = new ArrayList<>();
        try {
            String[] sourceNames = Naming.list(hostname);
            for (String name: sourceNames) {
                if (!name.contains("sink") && !name.contains("regProxy")) {
                    NotificationSourceInterface source = (NotificationSourceInterface) Naming.lookup(name);
                    topics.add(source.getTopic());
                }
            }
        } catch (RemoteException e) {
            throw new ConnectException("Could not connect to Registry - it is either offline or unreachable.");
        } catch (MalformedURLException e) {
            throw new ConnectException("Registry does not exist at this hostname");
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

        return topics;
    }

    public static String generateSinkID() {
        File f = new File("framework_client_config.dat");
        String id;
        try {
            if (!f.exists()) {
                UUID uuid = UUID.randomUUID(); //UUID has infinitely low collision chance so we do not check
                id = "sink:" + uuid.toString();
                Files.write(Paths.get(f.getPath()), id.getBytes());
            }
            else {
                id = Files.readAllLines(Paths.get(f.getPath())).get(0);
            }
        }
        catch (IOException ie) {
            return "";
        }
        return id;
    }
}
