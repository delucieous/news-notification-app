import java.awt.*;
import java.net.URI;
import java.util.Date;

/**
 * Created by marro on 11/12/2016.
 */
public class NewsEvent implements NotifiableEvent {

    private Date time;
    private Topic topic;
    private URI articleLink;
    private String textPreview;
    private String headline;

    public NewsEvent(Topic topic, URI articleLink, String textPreview, String headline) {
        this.topic = topic;
        this.articleLink = articleLink;
        this.textPreview = textPreview;
        this.headline = headline;
        this.time = new Date(); //Capture the tme of creation
    }

    @Override
    public Date getTime() {
        return this.time;
    }

    @Override
    public Topic getTopic() {
        return this.topic;
    }

    public URI getArticleLink() {
        return this.articleLink;
    }

    public String getTextPreview() {
        return this.textPreview;
    }

    public String getHeadline() {
        return this.headline;
    }

    @Override
    public String toString() {
        return "Date: " + this.time.toString() + " Link: " + this.articleLink.toString() + " Headline: " + this.headline;
    }
}
