import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by marro on 10/12/2016.
 */
public class ClientGUI extends JFrame {

    private ArrayList<Topic> topics;
    private NewsTickerClient client;

    public ClientGUI(NewsTickerClient client, ArrayList<Topic> topics) {
        this.topics = topics;
        this.client = client;
    }

    public void initialise() {
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(1200, 1300);
        this.setLocationRelativeTo(null);

        JPanel contentPane = new JPanel();
        this.setContentPane(contentPane);
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        JPanel topPanel = new JPanel();
        topPanel.setBackground(Color.cyan);
        topPanel.setLayout(new BorderLayout());

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new FlowLayout());
        topPanel.add(settingsPanel, BorderLayout.NORTH);

        Topic[] topicData = topics.stream().toArray(Topic[]::new);
        JComboBox<Topic> topicSelect = new JComboBox<>(topicData);
        settingsPanel.add(topicSelect);

        JButton subscribeButton = new JButton("Subscribe");
        settingsPanel.add(subscribeButton);
        contentPane.add(topPanel);

        JButton unsubscribeButton = new JButton("Unsubscribe");
        settingsPanel.add(unsubscribeButton);

        TickerPanel tickerPanel = new TickerPanel("Welcome to the News Ticker!", 2);
        topPanel.add(tickerPanel, BorderLayout.CENTER);

        subscribeButton.addActionListener((e) -> tickerPanel.launchLoop());
        unsubscribeButton.addActionListener((e) -> tickerPanel.addNotificationString(UUID.randomUUID().toString()));

        //Feed panel

        JScrollPane feedPanel = new JScrollPane();
        feedPanel.setBackground(Color.green);
        feedPanel.setPreferredSize(new Dimension(1200, 800));
        contentPane.add(feedPanel);

        JPanel panelToScroll = new JPanel();
        panelToScroll.setLayout(new BoxLayout(panelToScroll, BoxLayout.Y_AXIS));
        feedPanel.setViewportView(panelToScroll);


        for (Topic topic: topics) {
            JPanel p = new JPanel();
            JLabel lab = new JLabel(topic.getCode());
            p.add(lab, BorderLayout.CENTER);
            p.setPreferredSize(new Dimension(1100, 200));
            p.setBorder(new LineBorder(Color.BLACK, 5));
            panelToScroll.add(p);
            panelToScroll.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        this.setVisible(true);
        tickerPanel.launchLoop();
    }
}
