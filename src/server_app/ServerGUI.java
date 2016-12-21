package server_app;

import notification_framework.Topic;
import shared_elements.NewsEvent;
import shared_elements.NewsEventPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
This class defines the GUI of the Server application
 */
public class ServerGUI extends JFrame {

    private NewsTickerServer server;
    private ArrayList<Topic> topics;
    private ExecutorService workerThreadPool;
    private JPanel newsEventShowerPanel;
    private JComboBox<Topic> topicsToAdvertise;
    private JComboBox<Topic> topicsToNotify;
    private JButton notifyButton;

    public ServerGUI(NewsTickerServer server, ArrayList<Topic> topics) {
        this.server = server;
        this.topics = topics;
        workerThreadPool = Executors.newFixedThreadPool(4);
    }

    public void initialise() {

        try {
            for (UIManager.LookAndFeelInfo info: UIManager.getInstalledLookAndFeels()) {
                if (info.getName().equals("Windows")) {
                    UIManager.setLookAndFeel(info.getClassName());
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        //Set the main frame up
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(new Dimension(1200, 600));
        this.setLocationRelativeTo(null);

        JPanel contentPane = new JPanel();
        this.setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        Topic[] topicData = topics.stream().toArray(Topic[]::new);
        topicsToAdvertise = new JComboBox<>(topicData);
        topicsToNotify = new JComboBox<>();

        JButton advertiseButton = new JButton("Advertise");
        JButton fetchButton = new JButton("Fetch Story");

        topPanel.add(topicsToAdvertise);
        topPanel.add(advertiseButton);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(topicsToNotify);
        topPanel.add(fetchButton);

        //Listeners to allow the user to advertise the chosen topic and fetch a news story for the selected topic
        advertiseButton.addActionListener((e) -> workerThreadPool.submit(() -> server.advertiseSource((Topic) topicsToAdvertise.getSelectedItem())));
        fetchButton.addActionListener((e) -> workerThreadPool.submit(() -> server.getStoryFromTopic((Topic) topicsToNotify.getSelectedItem())));


        JPanel storyPanel = new JPanel();
        storyPanel.setLayout(new BorderLayout());


        newsEventShowerPanel = new JPanel();
        newsEventShowerPanel.setLayout(new BorderLayout());

        storyPanel.add(newsEventShowerPanel, BorderLayout.CENTER);

        notifyButton = new JButton("Notify");
        notifyButton.setEnabled(false);
        //Listener to allow the user to publish the currently displayed event
        notifyButton.addActionListener((e) -> server.publishEvent());

        storyPanel.add(notifyButton, BorderLayout.SOUTH);

        contentPane.add(storyPanel, BorderLayout.CENTER);
        contentPane.add(topPanel, BorderLayout.NORTH);

        this.setVisible(true);

    }

    //Displays the latest event on a NewsEventPanel
    public void displayLatestEvent(NewsEvent latestEvent) {
        newsEventShowerPanel.removeAll();
        NewsEventPanel newPanel = new NewsEventPanel(latestEvent);
        newsEventShowerPanel.add(newPanel, BorderLayout.CENTER);
        notifyButton.setEnabled(true);

        revalidate();
        repaint();
    }

    //Removes a topic from the combobox of topics available to advertise once it has been done
    public void removeAdvertisedFromComboBox(Topic topic) {
        topicsToAdvertise.removeItem(topic);
        topicsToNotify.addItem(topic);

        repaint();
    }
}
