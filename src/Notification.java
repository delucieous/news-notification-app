import java.io.Serializable;

/*
Notification is the serializable part of the Notification Framework, this is the object which is sent from source to sink
Generics are used to ensure that any object implementing NotifiableEvent can be put inside a notification
 */
public class Notification<T extends NotifiableEvent> implements Serializable {

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
