//
// $Id: TileSet.java,v 1.40 2003/01/17 02:30:50 mdb Exp $

package com.threerings.media.tile;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import com.samskivert.util.LRUHashMap;
import com.samskivert.util.RuntimeAdjust;
import com.samskivert.util.StringUtil;
import com.samskivert.util.Tuple;

import com.threerings.media.Log;
import com.threerings.media.MediaPrefs;
import com.threerings.media.image.Colorization;
import com.threerings.media.image.Mirage;

/**
 * A tileset stores information on a single logical set of tiles. It
 * provides a clean interface for the {@link TileManager} or other
 * entities to retrieve individual tiles from the tile set and
 * encapsulates the potentially sophisticated process of extracting the
 * tile image from a composite tileset image.
 *
 * <p> Tiles are referenced by their tile id.  The tile id is essentially
 * the tile number, assuming the tile at the top-left of the image is tile
 * id zero and tiles are numbered, in ascending order, left to right, top
 * to bottom.
 *
 * <p> This class is serializable and will be serialized, so derived
 * classes should be sure to mark non-persistent fields as
 * <code>transient</code>.
 */
public abstract class TileSet
    implements Cloneable, Serializable
{
    /**
     * Configures this tileset with an image provider that it can use to
     * load its tileset image. This will be called automatically when the
     * tileset is fetched via the {@link TileManager}.
     */
    public void setImageProvider (ImageProvider improv)
    {
        _improv = improv;
    }

    /**
     * Returns the tileset name.
     */
    public String getName ()
    {
	return _name;
    }

    /**
     * Specifies the tileset name.
     */
    public void setName (String name)
    {
        _name = name;
    }

    /**
     * Sets the path to the image that will be used by this tileset. This
     * must be called before the first call to {@link #getTile}.
     */
    public void setImagePath (String imagePath)
    {
        _imagePath = imagePath;
    }

    /**
     * Returns the path to the composite image used by this tileset.
     */
    public String getImagePath ()
    {
        return _imagePath;
    }

    /**
     * Returns the number of tiles in the tileset.
     */
    public abstract int getTileCount ();

    /**
     * Creates a copy of this tileset which will apply the supplied
     * colorizations to its tileset image when creating tiles.
     */
    public TileSet clone (Colorization[] zations)
    {
        try {
            TileSet tset = (TileSet)clone();
            tset._zations = zations;
            return tset;

        } catch (CloneNotSupportedException cnse) {
            Log.warning("Unable to clone tileset prior to colorization " +
                        "[tset=" + this +
                        ", zations=" + StringUtil.toString(zations) +
                        ", error=" + cnse + "].");
            return null;
        }
    }

    /**
     * Returns a new tileset that is a clone of this tileset with the
     * image path updated to reference the given path. Useful for
     * configuring a single tileset and then generating additional
     * tilesets with new images with the same configuration.
     */
    public TileSet clone (String imagePath)
        throws CloneNotSupportedException
    {
        TileSet dup = (TileSet)clone();
        dup.setImagePath(imagePath);
        return dup;
    }

    /**
     * Creates a {@link Tile} object from this tileset corresponding to
     * the specified tile id and returns that tile. A null tile will never
     * be returned, but one with an error image may be returned if a
     * problem occurs loading the underlying tileset image.
     *
     * @param tileIndex the index of the tile in the tileset. Tile indexes
     * start with zero as the upper left tile and increase by one as the
     * tiles move left to right and top to bottom over the source image.
     *
     * @return the tile object.
     *
     * @exception NoSuchTileException thrown if the specified tile index
     * is out of range for this tileset.
     */
    public Tile getTile (int tileIndex)
        throws NoSuchTileException
    {
        checkTileIndex(tileIndex);

        // create our tile cache if necessary
        if (_tiles == null) {
            int tcsize = _cacheSize.getValue();
            Log.debug("Creating tile cache [size=" + tcsize + "k].");
            _tiles = new LRUHashMap(tcsize*1024, new LRUHashMap.ItemSizer() {
                public int computeSize (Object value) {
                    return (int)((Tile)value).getEstimatedMemoryUsage();
                }
            });
        }

        // fetch and cache the tile
        Tuple key = new Tuple(this, new Integer(tileIndex));
        Tile tile = (Tile)_tiles.get(key);
        if (tile == null) {
            // create and initialize the tile object
            tile = createTile(tileIndex, getTileMirage(tileIndex));
            initTile(tile);
            _tiles.put(key, tile);
        }

        return tile;
    }

    /**
     * Returns the entire, raw, uncut, unprepared tileset source image.
     */
    public BufferedImage getTileSetImage ()
    {
        return _improv.getTileSetImage(_imagePath, _zations);
    }

    /**
     * Returns the raw (unprepared) image that would be used by the tile
     * at the specified index.
     */
    public BufferedImage getTileImage (int tileIndex)
        throws NoSuchTileException
    {
        checkTileIndex(tileIndex);
        Rectangle bounds = computeTileBounds(tileIndex);
        BufferedImage timg = getTileSetImage();
        return timg.getSubimage(bounds.x, bounds.y,
                                bounds.width, bounds.height);
    }

    /**
     * Returns a prepared version of the image that would be used by the
     * tile at the specified index. Because tilesets are often used simply
     * to provide access to a collection of uniform images, this method is
     * provided to bypass the creation of a {@link Tile} object when all
     * that is desired is access to the underlying image.
     */
    public Mirage getTileMirage (int tileIndex)
        throws NoSuchTileException
    {
        checkTileIndex(tileIndex);
        Rectangle bounds = computeTileBounds(tileIndex);
        if (_improv == null) {
            Log.warning("Aiya! Tile set missing image provider " +
                        "[path=" + _imagePath + "].");
        }
        return _improv.getTileImage(_imagePath, bounds, _zations);
    }

    /**
     * Used to ensure that the specified tile index is valid.
     */
    protected void checkTileIndex (int tileIndex)
        throws NoSuchTileException
    {
	// bail if there's no such tile
        int tcount = getTileCount();
	if (tileIndex < 0 || tileIndex >= tcount) {
	    throw new NoSuchTileException(tileIndex);
	}
    }

    /**
     * Computes and returns the bounds for the specified tile based on the
     * mechanism used by the derived class to do such things. The width
     * and height of the bounds should be the size of the tile image and
     * the x and y offset should be the offset in the tileset image for
     * the image data of the specified tile.
     *
     * @param tileIndex the index of the tile whose bounds are to be
     * computed.
     */
    protected abstract Rectangle computeTileBounds (int tileIndex);

    /**
     * Creates a tile for the specified tile index.
     *
     * @param tileIndex the index of the tile to be created.
     * @param image the tile image in the form of a {@link Mirage}.
     *
     * @return a configured tile.
     */
    protected Tile createTile (int tileIndex, Mirage image)
    {
        return new Tile(image);
    }

    /**
     * Initializes the supplied tile. Derived classes can override this
     * method to add in their own tile information, but should be sure to
     * call <code>super.initTile()</code>.
     *
     * @param tile the tile to initialize.
     */
    protected void initTile (Tile tile)
    {
	// nothing for now
    }

    /**
     * Generates a string representation of the tileset information.
     */
    public String toString ()
    {
	StringBuffer buf = new StringBuffer("[");
        toString(buf);
	return buf.append("]").toString();
    }

    /**
     * Derived classes can override this, calling
     * <code>super.toString(buf)</code> and then appending additional
     * information to the buffer.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append("name=").append(_name);
	buf.append(", path=").append(_imagePath);
	buf.append(", tileCount=").append(getTileCount());
    }

    /** The path to the file containing the tile images. */
    protected String _imagePath;

    /** The tileset name. */
    protected String _name;

    /** Colorizations to be applied to tiles created from this tileset. */
    protected transient Colorization[] _zations;

    /** The entity from which we obtain our tile image. */
    protected transient ImageProvider _improv;

    /** Increase this value when object's serialized state is impacted by
     * a class change (modification of fields, inheritance). */
    private static final long serialVersionUID = 1;

    /** Temporary HACKOLA. */
    protected static Colorization[] ZATIONS = new Colorization[] {
        new Colorization(-1, new Color(0x8A1A76),
                         new float[] { .1f, .3f, 0.6f },
                         new float[] { -.85f, .3f, 0f }),
        new Colorization(-1, new Color(0x2A864E),
                         new float[] { .1f, .3f, 0.6f },
                         new float[] { .71f, 0f, .07f }),
    };

    /** A weak cache of our tiles. */
    protected static LRUHashMap _tiles;

    /** Register our tile cache size with the runtime adjustments
     * framework. */
    protected static RuntimeAdjust.IntAdjust _cacheSize =
        new RuntimeAdjust.IntAdjust(
            "Size (in kb of memory used) of the tile LRU cache " +
            "[requires restart]", "narya.media.tile.cache_size",
            MediaPrefs.config, 1024);
}
