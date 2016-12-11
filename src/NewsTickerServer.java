import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by marro on 11/12/2016.
 */
public class NewsTickerServer {

    public static void main(String[] args) {

        HashMap<String, NotificationSource> sources = new HashMap<>();
        ArrayList<Topic> registeredTopics = new ArrayList<>();
        Random rand = new Random();

        try {
            RegistryProxyInterface regProxy = (RegistryProxyInterface) Naming.lookup("regProxy");
            for (NewsTopics nt: NewsTopics.values()) {
                Topic topic = nt.getTopic();
                registeredTopics.add(topic);
                NotificationSource source = new NotificationSource(topic, regProxy);
                regProxy.proxyBind(topic.getCode(), source);
                sources.put(topic.getCode(), source);
            }

            while (true) {
                Thread.sleep(5000);
                Topic randTopic = registeredTopics.get(rand.nextInt(registeredTopics.size()));
                NewsEvent randEvent = new NewsEvent(randTopic, new URI("http://joshuamarron.com"), "some text here " + randTopic.getCode(), "Some Things Happened Today in " + randTopic.toString() + "!");
                Notification<NewsEvent> not = new Notification<>(randEvent);
                sources.get(randTopic.getCode()).publishToSource(not);
                System.out.println("sent" + not.toString());
            }

        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

}
