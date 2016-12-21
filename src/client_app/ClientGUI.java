package client_app;

import notification_framework.Topic;
import shared_elements.NewsEvent;
import shared_elements.NewsEventPanel;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
The class which represents the GUI of the client application
 */
public class ClientGUI extends JFrame {

    private ArrayList<Topic> topics; //Topics which are available to subscribe to
    private ArrayList<Topic> subbedTopics; //Topics which are already subscribed to
    private NewsTickerClient client; //Reference to the client application
    private TreeSet<NewsEventPanel> newsPanels; //A sorted set of panels representing the notifications
    private JPanel eventsListPanel;
    private TickerPanel tickerPanel;
    //This first ExecutorService allows for the periodic refreshing of the ticker on the app
    private ScheduledExecutorService tickerRefresh = Executors.newScheduledThreadPool(1);
    private ExecutorService helperThreads = Executors.newFixedThreadPool(4);
    private JComboBox<Topic> topicSelect;

    public ClientGUI(NewsTickerClient client, ArrayList<Topic> topics, ArrayList<Topic> subbedTopics) {
        this.topics = topics;
        this.subbedTopics = subbedTopics;
        this.client = client;
        newsPanels = new TreeSet<>();
    }

    public void initialise() {

        //Try to make the program look nice
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
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.X_AXIS));
        settingsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Settings"));
        topPanel.add(settingsPanel, BorderLayout.NORTH);

        //Put the topic ArrayList into a fixed array for the combo boxes
        Topic[] topicData = topics.stream().toArray(Topic[]::new);
        Topic[] subbedTopicsData = subbedTopics.stream().toArray(Topic[]::new);
        topicSelect = new JComboBox<>(topicData);
        JComboBox<Topic> unsubSelect = new JComboBox<>(subbedTopicsData);

        JButton subscribeButton = new JButton("Subscribe");
        JButton unsubscribeButton = new JButton("Unsubscribe");
        JButton refreshTickerButton = new JButton("Refresh Ticker");
        JButton refreshTopicsButton = new JButton("Refresh Topics");

        settingsPanel.add(refreshTopicsButton);
        settingsPanel.add(topicSelect);
        settingsPanel.add(subscribeButton);
        settingsPanel.add(Box.createHorizontalGlue());
        settingsPanel.add(refreshTickerButton);
        settingsPanel.add(Box.createHorizontalGlue());
        settingsPanel.add(unsubSelect);
        settingsPanel.add(unsubscribeButton);

        contentPane.add(topPanel);

        //==Add the ticker panel==
        tickerPanel = new TickerPanel("Welcome to the News Ticker!", 2);
        topPanel.add(tickerPanel, BorderLayout.CENTER);

        //Allows the user to refresh the available topics
        refreshTopicsButton.addActionListener((e) -> helperThreads.submit(() -> {
            client.refreshTopics();
        }));

        //Allows the user to subscribe to a topic - this is done in a separate thread to avoid GUI hanging
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

        //Allows the user to unsubscribe from a topic
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

        //Allows the user to manually refresh the ticker
        refreshTickerButton.addActionListener((e) -> tickerPanel.launchLoop());

        //=====Bottom half of the gui=====

        JScrollPane feedPanel = new JScrollPane();
        feedPanel.setPreferredSize(new Dimension(1200, 800));
        feedPanel.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        feedPanel.setBorder(new TitledBorder(new EtchedBorder(), "Latest News"));
        contentPane.add(feedPanel);

        eventsListPanel = new JPanel();
        eventsListPanel.setLayout(new BoxLayout(eventsListPanel, BoxLayout.Y_AXIS));
        feedPanel.setViewportView(eventsListPanel);

        this.setVisible(true);
        tickerPanel.launchLoop();
        tickerRefresh.scheduleAtFixedRate(tickerPanel::launchLoop, 1, 90, TimeUnit.SECONDS);
    }

    //Displays an incoming news event as a panel - the events will be sorted by date
    public void displayNewsEvent(NewsEvent event) {
        NewsEventPanel panel = new NewsEventPanel(event);
        panel.addMouseListener(new NewsEventPanelListener(panel));
        panel.getArticleDesc().addMouseListener(new NewsEventPanelListener(panel)); //So child triggers too
        newsPanels.add(panel);

        eventsListPanel.removeAll();
        for (NewsEventPanel p: newsPanels) {
            addToEventsListPanel(p);
        }

        //Add the latest headline to the ticker
        tickerPanel.addNotificationString(event.getHeadline());

        revalidate();
        repaint();
    }

    //This method handles the actual adding to the GUI for the NewsEventPanels
    private void addToEventsListPanel(NewsEventPanel ePanel) {
        ePanel.setPreferredSize(new Dimension(1100, 200));
        ePanel.setBorder(new LineBorder(Color.BLACK, 5));
        eventsListPanel.add(ePanel);
        eventsListPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    //This method allows us to change the topics advertised in the combo box
    public void displayNewTopics(ArrayList<Topic> newTopics) {
        Topic[] topicData = newTopics.stream().toArray(Topic[]::new);
        DefaultComboBoxModel<Topic> model = new DefaultComboBoxModel<>(topicData);
        topicSelect.setModel(model);

        repaint();
    }

    //This class is a listener class for when a NewsEventPanel is hovered over or clicked on
    private class NewsEventPanelListener extends MouseAdapter {

        private NewsEventPanel panel;

        public NewsEventPanelListener(NewsEventPanel panel) {
            this.panel = panel;
        }

        //On click, open the article in the browser
        @Override
        public void mouseClicked(MouseEvent e) {
            try {
                Desktop.getDesktop().browse(panel.getLink());
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(ClientGUI.this, e1.getMessage(), "Could not open link", JOptionPane.ERROR_MESSAGE);
            }
        }

        //On hover, change the colour of the panel to make it clear it can be clicked on
        @Override
        public void mouseEntered(MouseEvent e) {

            //Convert to HSB
            float[] tempColorHSB = Color.RGBtoHSB(panel.getBgColor().getRed(), panel.getBgColor().getGreen(), panel.getBgColor().getBlue(), null);
            //Make the colour brighter
            Color tempColor = Color.getHSBColor(tempColorHSB[0], tempColorHSB[1], tempColorHSB[2] + 0.2f);
            tempColor = new Color(tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), 0);

            //Do the same for the text box
            float[] tempTextColorHSB = Color.RGBtoHSB(panel.getBgColor().getRed(), panel.getBgColor().getGreen(), panel.getBgColor().getBlue(), null);
            Color tempTextColor = Color.getHSBColor(tempTextColorHSB[0], tempTextColorHSB[1], tempTextColorHSB[2] + 0.5f);
            tempTextColor = new Color(tempTextColor.getRed(), tempTextColor.getGreen(), tempTextColor.getBlue(), 0);

            panel.getTopPanel().setBackground(tempColor);
            panel.getArticleDesc().setBackground(tempTextColor);

            repaint();
        }

        //On mouse exit, reset the colour to what it was
        @Override
        public void mouseExited(MouseEvent e) {
            panel.getTopPanel().setBackground(panel.getBgColor());
            panel.getArticleDesc().setBackground(panel.getTextBgColor());

            repaint();
        }
    }
}
