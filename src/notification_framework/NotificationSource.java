package notification_framework;

import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.*;

/*
This class is the source section of the Notification Framework, able to send notifications to sink objects
 */
public class NotificationSource extends UnicastRemoteObject implements NotificationSourceInterface {

    //An executor service used to manage reconnections without interfering with sending
    private ExecutorService connectionHandler;
    //A scheduled executor which empties a sink's queue every 30 minutes
    private ScheduledExecutorService pruneRegister;

    //Contains the sinks which are currently available to receive notifications
    private ConcurrentHashMap<String, NotificationSinkInterface> sinkMap;
    //This map allows us to store notifications for a sink once it has disconnected
    private ConcurrentHashMap<String, LinkedBlockingQueue<Notification<? extends NotifiableEvent>>> heldMap;
    private Topic topic;
    private String id;
    //Allows the source to interface with the registry from another machine
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

    //If a sink has disappeared from the register then it is taken out of the available map
    //This is done every 2 minutes to allow for less attempted connections
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

    //Registers the given sink to this source
    //If the sink is found in the holding map then we send it the notifications that were queued up
    @Override
    public void register(NotificationSinkInterface sink) throws RemoteException {
        String sinkID = sink.getID();
        if (heldMap.containsKey(sinkID)) { //If we reregister before the grace period ends
            sendHeldMessages(sink);
        }
        sinkMap.put(sinkID, sink);
        regProxy.proxyBind(sinkID, sink); //Bind the sink to the registry via the proxy
    }

    //This method sends all the held notifications to the given sink
    private void sendHeldMessages(NotificationSinkInterface sink) throws RemoteException {
        String sinkID = sink.getID();
        //Get the queue
        LinkedBlockingQueue<Notification<? extends NotifiableEvent>> queue = heldMap.get(sinkID);
        while (!queue.isEmpty()) { //Remove notifications until the queue is empty
            Notification<? extends NotifiableEvent> notification = queue.remove();
            try {
                sink.notifyOfEvent(notification);
            }
            catch (ConnectException ce) {
                //If we lose connection, go back through the connection loss cycle and stop trying to send
                handleConnectionLoss(sink, sinkID, notification);
                return;
            }
        }
    }

    //Unregister the sink with the given ID from this source
    @Override
    public void unregister(String id) {
        if (sinkMap.containsKey(id)) {
            sinkMap.remove(id);
            heldMap.put(id, new LinkedBlockingQueue<>()); //Start holding notifications after unregistering
        }
        pruneRegister.scheduleAtFixedRate(() -> {      //After a 30 minute grace period we restart the stored notifications
            heldMap.remove(id);
            heldMap.put(id, new LinkedBlockingQueue<>());
        }, 30, 30, TimeUnit.MINUTES);
        System.out.println("unregistered" + id);
    }

    //Send the given notification to all the registered sources or put it in their holding queue
    public void publishToSource(Notification<? extends NotifiableEvent> notification)  {
        for (Map.Entry<String, NotificationSinkInterface> entry: sinkMap.entrySet()) {
            try {
                entry.getValue().notifyOfEvent(notification); //Try to send
            }
            catch(RemoteException re) {
                heldMap.put(entry.getKey(), new LinkedBlockingQueue<>()); //put in holding map for reconnection
                sinkMap.remove(entry.getKey()); //Remove so we do not try to send notifications to dead sink
                handleConnectionLoss(entry.getValue(), entry.getKey(), notification);
            }
        }
        //Add the notification to the queue of all the sinks in the holding group
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

    //Allows remote access to the source's topic
    @Override
    public Topic getTopic() {
        return this.topic;
    }

    //Try reconnecting 3 times and then give up - when giving up unregister the sink.
    private boolean handleConnectionLoss(NotificationSinkInterface sink, String id, Notification<? extends NotifiableEvent> notification) {
        connectionHandler.execute(() -> {
            int attempts = 1;
            while (true) {
                try {
                    System.out.println("Polling after " + (attempts * 5000));
                    Thread.sleep(5000 * attempts);
                    if (sinkMap.containsKey(id)) { //This would signify a reregistration
                        System.out.println(id + " reconnected");
                        heldMap.remove(id);
                        return;
                    }
                    sink.notifyOfEvent(notification);
                    sendHeldMessages(sink); //This indicates a successful reconnection to the SAME sink

                    heldMap.remove(id);
                    sinkMap.put(id, sink); //Put it back into the sinkMap and proceed as before
                    System.out.println(id+ " reconnected");
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
