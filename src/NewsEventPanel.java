import javax.swing.*;
import java.awt.*;
import java.net.URI;

/*
This class handles the presentation of a NewsEvent on the GUI
 */
public class NewsEventPanel extends JPanel implements Comparable<NewsEventPanel> {

    private NewsEvent event;
    private JPanel topPanel;
    private Color bgColor;
    private Color textBgColor;
    private JTextArea articleDesc;
    private URI link;

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
        topPanel.setLayout(new GridLayout(2, 2));
        JLabel topicLabel = new JLabel(event.getTopic().toString());
        JLabel urlLabel = new JLabel(event.getArticleLink().toString());
        JLabel headline = new JLabel(event.getHeadline());
        headline.setFont(headline.getFont().deriveFont(Font.BOLD));
        JLabel dateLabel = new JLabel(event.getTime().toString());
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        urlLabel.setHorizontalAlignment(SwingConstants.RIGHT);


        topPanel.add(topicLabel);
        topPanel.add(urlLabel);
        topPanel.add(headline);
        topPanel.add(dateLabel);

        this.add(topPanel, BorderLayout.NORTH);

        //Text area containing the description of the article
        articleDesc = new JTextArea(event.getTextPreview());
        articleDesc.setEditable(false);
        articleDesc.setLineWrap(true);
        articleDesc.setWrapStyleWord(true);
        articleDesc.setFont(articleDesc.getFont().deriveFont(20f));
        articleDesc.setFont(new Font("Helvetica", articleDesc.getFont().getStyle(), articleDesc.getFont().getSize()));

        textBgColor = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 15);
        articleDesc.setBackground(textBgColor);

        this.add(articleDesc, BorderLayout.CENTER);
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
        articleDesc.setCursor(new Cursor(Cursor.HAND_CURSOR));

    }

    //Gets the background colour from the given topic
    private Color getBGColor(Topic topic) {
        Color bgColor = NewsTopics.colourFromTopic(topic);
        bgColor = bgColor == null ? Color.white : bgColor; //Previous method can return null so need to check
        //Change the alpha value since this cannot be encoded in hex
        return new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 70);
    }

    //This is used to allow the panels to be sorted in the TreeSet, guaranteeing date order
    @Override
    public int compareTo(NewsEventPanel o) {
        if (this.event.getTime().after(o.event.getTime())) {
            return -2;
        }
        else if (this.event.getTime().before(o.event.getTime())) {
            return 2;
        }
        else {return 0;}
    }


    public NewsEvent getEvent() {
        return event;
    }

    public JPanel getTopPanel() {
        return topPanel;
    }

    public Color getBgColor() {
        return bgColor;
    }

    public Color getTextBgColor() {
        return textBgColor;
    }

    public JTextArea getArticleDesc() {
        return articleDesc;
    }

    public URI getLink() {
        return link;
    }

}
