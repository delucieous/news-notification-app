import javax.swing.*;
import java.net.ConnectException;
import java.util.ArrayList;

/**
 * Created by marro on 10/12/2016.
 */
public class NewsTickerClient {

    private ClientGUI gui;

    public NewsTickerClient() {

    }

    public void start() {
        try {
            ArrayList<Topic> topics = NotificationFramework.getSources();
            gui = new ClientGUI(this, topics);
            SwingUtilities.invokeLater(gui::initialise);
        } catch (ConnectException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Connection Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
