package com.threerings.io.streamers {

import com.threerings.util.Float;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;

/**
 * A Streamer for Float objects.
 */
public class FloatStreamer extends Streamer
{
    public function FloatStreamer ()
    {
        super(Float, "java.lang.Float");
    }

    public override function createObject (ins :ObjectInputStream) :Object
    {
        return new Float(ins.readFloat());
    }

    public override function writeObject (obj :Object, out :ObjectOutputStream)
            :void
    {
        var float :Float = (obj as Float);
        out.writeFloat(float.value);
    }

    public override function readObject (obj :Object, ins :ObjectInputStream)
            :void
    {
        // unneeded, done in createObject
    }
}
}