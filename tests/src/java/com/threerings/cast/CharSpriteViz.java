//
// $Id: CharSpriteViz.java,v 1.1 2002/06/26 23:53:07 mdb Exp $

package com.threerings.cast;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.ImageManager;
import com.threerings.resource.ResourceManager;
import com.threerings.util.DirectionCodes;
import com.threerings.util.DirectionUtil;

import com.threerings.cast.bundle.BundledComponentRepository;

/**
 * Displays a character sprite in all of the various orientations.
 */
public class CharSpriteViz extends JPanel
    implements DirectionCodes, MouseMotionListener
{
    public CharSpriteViz (
        CharacterManager charmgr, CharacterComponent ccomp, String action)
    {
        // get a handle on our sprite
        _sprite = charmgr.getCharacter(
            new CharacterDescriptor(
                new int[] { ccomp.componentId }, null));

        // put the sprite in the appropriate action mode
        _sprite.setRestingAction(action);
        _sprite.setFollowingPathAction(action);
        _sprite.setActionSequence(action);

        addMouseMotionListener(this);
    }

    public void mouseDragged (MouseEvent event)
    {
    }

    public void mouseMoved (MouseEvent event)
    {
        int orient = DirectionUtil.getFineDirection(
            getWidth()/2, getHeight()/2, event.getX(), event.getY());
        if (_orient != orient) {
            System.out.println("Switching to " +
                               DirectionUtil.toShortString(orient) + ".");
            _orient = orient;
            repaint();
        }
    }

    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        int width = getWidth(), height = getHeight();
        int cx = width/2, cy = height/2;
        g.setColor(Color.darkGray);
        g.drawLine(cx, cy, 0, cy);
        g.drawLine(cx, cy, 0, cy/2);
        g.drawLine(cx, cy, 0, 0);
        g.drawLine(cx, cy, cx/2, 0);
        g.drawLine(cx, cy, cx, 0);
        g.drawLine(cx, cy, 3*width/4, 0);
        g.drawLine(cx, cy, width, 0);
        g.drawLine(cx, cy, width, cy/2);
        g.drawLine(cx, cy, width, cy);
        g.drawLine(cx, cy, width, 3*height/4);
        g.drawLine(cx, cy, width, height);
        g.drawLine(cx, cy, 3*width/4, height);
        g.drawLine(cx, cy, cx, height);
        g.drawLine(cx, cy, cx/2, height);
        g.drawLine(cx, cy, 0, height);
        g.drawLine(cx, cy, 0, 3*height/4);

        _sprite.setLocation(cx, cy);
        _sprite.setOrientation(_orient);
        _sprite.paint((Graphics2D)g);
    }

    public static void main (String[] args)
    {
        if (args.length < 3) {
            System.err.println("Usage: CharSpriteViz cclass cname action");
            System.err.println("  (eg. CharSpriteViz navsail smsloop sailing");
            System.exit(-1);
        }

        JFrame frame = new JFrame("CharSpriteViz");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            ResourceManager rmgr = new ResourceManager(
                "rsrc", null, "config/resource/manager.properties");
            ImageManager imgr = new ImageManager(rmgr, frame);
            ComponentRepository crepo =
                new BundledComponentRepository(rmgr, imgr, "components");
            CharacterManager charmgr = new CharacterManager(crepo);
            CharacterComponent ccomp = crepo.getComponent(args[0], args[1]);

            frame.getContentPane().add(
                new CharSpriteViz(charmgr, ccomp, args[2]),
                BorderLayout.CENTER);
            frame.setSize(200, 200);
            SwingUtil.centerWindow(frame);
            frame.show();

        } catch (NoSuchComponentException nsce) {
            System.err.println("No component with specified class " +
                               "and name [cclass=" + args[0] +
                               ", cname=" + args[1] + "].");
            System.exit(-1);

        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }

    protected CharacterSprite _sprite;
    protected int _orient = NORTH;
}
