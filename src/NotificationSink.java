import java.net.ConnectException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * Created by marro on 07/12/2016.
 */
public class NotificationSink extends UnicastRemoteObject implements NotificationSinkInterface {

    private String id;
    private HashSet<Topic> topics;
    private LinkedBlockingQueue<NotifiableEvent> notificationQueue;

    public NotificationSink() throws RemoteException {
        super();
        this.id = NotificationFramework.generateSinkID();
        this.notificationQueue = new LinkedBlockingQueue<>();
        this.topics = new HashSet<>();
    }

    //Make 3 attempts to connect to the source, if failure then throw exception
    public void registerToSource(Topic topic) throws ConnectException {
        int attempts = 1;
        while(true) {
            try {
                NotificationSourceInterface source = (NotificationSourceInterface) Naming.lookup(NotificationFramework.hostname + topic.getCode());
                source.register(this);
                topics.add(topic);
                return;
            } catch (NotBoundException | RemoteException e) {
                if (attempts < 4) {
                    attempts++;
                }
                else {
                    throw new ConnectException("Could not connect to registry or source");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean notifyOfEvent(Notification<? extends NotifiableEvent> notification) throws RemoteException {
        try {
            notificationQueue.put(notification.getEvent());
            System.out.println(notificationQueue.size());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public String getID() throws RemoteException {
        return id;
    }

    /*
    Used by the client to get the next notification
     */
    public NotifiableEvent takeNotification() {
        try {
            NotifiableEvent event = notificationQueue.take();
            return event;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public void unsubscribe(Topic topic) throws ConnectException{
        int attempts = 1;
        while(true) {
            try {
                NotificationSourceInterface source = (NotificationSourceInterface) Naming.lookup(NotificationFramework.hostname + topic.getCode());
                source.unregister(this.id);
                return;
            } catch (NotBoundException e) {
                throw new ConnectException("This topic does not exist or has been taken down.");
            } catch (MalformedURLException e) {
                throw new ConnectException("The provided topic name contains illegal characters.");
            } catch (RemoteException e) {
                if (attempts < 4) {
                    attempts++;
                }
                else {
                    throw new ConnectException("This source has already been taken down.");
                }
            }
        }
    }

    public void unsubscribeAll() throws ConnectException {
        for (Topic topic: this.topics) {
            this.unsubscribe(topic);
            this.topics.remove(topic);
        }
    }

}
