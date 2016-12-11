import javax.swing.*;
import java.net.ConnectException;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Created by marro on 10/12/2016.
 */
public class NewsTickerClient {

    private ClientGUI gui;
    private NotificationSink sink;
    private ArrayList<NewsEvent> newsEvents;

    public NewsTickerClient() {
        newsEvents = new ArrayList<>();
    }

    public void start() {
        try {
            ArrayList<Topic> topics = NotificationFramework.getSources();
            this.sink = new NotificationSink();
            this.gui = new ClientGUI(this, topics);
            SwingUtilities.invokeLater(this.gui::initialise);
            for (Topic topic: topics) {
                sink.registerToSource(topic);
            }

            new Thread(() -> {
                while(true) {
                    System.out.println("take");
                    NotifiableEvent event = sink.takeNotification();
                    this.handleNotification(event);
                }
            }).start();

        } catch (ConnectException | RemoteException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Connection Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void registerToSource(Topic topic) throws ConnectException {

    }

    public void handleNotification(NotifiableEvent event) {
        if (event instanceof NewsEvent) {
            NewsEvent newsEvent = (NewsEvent) event;
            newsEvents.add(newsEvent);
            SwingUtilities.invokeLater(() -> this.gui.displayNewsEvent(newsEvent));
        }
    }
}
