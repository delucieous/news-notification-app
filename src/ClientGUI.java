import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by marro on 10/12/2016.
 */
public class ClientGUI extends JFrame {

    private ArrayList<Topic> topics;
    private ArrayList<Topic> subbedTopics;
    private NewsTickerClient client;
    private TreeSet<NewsEventPanel> newsPanels;
    private JPanel eventsListPanel;
    private TickerPanel tickerPanel;
    private ScheduledExecutorService tickerRefresh = Executors.newScheduledThreadPool(1);
    private ExecutorService helperThreads = Executors.newFixedThreadPool(4);

    public ClientGUI(NewsTickerClient client, ArrayList<Topic> topics, ArrayList<Topic> subbedTopics) {
        this.topics = topics;
        this.subbedTopics = subbedTopics;
        this.client = client;
        newsPanels = new TreeSet<>();
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

        // ==Set up window==
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(1200, 1300);
        this.setLocationRelativeTo(null);

        JPanel contentPane = new JPanel();
        this.setContentPane(contentPane);
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        //==topPanel contains the ticker and settings==
        JPanel topPanel = new JPanel();
        topPanel.setBackground(Color.cyan);
        topPanel.setLayout(new BorderLayout());

        //==settingsPanel for subscribe/unsubscribe==
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new FlowLayout());
        topPanel.add(settingsPanel, BorderLayout.NORTH);

        Topic[] topicData = topics.stream().toArray(Topic[]::new);
        Topic[] subbedTopicsData = subbedTopics.stream().toArray(Topic[]::new);
        JComboBox<Topic> topicSelect = new JComboBox<>(topicData);
        JComboBox<Topic> unsubSelect = new JComboBox<>(subbedTopicsData);

        JButton subscribeButton = new JButton("Subscribe");
        JButton unsubscribeButton = new JButton("Unsubscribe");

        settingsPanel.add(subscribeButton);
        settingsPanel.add(topicSelect);
        settingsPanel.add(unsubSelect);
        settingsPanel.add(unsubscribeButton);

        contentPane.add(topPanel);

        //==Add the ticker panel==
        tickerPanel = new TickerPanel("Welcome to the News Ticker!", 2);
        topPanel.add(tickerPanel, BorderLayout.CENTER);

        subscribeButton.addActionListener((e) -> helperThreads.submit(() -> {
                try {
                    Topic selected = (Topic) topicSelect.getSelectedItem();
                    client.subscribeToTopic(selected);
                    SwingUtilities.invokeLater(() -> {
                        topicSelect.removeItemAt(topicSelect.getSelectedIndex());
                        unsubSelect.addItem(selected);
                    });
                } catch (ConnectException e1) {
                    JOptionPane.showMessageDialog(this, "Could not connect to source for the desired topic, please try again later", "Connection Error", JOptionPane.ERROR_MESSAGE);
                }
            }));

        unsubscribeButton.addActionListener((e) -> helperThreads.submit(() -> {
            try {
                Topic selected = (Topic) unsubSelect.getSelectedItem();
                client.unsubscribeFromTopic(selected);
                SwingUtilities.invokeLater(() -> {
                    unsubSelect.removeItemAt(unsubSelect.getSelectedIndex());
                    topicSelect.addItem(selected);
                });
            } catch (ConnectException ce) {
                JOptionPane.showMessageDialog(this, "Could not connect to source for the desired topic, please try again later", "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        }));

        //=====Bottom half of the gui=====

        JScrollPane feedPanel = new JScrollPane();
        feedPanel.setBackground(Color.green);
        feedPanel.setPreferredSize(new Dimension(1200, 800));
        feedPanel.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        contentPane.add(feedPanel);

        eventsListPanel = new JPanel();
        eventsListPanel.setLayout(new BoxLayout(eventsListPanel, BoxLayout.Y_AXIS));
        feedPanel.setViewportView(eventsListPanel);

        this.setVisible(true);
        tickerPanel.launchLoop();
        tickerRefresh.scheduleAtFixedRate(tickerPanel::launchLoop, 1, 1, TimeUnit.MINUTES);
    }

    public void displayNewsEvent(NewsEvent event) {
        NewsEventPanel panel = new NewsEventPanel(event);
        panel.addMouseListener(new NewsEventPanelListener(panel));
        panel.articleDesc.addMouseListener(new NewsEventPanelListener(panel)); //So child triggers too
        newsPanels.add(panel);

        eventsListPanel.removeAll();
        for (NewsEventPanel p: newsPanels) {
            addToEventsListPanel(p);
        }

        tickerPanel.addNotificationString(event.getHeadline());

        revalidate();
        repaint();
    }

    private void addToEventsListPanel(NewsEventPanel ePanel) {
        ePanel.setPreferredSize(new Dimension(1100, 200));
        ePanel.setBorder(new LineBorder(Color.BLACK, 5));
        eventsListPanel.add(ePanel);
        eventsListPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    private class NewsEventPanelListener extends MouseAdapter {

        private NewsEventPanel panel;

        public NewsEventPanelListener(NewsEventPanel panel) {
            this.panel = panel;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            try {
                Desktop.getDesktop().browse(panel.link);
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(ClientGUI.this, e1.getMessage(), "Could not open link", JOptionPane.ERROR_MESSAGE);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {

            float[] tempColorHSB = Color.RGBtoHSB(panel.bgColor.getRed(), panel.bgColor.getGreen(), panel.bgColor.getBlue(), null);
            Color tempColor = Color.getHSBColor(tempColorHSB[0], tempColorHSB[1], tempColorHSB[2] + 0.2f);
            tempColor = new Color(tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), 0);

            float[] tempTextColorHSB = Color.RGBtoHSB(panel.textBgColor.getRed(), panel.textBgColor.getGreen(), panel.textBgColor.getBlue(), null);
            Color tempTextColor = Color.getHSBColor(tempTextColorHSB[0], tempTextColorHSB[1], tempTextColorHSB[2] + 0.5f);
            tempTextColor = new Color(tempTextColor.getRed(), tempTextColor.getGreen(), tempTextColor.getBlue(), 0);

            panel.topPanel.setBackground(tempColor);
            panel.articleDesc.setBackground(tempTextColor);

            repaint();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            panel.topPanel.setBackground(panel.bgColor);
            panel.articleDesc.setBackground(panel.textBgColor);

            repaint();
        }
    }
}
