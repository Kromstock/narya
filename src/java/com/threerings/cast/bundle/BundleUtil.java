//
// $Id: BundleUtil.java,v 1.1 2001/11/27 08:09:35 mdb Exp $

package com.threerings.cast.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import com.threerings.resource.ResourceBundle;

/**
 * Utility functions related to creating and manipulating component
 * bundles.
 */
public class BundleUtil
{
    /** The path in the metadata bundle to the serialized action table. */
    public static final String ACTIONS_PATH = "actions.dat";

    /** The path in the metadata bundle to the serialized action tile sets
     * table. */
    public static final String ACTION_SETS_PATH = "action_sets.dat";

    /** The path in the metadata bundle to the serialized component class
     * table. */
    public static final String CLASSES_PATH = "classes.dat";

    /** The path in the component bundle to the serialized component id to
     * class/type mapping. */
    public static final String COMPONENTS_PATH = "components.dat";

    /** The file extension of our action tile images. */
    public static final String IMAGE_EXTENSION = ".png";

    /**
     * Attempts to load an object from the supplied resource bundle with
     * the specified path.
     *
     * @return the unserialized object in question or null if no
     * serialized object data was available at the specified path.
     *
     * @exception IOException thrown if an I/O error occurs while reading
     * the object from the bundle.
     */     
    public static Object loadObject (ResourceBundle bundle, String path)
        throws IOException, ClassNotFoundException
    {
        InputStream bin = bundle.getResource(path);
        if (bin == null) {
            return null;
        }
        return new ObjectInputStream(bin).readObject();
    }
}
