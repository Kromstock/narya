//
// $Id: DummyAuthenticator.java,v 1.5 2002/03/05 03:19:18 mdb Exp $

package com.threerings.presents.server;

import com.threerings.presents.Log;
import com.threerings.presents.net.AuthResponse;
import com.threerings.presents.net.AuthResponseData;
import com.threerings.presents.server.net.Authenticator;
import com.threerings.presents.server.net.AuthingConnection;

/**
 * A simple authenticator implementation that simply accepts all
 * authentication requests.
 */
public class DummyAuthenticator extends Authenticator
{
    /**
     * Accept all authentication requests.
     */
    public void authenticateConnection (AuthingConnection conn)
    {
        Log.info("Accepting request: " + conn.getAuthRequest());
        AuthResponseData rdata = new AuthResponseData();
        rdata.code = AuthResponseData.SUCCESS;
        connectionWasAuthenticated(conn, new AuthResponse(rdata));
    }
}
