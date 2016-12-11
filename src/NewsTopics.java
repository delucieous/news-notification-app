import java.awt.*;

/**
 * Created by marro on 11/12/2016.
 */
public enum NewsTopics {

    BUSINESS(new Topic("business"), new Color(0x3686FF)),
    GAMING(new Topic("gaming"), new Color(0x6CEF86)),
    ENTERTAINMENT(new Topic("entertainment"), new Color(0xEF68B9)),
    GENERAL(new Topic("general"), new Color(0xA8B6AC)),
    MUSIC(new Topic("music"), new Color(0xD5BB22)),
    SCIENCE(new Topic("science-and-nature"), new Color(0x20D1C3)),
    SPORT(new Topic("sport"), new Color(255, 0, 6)),
    TECHNOLOGY(new Topic("technology"), new Color(0x41C720));


    private Topic topic;
    private Color colour;

    NewsTopics(Topic topic, Color colour) {
        this.topic = topic;
        this.colour = colour;
    }

    public Topic getTopic() {
        return this.topic;
    }

    public Color getColour() {
        return this.colour;
    }

    public static Color colourFromTopic(Topic topic) {
        for (NewsTopics nt: values()) {
            if (nt.getTopic().getCode().equals(topic.getCode())) {
                return nt.getColour();
            }
        }
        return null;
    }
}
