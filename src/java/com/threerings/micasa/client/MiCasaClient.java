//
// $Id: MiCasaClient.java,v 1.6 2001/10/25 23:21:32 mdb Exp $

package com.threerings.micasa.client;

import java.awt.event.*;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.samskivert.util.Config;

import com.threerings.presents.client.*;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.net.*;

import com.threerings.crowd.client.LocationDirector;
import com.threerings.crowd.client.OccupantManager;
import com.threerings.crowd.client.PlaceView;

import com.threerings.parlor.client.ParlorDirector;

import com.threerings.micasa.Log;
import com.threerings.micasa.util.MiCasaContext;

/**
 * The MiCasa client takes care of instantiating all of the proper
 * managers and loading up all of the necessary configuration and getting
 * the client bootstrapped.
 */
public class MiCasaClient
    implements Client.Invoker
{
    /**
     * Creates a new client and provides it with a frame in which to
     * display everything.
     */
    public MiCasaClient (MiCasaFrame frame)
        throws IOException
    {
        // create our context
        _ctx = new MiCasaContextImpl();

        // create the handles on our various services
        _config = new Config();
        _client = new Client(null, this);

        // create our managers and directors
        _locdir = new LocationDirector(_ctx);
        _occmgr = new OccupantManager(_ctx);
        _pardtr = new ParlorDirector(_ctx);

        // for test purposes, hardcode the server info
        _client.setServer("bering", 4007);

        // keep this for later
        _frame = frame;

        // log off when they close the window
        _frame.addWindowListener(new WindowAdapter() {
            public void windowClosing (WindowEvent evt) {
                // if we're logged on, log off
                if (_client.loggedOn()) {
                    _client.logoff(true);
                }
            }
        });

        // create our client controller and stick it in the frame
        _frame.setController(new ClientController(_ctx, _frame));
    }

    /**
     * Returns a reference to the context in effect for this client. This
     * reference is valid for the lifetime of the application.
     */
    public MiCasaContext getContext ()
    {
        return _ctx;
    }

    // documentation inherited
    public void invokeLater (Runnable run)
    {
        // queue it on up on the swing thread
        SwingUtilities.invokeLater(run);
    }

    /**
     * The context implementation. This provides access to all of the
     * objects and services that are needed by the operating client.
     */
    protected class MiCasaContextImpl implements MiCasaContext
    {
        public Config getConfig ()
        {
            return _config;
        }

        public Client getClient ()
        {
            return _client;
        }

        public DObjectManager getDObjectManager ()
        {
            return _client.getDObjectManager();
        }

        public LocationDirector getLocationDirector ()
        {
            return _locdir;
        }

        public OccupantManager getOccupantManager ()
        {
            return _occmgr;
        }

        public ParlorDirector getParlorDirector ()
        {
            return _pardtr;
        }

        public void setPlaceView (PlaceView view)
        {
            // stick the place view into our frame
            _frame.setPanel((JPanel)view);
        }

        public MiCasaFrame getFrame ()
        {
            return _frame;
        }
    }

    protected MiCasaContext _ctx;
    protected MiCasaFrame _frame;

    protected Config _config;
    protected Client _client;
    protected LocationDirector _locdir;
    protected OccupantManager _occmgr;
    protected ParlorDirector _pardtr;
}
