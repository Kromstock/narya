//
// $Id: EditableSceneImpl.java,v 1.7 2003/01/31 23:10:46 mdb Exp $

package com.threerings.whirled.tools;

import java.util.ArrayList;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.whirled.client.DisplaySceneImpl;
import com.threerings.whirled.data.SceneModel;

/**
 * A basic implementation of the {@link EditableScene} interface.
 */
public class EditableSceneImpl implements EditableScene
{
    /**
     * Creates an instance that will create and use a blank scene model.
     */
    public EditableSceneImpl ()
    {
        this(EditableSceneModel.blankSceneModel());
    }

    /**
     * Creates an instance that will obtain data from the supplied scene
     * model and update it when changes are made.
     */
    public EditableSceneImpl (EditableSceneModel model)
    {
        this(model, new DisplaySceneImpl(model.sceneModel, null));
    }

    /**
     * Creates an instance that will obtain data from the supplied scene
     * model and update it when changes are made. It will delegate to the
     * supplied display scene instead of creating its own delegate.
     */
    public EditableSceneImpl (
        EditableSceneModel model, DisplaySceneImpl delegate)
    {
        _model = model.sceneModel;
        _emodel = model;
        _delegate = delegate;
    }

    // documentation inherited
    public int getId ()
    {
        return _delegate.getId();
    }

    // documentation inherited
    public String getName ()
    {
        return _delegate.getName();
    }

    // documentation inherited
    public int getVersion ()
    {
        return _delegate.getVersion();
    }

    // documentation inherited
    public int[] getNeighborIds ()
    {
        return _delegate.getNeighborIds();
    }

    // documentation inherited
    public PlaceConfig getPlaceConfig ()
    {
        return _delegate.getPlaceConfig();
    }

    // documentation inherited
    public void setId (int sceneId)
    {
        _model.sceneId = sceneId;
    }

    // documentation inherited
    public void setName (String name)
    {
        _model.sceneName = name;
    }

    // documentation inherited
    public void setVersion (int version)
    {
        _model.version = version;
    }

    // documentation inherited
    public void setNeighborIds (int[] neighborIds)
    {
        _model.neighborIds = neighborIds;
    }

    // documentation inherited
    public ArrayList getNeighborNames ()
    {
        return _emodel.neighborNames;
    }

    // documentation inherited
    public void addNeighbor (String neighborName)
    {
        if (!_emodel.neighborNames.contains(neighborName)) {
            _emodel.neighborNames.add(neighborName);
        }
    }

    // documentation inherited
    public boolean removeNeighbor (String neighborName)
    {
        return _emodel.neighborNames.remove(neighborName);
    }

    // documentation inherited
    public EditableSceneModel getSceneModel ()
    {
        flushToModel();
        return _emodel;
    }

    /**
     * Derived classes should override this method and flush any editable
     * modifications to the scene model when this method is called.
     */
    protected void flushToModel ()
    {
    }

    /** A reference to our scene model. */
    protected SceneModel _model;

    /** A reference to our editable scene model. */
    protected EditableSceneModel _emodel;

    /** Our display scene delegate. */
    protected DisplaySceneImpl _delegate;
}
