//
// $Id: ClientDObjectMgr.java,v 1.1 2001/06/09 23:39:03 mdb Exp $

package com.threerings.cocktail.cher.client;

import java.util.ArrayList;
import com.samskivert.util.IntMap;
import com.samskivert.util.Queue;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.dobj.*;
import com.threerings.cocktail.cher.net.*;

/**
 * The client distributed object manager manages a set of proxy objects
 * which mirror the distributed objects maintained on the server.
 * Requests for modifications, etc. are forwarded to the server and events
 * are dispatched from the server to this client for objects to which this
 * client is subscribed.
 */
public class ClientDObjectMgr
    implements DObjectManager, Runnable
{
    /**
     * Constructs a client distributed object manager.
     *
     * @param comm a communicator instance by which it can communicate
     * with the server.
     * @param client a reference to the client that is managing this whole
     * communications and event dispatch business.
     */
    public ClientDObjectMgr (Communicator comm, Client client)
    {
        _comm = comm;
        _client = client;
    }

    // inherit documentation from the interface
    public void createObject (Class dclass, Subscriber target,
                              boolean subscribe)
    {
        // not presently supported
        throw new RuntimeException("createObject() not supported");
    }

    // inherit documentation from the interface
    public void subscribeToObject (int oid, Subscriber target)
    {
        queueAction(oid, target, true);
    }

    // inherit documentation from the interface
    public void unsubscribeFromObject (int oid, Subscriber target)
    {
        queueAction(oid, target, false);

        // forward an unsubscribe request to the server
        _comm.postMessage(new UnsubscribeRequest(oid));
    }

    protected void queueAction (int oid, Subscriber target, boolean subscribe)
    {
        // queue up an action
        _actions.append(new ObjectAction(oid, target, subscribe));
        // and queue up the omgr to get invoked on the invoker thread
        _client.getInvoker().invokeLater(this);
    }

    // inherit documentation from the interface
    public void postEvent (DEvent event)
    {
        // send a forward event request to the server
        _comm.postMessage(new ForwardEventRequest(event));
    }

    /**
     * Called by the communicator when a downstream message arrives from
     * the network layer. We queue it up for processing and request some
     * processing time on the main thread.
     */
    public void processMessage (DownstreamMessage msg)
    {
        // append it to our queue
        _actions.append(msg);
        // and queue ourselves up to be run
        _client.getInvoker().invokeLater(this);
    }

    /**
     * Invoked on the AWT thread to process any newly arrived messages
     * that we have waiting in our queue.
     */
    public void run ()
    {
        // process all of the events on our queue
        Object obj;
        while ((obj = _actions.getNonBlocking()) != null) {
            // do the proper thing depending on the object
            if (obj instanceof EventNotification) {
                DEvent evt = ((EventNotification)obj).getEvent();
                dispatchEvent(evt);

            } else if (obj instanceof ObjectResponse) {
                registerObjectAndNotify(((ObjectResponse)obj).getObject());

            } else if (obj instanceof FailureResponse) {
                int oid = ((FailureResponse)obj).getOid();
                notifyFailure(oid);

            } else if (obj instanceof PongResponse) {
                Log.info("Got pong.");

            } else if (obj instanceof ObjectAction) {
                ObjectAction act = (ObjectAction)obj;
                if (act.subscribe) {
                    doSubscribe(act.oid, act.target);
                } else {
                    doUnsubscribe(act.oid, act.target);
                }
            }
        }
    }

    /**
     * Called when a new event arrives from the server that should be
     * dispatched to subscribers here on the client.
     */
    protected void dispatchEvent (DEvent event)
    {
        Log.info("Dispatch event: " + event);

        // look up the object on which we're dispatching this event
        DObject target = (DObject)_ocache.get(event.getTargetOid());
        if (target == null) {
            Log.info("Unable to dispatch event on non-proxied " +
                     "object [event=" + event + "].");
            return;
        }
    }

    /**
     * Registers this object in our proxy cache and notifies the
     * subscribers that were waiting for subscription to this object.
     */
    protected void registerObjectAndNotify (DObject obj)
    {
        // let the object know that we'll be managing it
        obj.setManager(this);

        // stick the object into the proxy object table
        _ocache.put(obj.getOid(), obj);

        // let the penders know that the object is available
        PendingRequest req = (PendingRequest)_penders.remove(obj.getOid());
        if (req == null) {
            Log.warning("Got object, but no one cares?! " +
                        "[oid=" + obj.getOid() + ", obj=" + obj + "].");
            return;
        }

        for (int i = 0; i < req.targets.size(); i++) {
            Subscriber target = (Subscriber)req.targets.get(i);
            target.objectAvailable(obj);
        }
    }

    /**
     * Notifies the subscribers that had requested this object (for
     * subscription) that it is not available.
     */
    protected void notifyFailure (int oid)
    {
        Log.info("Get failed: " + oid);
    }

    /**
     * This is guaranteed to be invoked via the invoker and can safely do
     * main thread type things like call back to the subscriber.
     */
    protected void doSubscribe (int oid, Subscriber target)
    {
        Log.info("doSubscribe: " + oid + ": " + target);

        // first see if we've already got the object in our table
        DObject obj = (DObject)_ocache.get(oid);
        if (obj != null) {
            // add the subscriber and call them back straight away
            obj.addSubscriber(target);
            target.objectAvailable(obj);
            return;
        }

        // see if we've already got an outstanding request for this object
        PendingRequest req = (PendingRequest)_penders.get(oid);
        if (req != null) {
            // add this subscriber to the list of subscribers to be
            // notified when the request is satisfied
            req.addTarget(target);
            return;
        }

        // otherwise we need to create a new request
        req = new PendingRequest(oid);
        req.addTarget(target);
        _penders.put(oid, req);
        Log.info("Registering pending request [oid=" + oid + "].");

        // and issue a request to get things rolling
        _comm.postMessage(new SubscribeRequest(oid));
    }

    /**
     * This is guaranteed to be invoked via the invoker and can safely do
     * main thread type things like call back to the subscriber.
     */
    protected void doUnsubscribe (int oid, Subscriber target)
    {
        DObject dobj = (DObject)_ocache.get(oid);
        if (dobj != null) {
            dobj.removeSubscriber(target);

        } else {
            Log.info("Requested to remove subscriber from " +
                     "non-proxied object [oid=" + oid +
                     ", sub=" + target + "].");
        }
    }

    /**
     * The object action is used to queue up a subscribe or unsubscribe
     * request.
     */
    protected class ObjectAction
    {
        public int oid;
        public Subscriber target;
        public boolean subscribe;

        public ObjectAction (int oid, Subscriber target, boolean subscribe)
        {
            this.oid = oid;
            this.target = target;
            this.subscribe = subscribe;
        }
    }

    protected static class PendingRequest
    {
        public int oid;
        public ArrayList targets = new ArrayList();

        public PendingRequest (int oid)
        {
            this.oid = oid;
        }

        public void addTarget (Subscriber target)
        {
            targets.add(target);
        }
    }

    protected Communicator _comm;
    protected Client _client;
    protected Queue _actions = new Queue();

    /**
     * This table contains all of the distributed objects that are active
     * on this client.
     */
    protected IntMap _ocache = new IntMap();

    /** This table contains pending subscriptions. */
    protected IntMap _penders = new IntMap();
}
