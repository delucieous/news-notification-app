import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * Created by marro on 07/12/2016.
 */
public class Notification<T extends NotifiableEvent> implements Serializable{

    private T event;

    public Notification(T event) {
        this.event = event;
    }

    public T getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return event.toString();
    }
}
