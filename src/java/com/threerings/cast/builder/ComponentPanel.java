//
// $Id: ComponentPanel.java,v 1.3 2001/11/02 01:10:28 shaper Exp $

package com.threerings.cast.builder;

import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;

import com.samskivert.swing.*;
import com.samskivert.util.HashIntMap;

import com.threerings.cast.Log;
import com.threerings.cast.*;

/**
 * The component panel displays the available components for all
 * component classes and allows the user to choose a set of components
 * for compositing into a character image.
 */
public class ComponentPanel extends JPanel
{
    /**
     * Constructs the component panel.
     */
    public ComponentPanel (BuilderModel model)
    {
	setLayout(new VGroupLayout(GroupLayout.STRETCH));
	// set up a border
	setBorder(BorderFactory.createEtchedBorder());
        // add the component editors to the panel
        addClassEditors(model);
    }

    /**
     * Adds editor user interface elements for each component class to
     * allow the user to select the desired component.
     */
    protected void addClassEditors (BuilderModel model)
    {
        List classes = model.getComponentClasses();
        HashIntMap components = model.getComponents();

        int size = classes.size();
        for (int ii = 0; ii < size; ii++) {
            ComponentClass cclass = (ComponentClass)classes.get(ii);
            List ccomps = (List)components.get(cclass.clid);
            add(new ClassEditor(model, cclass, ccomps));
        }
    }
}
