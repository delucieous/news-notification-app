import javax.swing.*;
import java.io.*;
import java.net.ConnectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by marro on 10/12/2016.
 */
public class NewsTickerClient {

    private ClientGUI gui;
    private NotificationSink sink;
    private ArrayList<NewsEvent> newsEvents;
    private File subsFile;

    public NewsTickerClient() {
        newsEvents = new ArrayList<>();
        this.subsFile = new File("subsFile");
    }

    public void start() {
        try {
            ArrayList<Topic> topics = NotificationFramework.getSources();
            ArrayList<Topic> subbedTopics = this.getSavedSubs();

            topics = topics.stream().filter(topic -> !subbedTopics.contains(topic)).collect(Collectors.toCollection(ArrayList::new));
            this.sink = new NotificationSink();
            this.gui = new ClientGUI(this, topics, subbedTopics);
            SwingUtilities.invokeLater(this.gui::initialise);

            for (Topic topic: subbedTopics) {
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

    public void subscribeToTopic(Topic topic) throws ConnectException {
        sink.registerToSource(topic);
        saveSub(topic);
    }

    public void unsubscribeFromTopic(Topic topic) throws ConnectException {
        sink.unsubscribe(topic);
        removeSub(topic);
    }

    public void handleNotification(NotifiableEvent event) {
        if (event instanceof NewsEvent) {
            NewsEvent newsEvent = (NewsEvent) event;
            newsEvents.add(newsEvent);
            SwingUtilities.invokeLater(() -> this.gui.displayNewsEvent(newsEvent));
        }
    }

    private void saveSub(Topic topic) {
        ArrayList<Topic> currentSubs = getSavedSubs();
        currentSubs.add(topic);
        reWriteSubsFile(currentSubs);
    }

    private void removeSub(Topic topic) {
        ArrayList<Topic> currentSubs = getSavedSubs();
        currentSubs.remove(topic);
        reWriteSubsFile(currentSubs);
    }

    private void wipeSubsFile() {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(subsFile); //This wipes the file
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void reWriteSubsFile(ArrayList<Topic> currentSubs) {
        this.wipeSubsFile();
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(subsFile))) {
            oos.writeObject(currentSubs);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Subscription file is missing or corrupted.", "File error.", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    private ArrayList<Topic> getSavedSubs() {
        ArrayList<Topic> subbedTopics = new ArrayList<>();

        if(subsFile.exists()) {
            try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(subsFile))) {
                subbedTopics = (ArrayList<Topic>) ois.readObject();
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(null, "The subs file has been deleted.", "File error", JOptionPane.ERROR_MESSAGE);
            } catch (ClassNotFoundException | IOException e) {
                JOptionPane.showMessageDialog(null, "The subs file has been corrupted, could not load your previous subscriptions.", "File error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return subbedTopics;
    }
}

