package notification_framework;

import java.io.Serializable;
import java.util.Date;

/*
A simple interface which defines the shape of an event that can be passed from source to sink through a notification
 */
public interface NotifiableEvent extends Serializable {

    Date getTime();
    Topic getTopic();

}
