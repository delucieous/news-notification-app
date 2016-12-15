import javax.swing.*;
import java.io.*;
import java.net.ConnectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.stream.Collectors;

/*
This class manages the interaction between the GUI and the framework, providing all the controllers and logic
 */
public class NewsTickerClient {

    private ClientGUI gui;
    private NotificationSink sink;
    private ArrayList<NewsEvent> newsEvents;
    private File subsFile;

    public NewsTickerClient() {
        newsEvents = new ArrayList<>();
        this.subsFile = new File("subsFile.dat"); //The file which current subscriptions are saved in
    }

    /*
    This method is called on application startup and initialises all the threads necessary to run the application
     */
    public void start() {
        try {
            ArrayList<Topic> topics = NotificationFramework.getSources();
            ArrayList<Topic> subbedTopics = this.getSavedSubs();

            //This one liner subtracts the subscribed topics from the available topics, giving us a list of topics
            //which can still be subscribed to
            topics = topics.stream().filter(topic -> !subbedTopics.contains(topic)).collect(Collectors.toCollection(ArrayList::new));
            this.sink = new NotificationSink();
            this.gui = new ClientGUI(this, topics, subbedTopics);
            SwingUtilities.invokeLater(this.gui::initialise);

            //Attempt to connect to the saved topics
            for (Topic topic: subbedTopics) {
                sink.registerToSource(topic);
            }

            //Start a thread which is responsible for taking notifications from the sink queue
            new Thread(() -> {
                while(true) {
                    NotifiableEvent event = sink.takeNotification();
                    this.handleNotification(event);
                }
            }).start();

        } catch (ConnectException | RemoteException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Connection Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    //Registers the sink to the source associated to the given topic
    public void subscribeToTopic(Topic topic) throws ConnectException {
        sink.registerToSource(topic);
        saveSub(topic);
    }

    //Unregisters the sink from the source associated to the given topic
    public void unsubscribeFromTopic(Topic topic) throws ConnectException {
        sink.unsubscribe(topic);
        removeSub(topic);
    }

    //This gets all the currently available sources should this have changed during the running of the application
    //Get the full list of topics and take away the ones we were already subscribed to
    public void refreshTopics() {
        try {
            ArrayList<Topic> newTopics = NotificationFramework.getSources();
            ArrayList<Topic> subbedTopics = this.getSavedSubs();
            ArrayList<Topic> newTopicsFiltered = newTopics.stream().filter(topic -> !subbedTopics.contains(topic)).collect(Collectors.toCollection(ArrayList::new));
            SwingUtilities.invokeLater(() -> gui.displayNewTopics(newTopicsFiltered));

        } catch (ConnectException e) {
            JOptionPane.showMessageDialog(gui, "Could not connect to the registry.", "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Passes the incoming notification's event to the GUI to be displayed
    public void handleNotification(NotifiableEvent event) {
        if (event instanceof NewsEvent) {
            NewsEvent newsEvent = (NewsEvent) event;
            newsEvents.add(newsEvent);
            SwingUtilities.invokeLater(() -> this.gui.displayNewsEvent(newsEvent));
        }
    }

    //Saves a new subscription to the saved file
    private void saveSub(Topic topic) {
        ArrayList<Topic> currentSubs = getSavedSubs();
        currentSubs.add(topic);
        reWriteSubsFile(currentSubs);
    }

    //Removes a subscription from the saved file
    private void removeSub(Topic topic) {
        ArrayList<Topic> currentSubs = getSavedSubs();
        currentSubs.remove(topic);
        reWriteSubsFile(currentSubs);
    }

    //Wipe the subs file - used in the rewriting
    private void wipeSubsFile() {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(subsFile); //This wipes the file
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    //This function performs the work of writing the given ArrayList to the saved file after wiping it
    private void reWriteSubsFile(ArrayList<Topic> currentSubs) {
        this.wipeSubsFile();
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(subsFile))) {
            oos.writeObject(currentSubs);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Subscription file is missing or corrupted.", "File error.", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Get the client's currently saved subscriptions
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

