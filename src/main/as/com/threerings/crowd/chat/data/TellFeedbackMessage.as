//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.crowd.chat.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.util.Name;

/**
 * A feedback message to indicate that a tell succeeded.
 */
public class TellFeedbackMessage extends UserMessage
{
    /**
     * A tell feedback message is only composed on the client.
     */
    public function TellFeedbackMessage (target :Name, message :String, failed :Boolean = false)
    {
        super(target, null, message);
        _failure = failed;
    }

    /**
     * Returns true if this is a failure feedback, false if it is successful tell feedback.
     */
    public function isFailure () :Boolean
    {
        return _failure;
    }

    override public function getFormat () :String
    {
        return _failure ? null : "m.told_format";
    }

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _failure = ins.readBoolean();
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeBoolean(_failure);
    }

    protected var _failure :Boolean;
}
}