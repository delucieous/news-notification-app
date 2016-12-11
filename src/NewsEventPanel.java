import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;

/**
 * Created by marro on 11/12/2016.
 */
public class NewsEventPanel extends JPanel {

    NewsEvent event;
    public JPanel topPanel;
    public Color bgColor;
    public Color textBgColor;
    public JTextArea articleDesc;
    public URI link;

    public NewsEventPanel(NewsEvent event) {
        this.event = event;
        this.link = event.getArticleLink();
        this.build();
    }

    private void build() {
        Topic topic = event.getTopic();
        bgColor = getBGColor(topic);

        this.setBackground(bgColor);
        this.setLayout(new BorderLayout());

        topPanel = new JPanel();
        topPanel.setBackground(bgColor);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        JLabel topicLabel = new JLabel(event.getTopic().toString());
        JLabel urlLabel = new JLabel(event.getArticleLink().toString());
        JLabel headline = new JLabel(event.getHeadline());
        headline.setHorizontalAlignment(SwingConstants.CENTER);


        topPanel.add(topicLabel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(headline);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(urlLabel);

        this.add(topPanel, BorderLayout.NORTH);

        articleDesc = new JTextArea(event.getTextPreview());
        articleDesc.setEditable(false);
        articleDesc.setLineWrap(true);
        articleDesc.setWrapStyleWord(true);

        textBgColor = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 15);
        articleDesc.setBackground(textBgColor);

        this.add(articleDesc, BorderLayout.CENTER);
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
        articleDesc.setCursor(new Cursor(Cursor.HAND_CURSOR));

    }

    private Color getBGColor(Topic topic) {
        Color bgColor = NewsTopics.colourFromTopic(topic);
        System.out.println(bgColor);
        bgColor = bgColor == null ? Color.white : bgColor;
        return new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 70);
    }

}
