//
// $Id: SubscribeRequest.java,v 1.3 2001/06/05 22:44:31 mdb Exp $

package com.threerings.cocktail.cher.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class SubscribeRequest extends UpstreamMessage
{
    /** The code for an object subscription request. */
    public static final short TYPE = TYPE_BASE + 1;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public SubscribeRequest ()
    {
        super();
    }

    /**
     * Constructs a subscribe request for the distributed object with the
     * specified object id.
     */
    public SubscribeRequest (int oid)
    {
        _oid = oid;
    }

    public short getType ()
    {
        return TYPE;
    }

    /**
     * Returns the oid of the object to which we desire subscription.
     */
    public int getOid ()
    {
        return _oid;
    }

    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        out.writeInt(_oid);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _oid = in.readInt();
    }

    /**
     * The object id of the distributed object to which we are
     * subscribing.
     */
    protected int _oid;
}
