import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by marro on 06/12/2016.
 */
public class NotificationSource extends UnicastRemoteObject implements NotificationSourceInterface {

    private ExecutorService connectionHandler;
    private ScheduledExecutorService pruneRegister;

    private ConcurrentHashMap<String, NotificationSinkInterface> sinkMap;
    private ConcurrentHashMap<String, LinkedBlockingQueue<Notification<? extends NotifiableEvent>>> heldMap;
    private Topic topic;
    private String id;
    private RegistryProxyInterface regProxy;

    public NotificationSource(Topic topic, RegistryProxyInterface regProxy) throws RemoteException {
        super();
        this.topic = topic;
        id = topic.getCode();
        this.sinkMap = new ConcurrentHashMap<>();
        this.heldMap = new ConcurrentHashMap<>();
        this.connectionHandler = Executors.newFixedThreadPool(4);
        this.pruneRegister = Executors.newScheduledThreadPool(4);
        this.regProxy = regProxy;
        launchPruningTask();
    }

    private void launchPruningTask() {
        pruneRegister.scheduleAtFixedRate(() -> {
            for (String id: sinkMap.keySet()) {
                try {
                    Naming.lookup(NotificationFramework.hostname + id);
                }
                catch(NotBoundException nbe) {
                    sinkMap.remove(id);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }, 3, 2, TimeUnit.MINUTES);
    }

    @Override
    public void register(NotificationSinkInterface sink) throws RemoteException {
        String sinkID = sink.getID();
        if (heldMap.containsKey(sinkID)) { //If we reregister before the grace period ends
            sendHeldMessages(sink);
        }
        sinkMap.put(sinkID, sink);
        regProxy.proxyBind(sinkID, sink);
    }

    private void sendHeldMessages(NotificationSinkInterface sink) throws RemoteException {
        String sinkID = sink.getID();
        LinkedBlockingQueue<Notification<? extends NotifiableEvent>> queue = heldMap.get(sinkID);
        System.out.println(queue);
        while (!queue.isEmpty()) {
            Notification<? extends NotifiableEvent> notification = queue.remove();
            try {
                sink.notifyOfEvent(notification);
            }
            catch (ConnectException ce) {
                handleConnectionLoss(sink, sinkID, notification);
                return;
            }
        }
    }

    @Override
    public void unregister(String id) {
        if (sinkMap.containsKey(id)) {
            sinkMap.remove(id);
        }
        try {
            regProxy.proxyUnbind(id);
        } catch (Exception e) {
            System.out.println("Already unregistered");
        }
        pruneRegister.schedule(() -> {      //After a 30 minute grace period we wipe the stored notifications
            heldMap.remove(id);
        }, 30, TimeUnit.MINUTES);
        System.out.println("unregistered" + id);
    }

    @Override
    public void publishToSource(Notification<? extends NotifiableEvent> notification) throws RemoteException {
        for (Map.Entry<String, NotificationSinkInterface> entry: sinkMap.entrySet()) {
            try {
                entry.getValue().notifyOfEvent(notification);
            }
            catch(ConnectException ce) {
                heldMap.put(entry.getKey(), new LinkedBlockingQueue<>()); //put in holding map for reconnection -> need to add messages to queue
                sinkMap.remove(entry.getKey()); //Remove so we do not try to send notifications
                handleConnectionLoss(entry.getValue(), entry.getKey(), notification);
            }
        }
        for (Map.Entry<String, LinkedBlockingQueue<Notification<? extends NotifiableEvent>>> entry: heldMap.entrySet() ) {
            try {
                entry.getValue().put(notification);
            }
            catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Override
    public Topic getTopic() {
        return this.topic;
    }

    //Try reconnecting 5 times and then give up - when giving up unregister the sink.
    private boolean handleConnectionLoss(NotificationSinkInterface sink, String id, Notification<? extends NotifiableEvent> notification) {
        connectionHandler.execute(() -> {
            int attempts = 1;
            while (true) {
                try {
                    System.out.println("Polling after " + (attempts * 5000));
                    Thread.sleep(5000 * attempts);
                    if (sinkMap.containsKey(id)) { //This would signify a reregistration
                        System.out.println("reconnected");
                        heldMap.remove(id);
                        return;
                    }
                    sink.notifyOfEvent(notification);
                    sendHeldMessages(sink); //This indicates a successful reconnection to the SAME sink

                    heldMap.remove(id);
                    sinkMap.put(id, sink); //Put it back into the sinkMap and proceed as before
                    System.out.println("reconnected");
                    return;
                }
                catch(RemoteException ce) {
                    if(attempts < 3) {
                        attempts++;
                    }
                    else {
                        unregister(id); // Give up and unregister the sink
                        return;
                    }
                }
                catch(InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        return true;
    }
}
