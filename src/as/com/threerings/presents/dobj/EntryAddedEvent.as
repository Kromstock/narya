package com.threerings.presents.dobj {

import flash.util.StringBuilder;
import flash.util.trace;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * An entry added event is dispatched when an entry is added to a {@link
 * DSet} attribute of a distributed entry. It can also be constructed to
 * request the addition of an entry to a set and posted to the dobjmgr.
 *
 * @see DObjectManager#postEvent
 */
public class EntryAddedEvent extends NamedEvent
{
    /**
     * Constructs a new entry added event on the specified target object
     * with the supplied set attribute name and entry to add.
     *
     * @param targetOid the object id of the object to whose set we will
     * add an entry.
     * @param name the name of the attribute to which to add the specified
     * entry.
     * @param entry the entry to add to the set attribute.
     */
    public function EntryAddedEvent (
            targetOid :int, name :String, entry :DSetEntry)
    {
        super(targetOid, name);
        _entry = entry;
    }

    /**
     * Returns the entry that has been added.
     */
    public function getEntry () :DSetEntry
    {
        return _entry;
    }

    /**
     * Applies this event to the object.
     */
    public override function applyToObject (target :DObject) :Boolean
        //throws ObjectAccessException
    {
        var added :Boolean = target[_name].add(_entry);
        if (!added) {
            trace("Duplicate entry found [event=" + this + "].");
        }
        return true;
    }

    // documentation inherited
    internal override function notifyListener (listener :Object) :void
    {
        if (listener is SetListener) {
            listener.entryAdded(this);
        }
    }

    // documentation inherited
    protected override function toStringBuf (buf :StringBuilder) :void
    {
        buf.append("ELADD:");
        super.toStringBuf(buf);
        buf.append(", entry=", _entry);
    }

    public override function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(_entry);
    }

    public override function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _entry = (ins.readObject() as DSetEntry);
    }

    protected var _entry :DSetEntry;
}
}