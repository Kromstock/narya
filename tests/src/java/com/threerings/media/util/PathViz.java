//
// $Id: PathViz.java,v 1.2 2002/09/18 20:09:55 mdb Exp $

package com.threerings.media.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JFrame;

import com.threerings.media.FrameManager;
import com.threerings.media.ManagedJFrame;
import com.threerings.media.MediaPanel;

import com.threerings.media.sprite.Sprite;
import com.threerings.media.util.*;

/**
 * A test app that is useful for visualizing paths during their
 * development.
 */
public class PathViz extends MediaPanel
{
    public PathViz (FrameManager fmgr)
    {
        super(fmgr);

        // create a sprite that we can send along a path
        Sprite sprite = new HappySprite();
        addSprite(sprite);

        // create a path for our sprite
        Point start = new Point(150, 150);
        double sangle = -3*Math.PI/4;
        ArcPath path = new ArcPath(
            start, 64, 48, sangle, -Math.PI/2, MOVE_DURATION, ArcPath.NORMAL);
//         LinePath path = new LinePath(0, 0, 300, 300, MOVE_DURATION);
        sprite.move(path);
    }

    // documentation inherited from interface
    public void paintBetween (Graphics2D gfx, Rectangle dirtyRect)
    {
        super.paintBetween(gfx, dirtyRect);
        gfx.setColor(Color.gray);
        gfx.fill(dirtyRect);
        _spritemgr.renderSpritePaths(gfx);
    }

    public Dimension getPreferredSize ()
    {
        return new Dimension(300, 300);
    }

    public static void main (String[] args)
    {
        ManagedJFrame frame = new ManagedJFrame("Path viz");
        FrameManager fmgr = new FrameManager(frame);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new PathViz(fmgr));

        fmgr.start();
        frame.pack();
        frame.setVisible(true);
    }

    protected static class HappySprite extends Sprite
    {
        public HappySprite ()
        {
            _bounds.width = 32;
            _bounds.height = 32;
            _oxoff = 16;
            _oyoff = 16;
        }

        public void paint (Graphics2D gfx)
        {
            gfx.setColor(Color.blue);
            int hx = _bounds.x + _bounds.width/2;
            int hy = _bounds.y + _bounds.height/2;
            gfx.drawLine(hx, _bounds.y, hx, _bounds.y + _bounds.height-1);
            gfx.drawLine(_bounds.x, hy, _bounds.x+_bounds.width-1, hy);
            gfx.setColor(Color.black);
            gfx.drawRect(_bounds.x, _bounds.y,
                         _bounds.width-1, _bounds.height-1);
        }
    }

    protected static final long MOVE_DURATION = 10*1000L;
}
