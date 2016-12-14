import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by marro on 12/12/2016.
 */
public class ServerGUI extends JFrame {

    private NewsTickerServer server;
    private ArrayList<Topic> topics;
    private ExecutorService workerThreadPoll;
    private JPanel newsEventShowerPanel;
    private JComboBox<Topic> topicsToAdvertise;
    private JComboBox<Topic> topicsToNotify;
    private JButton notifyButton;

    public ServerGUI(NewsTickerServer server, ArrayList<Topic> topics) {
        this.server = server;
        this.topics = topics;
        workerThreadPoll = Executors.newFixedThreadPool(4);
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

        advertiseButton.addActionListener((e) -> workerThreadPoll.submit(() -> server.advertiseSource((Topic) topicsToAdvertise.getSelectedItem())));
        fetchButton.addActionListener((e) -> workerThreadPoll.submit(() -> server.getStoryFromTopic((Topic) topicsToNotify.getSelectedItem())));


        JPanel storyPanel = new JPanel();
        storyPanel.setLayout(new BorderLayout());


        newsEventShowerPanel = new JPanel();
        newsEventShowerPanel.setLayout(new BorderLayout());

        try {
            NewsEventPanel nPanel = new NewsEventPanel(new NewsEvent(new Topic("business"), new URI("http://joshuamarron.com"), "some preview lol lol", "Extra Extra William Henry Greedy is a tit"));
            newsEventShowerPanel.add(nPanel);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        storyPanel.add(newsEventShowerPanel, BorderLayout.CENTER);

        notifyButton = new JButton("Notify");
        notifyButton.setEnabled(false);
        notifyButton.addActionListener((e) -> server.publishEvent());

        storyPanel.add(notifyButton, BorderLayout.SOUTH);

        contentPane.add(storyPanel, BorderLayout.CENTER);
        contentPane.add(topPanel, BorderLayout.NORTH);

        this.setVisible(true);

    }

    public void displayLatestEvent(NewsEvent latestEvent) {
        System.out.println("called");
        newsEventShowerPanel.removeAll();
        NewsEventPanel newPanel = new NewsEventPanel(latestEvent);
        newsEventShowerPanel.add(newPanel, BorderLayout.CENTER);
        notifyButton.setEnabled(true);

        revalidate();
        repaint();
    }

    public void removeAdvertisedFromComboBox(Topic topic) {
        topicsToAdvertise.removeItem(topic);
        topicsToNotify.addItem(topic);

        repaint();
    }
}
