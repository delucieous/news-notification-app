import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

/**
 * Created by marro on 10/12/2016.
 */
public class TickerPanel extends JPanel implements ActionListener{

    private LinkedList<String> strings; //This is used as a ring data structure with the latest headlines
    private JLabel tickerLabel; //The label we will be scrolling
    private int scrollIncrement; //The amount we scroll the label by on each iteration
    private int currentScrollPosition; //The current position of the front of the label
    private int wrapPosition; //The current position of the front of the wrapping label
    private int stringWidth; //The width of the full string of all headlines
    private Timer timer; //The timer that we use to perform the scrolling

    public TickerPanel(String initialString, int scrollIncrement) {
        strings = new LinkedList<>();
        this.scrollIncrement = scrollIncrement;

        this.setLayout(new BorderLayout());
        timer = new Timer(10, this);

        tickerLabel = new JLabel(initialString);
        tickerLabel.setHorizontalAlignment(JLabel.CENTER);
        tickerLabel.setVerticalAlignment(JLabel.CENTER);
        tickerLabel.setFont(new Font("Helvetica", Font.BOLD, 50));

        this.add(tickerLabel, BorderLayout.CENTER);

        //By doing this and calling launchLoop in our GUI setup code, we get a nice scrolling welcome message
        strings.addFirst(initialString);

    }

    /*
    Builds the string from the LinkedList which we will scroll across the screen
     */
    public String createLoopingString() {
        StringBuilder sb = new StringBuilder();
        sb.append("---");
        for (String string: strings) {
            sb.append("  |||  ").append(string);
        }
        sb.append("  |||  ").append("---");
        return sb.toString();
    }

    /*
    Get the width of a string dependent on the font as given to the JLabel. Allows us to set the width we need for a string.
     */
    private int getStringWidth(String loopString) {
        Graphics2D g2d = (Graphics2D) tickerLabel.getGraphics();
        FontMetrics fm = g2d.getFontMetrics();
        return SwingUtilities.computeStringWidth(fm, loopString);

    }

    /**
     * This is called whenever the ticker is refreshed - build a string and create a proper size JLabel for it
     */
    public void launchLoop() {
        currentScrollPosition = -this.getWidth();
        String loopString = this.createLoopingString();
        tickerLabel.setText(loopString);
        this.stringWidth = getStringWidth(loopString);

        tickerLabel.setBounds(0, 0, this.stringWidth + 100, getHeight()); //ABSOLUTELY VITAL DO NOT REMOVE JOSH!!!!
        timer.start();
        repaint();
    }

    //This is how we add notifications - the linkedlist acts like a ring
    public void addNotificationString(String notificationString) {
        if (strings.size() < 5) {
            strings.addFirst(notificationString);
        }
        else {
            strings.removeLast();
            strings.addFirst(notificationString);
        }
    }

    /*
    Translate the label a distance corresponding to the current scrolling position, then paint the children
    When the wrapping starts, we are in effect painting the same label twice to achieve the wrapping effect
     */
    @Override
    public void paintChildren(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        g2.translate(-currentScrollPosition, 0);
        super.paintChildren(g);
        g2.translate(currentScrollPosition, 0);

        wrapPosition = currentScrollPosition - tickerLabel.getWidth() - 2;
        g2.translate(-wrapPosition, 0);
        super.paintChildren(g);
        g2.translate(wrapPosition, 0);

    }

    /*
    Override validateTree to stop Swing trying to "correct" the width of the label - IT MUST be wider than the panel
    to allow for long string scrolling. FIGHT THE SYSTEM
     */
    @Override
    public void validateTree() {
        super.validateTree();
        tickerLabel.setBounds(0, 0, (this.stringWidth + 100), getHeight()); //Stop Swing from overwriting the size of the label
    }

    //Used by the timer to change the position of the label
    //Each time this action is performed, increment the current position
    //If this position has moved past the width of the label we need to wrap
    @Override
    public void actionPerformed(ActionEvent e) {
        currentScrollPosition = currentScrollPosition + scrollIncrement;

        if (currentScrollPosition > tickerLabel.getWidth()) {
            currentScrollPosition = wrapPosition + scrollIncrement;
        }

        repaint();
    }
}
