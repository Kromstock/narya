//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.samskivert.util.StringUtil;
import com.samskivert.util.Tuple;

import com.threerings.io.Intern;
import com.threerings.util.StreamableTuple;

/**
 * Tests the {@link Streamable} class.
 */
public class StreamableTest
{
    public static class Widget extends SimpleStreamableObject
        implements Cloneable
    {
        public boolean bool1 = true;
        public byte byte1 = Byte.MAX_VALUE;
        public char char1 = 'a';
        public short short1 = Short.MAX_VALUE;
        public int int1 = Integer.MAX_VALUE;
        public long long1 = Long.MAX_VALUE;
        public float float1 = Float.MAX_VALUE;
        public double double1 = Double.MAX_VALUE;

        public Boolean boxedBool = true;
        public Byte boxedByte = Byte.MAX_VALUE;
        public Character boxedChar = 'a';
        public Short boxedShort = Short.MAX_VALUE;
        public Integer boxedInt = Integer.MAX_VALUE;
        public Long boxedLong = Long.MAX_VALUE;
        public Float boxedFloat = Float.MAX_VALUE;
        public Double boxedDouble = Double.MAX_VALUE;

        public Boolean nullBoxedBool;
        public Byte nullBoxedByte;
        public Character nullBoxedChar;
        public Short nullBoxedShort;
        public Integer nullBoxedInt;
        public Long nullBoxedLong;
        public Float nullBoxedFloat;
        public Double nullBoxedDouble;

        public String string1 = "one";
        public String nullString1;

        @Intern public String internedString1 = "monkey butter";
        @Intern public String nullInternedString1;

        public Date date1 = new Date(42L);
        // public Date nullDate1; // null Date is apparently not supported, interesting!

        // note: must be a Streamable class
        public Class<?> class1 = Widget.class;
        public Class<?> nullClass1;

        public boolean[] bools = new boolean[] { true, false, true };
        public byte[] bytes = new byte[] { Byte.MAX_VALUE, 2, 3 };
        public short[] shorts = new short[] { Short.MAX_VALUE, 2, 3 };
        public char[] chars = new char[] { 'a', 'b', 'c' };
        public int[] ints = new int[] { Integer.MAX_VALUE, 2, 3 };
        public long[] longs = new long[] { Long.MAX_VALUE, 2, 3 };
        public float[] floats = new float[] { Float.MAX_VALUE, 2, 3 };
        public double[] doubles = new double[] { Double.MAX_VALUE, 2, 3 };

        public boolean[] nullBools;
        public byte[] nullBytes;
        public short[] nullShorts;
        public char[] nullChars;
        public int[] nullInts;
        public long[] nullLongs;
        public float[] nullFloats;
        public double[] nullDoubles;

        public Wocket wocket1 = new Wocket();
        public Wocket[] wockets = new Wocket[] { new Wocket(), new Wocket() };
        public Wicket[] wickets = new Wicket[] { new Wicket(), new Wicket(), new Wicket() };

        public Wocket nullWocket1;
        public Wocket[] nullWockets;
        public Wicket[] nullWickets;

        public Wackable[] wackables = new Wackable[] { new Wacket("for"), new Wacket("sure") };

        // it's legal for an Object field to contain a Streamable reference
        public Object object1 = new Wocket();
        public Object nullObject1 = new Wocket();

        // it's legal for an Object[] field to contain an array of Streamable references; NOTE:
        // this will come back as "new Object[] { new Wocket(), new Wocket() }", so it's not kosher
        // to rely on the type of your array being preserved, only the type of its contents
        public Object[] objects = new Wocket[] { new Wocket(), new Wocket() };
        public Object[] nullObjects;

        public List<Integer> list = Lists.newArrayList(1, 2, 3);
        public List<Integer> nullList = null;
        public ArrayList<Integer> arrayList = Lists.newArrayList(3, 2, 1);
        public ArrayList<Integer> nullArrayList = null;

        public Collection<Integer> collection = Arrays.asList(4, 5, 6);
        public Collection<Integer> nullCollection = null;

        public Set<Integer> set = Sets.newHashSet(ImmutableSet.of(6, 7, 8));
        public Set<Integer> nullSet = null;
        public HashSet<Integer> hashSet = Sets.newHashSet(ImmutableSet.of(7, 8, 9));
        public HashSet<Integer> nullHashSet = null;

        public Map<Integer, String> map = ImmutableMap.of(1, "one", 2, "two", 3, "three");
        public Map<Integer, String> nullMap = null;
        public HashMap<String, Integer> hashMap = Maps.newHashMap(
            ImmutableMap.of("one", 1, "two", 2, "three", 3));
        public HashMap<String, Integer> nullHashMap = null;

        @Override
        public boolean equals (Object other) {
            if (!(other instanceof Widget)) {
                return false;
            }
            Widget ow = (Widget)other;
            return bool1 == ow.bool1 &&
                byte1 == ow.byte1 &&
                char1 == ow.char1 &&
                short1 == ow.short1 &&
                int1 == ow.int1 &&
                long1 == ow.long1 &&
                float1 == ow.float1 &&
                double1 == ow.double1 &&

                Objects.equal(boxedBool, ow.boxedBool) &&
                Objects.equal(boxedByte, ow.boxedByte) &&
                Objects.equal(boxedShort, ow.boxedShort) &&
                Objects.equal(boxedChar, ow.boxedChar) &&
                Objects.equal(boxedInt, ow.boxedInt) &&
                Objects.equal(boxedLong, ow.boxedLong) &&
                Objects.equal(boxedFloat, ow.boxedFloat) &&
                Objects.equal(boxedDouble, ow.boxedDouble) &&

                Objects.equal(nullBoxedBool, ow.nullBoxedBool) &&
                Objects.equal(nullBoxedByte, ow.nullBoxedByte) &&
                Objects.equal(nullBoxedShort, ow.nullBoxedShort) &&
                Objects.equal(nullBoxedChar, ow.nullBoxedChar) &&
                Objects.equal(nullBoxedInt, ow.nullBoxedInt) &&
                Objects.equal(nullBoxedLong, ow.nullBoxedLong) &&
                Objects.equal(nullBoxedFloat, ow.nullBoxedFloat) &&
                Objects.equal(nullBoxedDouble, ow.nullBoxedDouble) &&

                Objects.equal(string1, ow.string1) &&
                Objects.equal(nullString1, ow.nullString1) &&

                internedString1 == ow.internedString1 &&
                nullInternedString1 == ow.nullInternedString1 &&

                Objects.equal(date1, ow.date1) &&
                // Objects.equal(nullDate1, ow.nullDate1) &&

                class1 == ow.class1 &&
                nullClass1 == ow.nullClass1 &&

                Arrays.equals(bools, ow.bools) &&
                Arrays.equals(bytes, ow.bytes) &&
                Arrays.equals(shorts, ow.shorts) &&
                Arrays.equals(chars, ow.chars) &&
                Arrays.equals(ints, ow.ints) &&
                Arrays.equals(longs, ow.longs) &&
                Arrays.equals(floats, ow.floats) &&
                Arrays.equals(doubles, ow.doubles) &&

                Arrays.equals(nullBools, ow.nullBools) &&
                Arrays.equals(nullBytes, ow.nullBytes) &&
                Arrays.equals(nullShorts, ow.nullShorts) &&
                Arrays.equals(nullChars, ow.nullChars) &&
                Arrays.equals(nullInts, ow.nullInts) &&
                Arrays.equals(nullLongs, ow.nullLongs) &&
                Arrays.equals(nullFloats, ow.nullFloats) &&
                Arrays.equals(nullDoubles, ow.nullDoubles) &&

                Objects.equal(wocket1, ow.wocket1) &&
                Arrays.equals(wockets, ow.wockets) &&
                Arrays.equals(wickets, ow.wickets) &&

                Objects.equal(nullWocket1, ow.nullWocket1) &&
                Arrays.equals(nullWockets, ow.nullWockets) &&
                Arrays.equals(nullWickets, ow.nullWickets) &&

                Arrays.equals(wackables, ow.wackables) &&

                Objects.equal(object1, ow.object1) &&
                Objects.equal(nullObject1, ow.nullObject1) &&
                Arrays.equals(objects, ow.objects) &&
                Arrays.equals(nullObjects, ow.nullObjects) &&

                Objects.equal(list, ow.list) &&
                Objects.equal(nullList, ow.nullList) &&
                Objects.equal(arrayList, ow.arrayList) &&
                Objects.equal(nullArrayList, ow.nullArrayList) &&

                Objects.equal(collection, ow.collection) &&
                Objects.equal(nullCollection, ow.nullCollection) &&

                Objects.equal(set, ow.set) &&
                Objects.equal(nullSet, ow.nullSet) &&
                Objects.equal(hashSet, ow.hashSet) &&
                Objects.equal(nullHashSet, ow.nullHashSet) &&

                Objects.equal(map, ow.map) &&
                Objects.equal(nullMap, ow.nullMap) &&
                Objects.equal(hashMap, ow.hashMap) &&
                Objects.equal(nullHashMap, ow.nullHashMap) &&

                true; // handy end-of-chain
        }

        @Override
        public Widget clone ()
        {
            try {
                return (Widget)super.clone();
            } catch (CloneNotSupportedException cnse) {
                throw new AssertionError(cnse);
            }
        }
    }

    public static class Wocket extends SimpleStreamableObject
    {
        public byte bizyte = 15;
        public short shizort = Short.MAX_VALUE;
        public double dizouble = Math.PI;

        @Override
        public boolean equals (Object other) {
            if (!(other instanceof Wocket)) {
                return false;
            }
            Wocket ow = (Wocket)other;
            return bizyte == ow.bizyte &&
                shizort == ow.shizort &&
                dizouble == ow.dizouble;
        }
    }

    public static final class Wicket extends SimpleStreamableObject
    {
        public byte bizyte = 15;
        public short shizort = Short.MAX_VALUE;
        public double dizouble = Math.PI;

        public void writeObject (ObjectOutputStream out)
            throws IOException
        {
            out.defaultWriteObject();
            out.writeInt(_fizzle);
        }

        public void readObject (ObjectInputStream in)
            throws IOException, ClassNotFoundException
        {
            in.defaultReadObject();
            _fizzle = in.readInt();
        }

        @Override
        public boolean equals (Object other) {
            if (!(other instanceof Wicket)) {
                return false;
            }
            Wicket ow = (Wicket)other;
            return bizyte == ow.bizyte &&
                shizort == ow.shizort &&
                dizouble == ow.dizouble &&
                _fizzle == ow._fizzle;
        }

        @Override
        protected void toString (StringBuilder buf)
        {
            super.toString(buf);
            buf.append(", fizzle=").append(_fizzle);
        }

        protected int _fizzle = 19;
    }

    /**
     * An interface that extends Streamable.
     */
    public static interface Wackable extends Streamable
    {
        /**
         * You shall never taste my ambrosia.
         */
        void wack ();
    }

    public static class Wacket
        implements Wackable
    {
        public String strizzing;

        public Wacket (String s)
        {
            this.strizzing = (s == null) ? null : (s + "-izzle");
        }

        // from Wackable
        public void wack ()
        {
            // *burp*
        }

        @Override
        public boolean equals (Object other)
        {
            if (!(other instanceof Wacket)) {
                return false;
            }
            Wacket that = (Wacket)other;
            return Objects.equal(this.strizzing, that.strizzing);
        }

        @Override
        public int hashCode ()
        {
            return (strizzing == null) ? 0 : strizzing.hashCode();
        }

        @Override
        public String toString ()
        {
            return "Wacket[" + strizzing + "]";
        }
    }

    @Test
    public void testFields ()
        throws IOException, ClassNotFoundException
    {
        Widget w = new Widget();
        assertEquals(w, unflatten(flatten(w)));
    }

    @Test
    public void testWireFormat ()
        throws IOException, ClassNotFoundException
    {
        Widget w = new Widget();

        // make sure that we serialize to the expected stream of bytes
        byte[] data = flatten(w);

        // uncomment this and rerun the tests to generate an updated WIRE_DATA blob
        // printWireData(w);

        // oddly, JUnit doesn't like comparing byte arrays directly (this fails:
        // assertEquals(WIRE_DATA, WIRE_DATA.clone())), but comparing strings is fine
        assertEquals(StringUtil.hexlate(data), StringUtil.hexlate(WIRE_DATA));

        // make sure that we unserialize a known stream of bytes to the expected object
        assertEquals(w, unflatten(WIRE_DATA));
    }

    protected void printWireData (Object o)
        throws IOException
    {
        String dstr = StringUtil.wordWrap(StringUtil.hexlate(flatten(o)), 80);
        dstr = StringUtil.join(dstr.split("\n"), "\" +\n        \"");
        System.out.println("    protected static final byte[] WIRE_DATA = "
            + "StringUtil.unhexlate(\n        \"" + dstr + "\");");
    }

    @Test
    public void testPostStreamingMutation ()
        throws IOException, ClassNotFoundException
    {
        // create an object graph to be streamed
        Widget w = new Widget();

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        Widget w1 = w.clone();
        oout.writeObject(w);
        w.string1 = "two";
        Widget w2 = w.clone();
        oout.writeObject(w);
        w.string1 = "three";
        Widget w3 = w.clone();
        oout.writeObject(w);
        Tuple<String,String> tup = StreamableTuple.newTuple("left", "right");
        oout.writeObject(tup);

        byte[] data = bout.toByteArray();
        // System.out.println(data.length + " bytes were written.");

        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        ObjectInputStream oin = new ObjectInputStream(bin);
        Object ow1 = oin.readObject(); // widget
        Object ow2 = oin.readObject(); // modified widget
        Object ow3 = oin.readObject(); // again modified widget
        Object otup = oin.readObject(); // streamable tuple

        assertEquals(w1, ow1);
        assertEquals(w2, ow2);
        assertEquals(w3, ow3);
        assertEquals(tup, otup);
    }

    @Test(expected=RuntimeException.class)
    public void testUnlabledClosureFail ()
        throws IOException, ClassNotFoundException
    {
        abstract class Action implements Streamable {
            public abstract String act ();
        }
        Action act = new Action() {
            @Override
            public String act () {
                return "bang!";
            }
        };
        Action react = (Action)unflatten(flatten(act));
        assertEquals(act.act(), react.act());
    }

    @Test
    public void testClosure ()
        throws IOException, ClassNotFoundException
    {
        abstract class Action implements Streamable.Closure {
            public abstract String act ();
        }
        final int count = unsafeOuterCall();
        final String arg = "hello";
        Action act = new Action() {
            @Override
            public String act () {
                return count + ":" + arg;
            }
        };
        Action react = (Action)unflatten(flatten(act));
        assertEquals(act.act(), react.act());
    }

    // unfortunately we can't warn you if you do something naughty in your closure, but since we
    // flatten and unflatten closures even when running on the local peer, the programmer should
    // find about about funny business early enough
    @Test(expected=NullPointerException.class)
    public void testNaughtyClosureFail ()
        throws IOException, ClassNotFoundException
    {
        abstract class Action implements Streamable.Closure {
            public abstract String act ();
        }
        final String arg = "hello";
        Action act = new Action() {
            @Override
            public String act () {
                return unsafeOuterCall() + ":" + arg;
            }
        };
        Action react = (Action)unflatten(flatten(act));
        assertEquals(act.act(), react.act());
    }

    protected static class LegacyClosure implements Streamable.Closure
    {
        public int arg;
        public LegacyClosure (int arg) {
            this.arg = arg;
        }
        public LegacyClosure () {
        }
    }

    @Test
    public void testLegacyZeroMultipleCtorHavingClosure ()
        throws IOException, ClassNotFoundException
    {
        LegacyClosure inst = new LegacyClosure(42);
        LegacyClosure reinst = (LegacyClosure)unflatten(flatten(inst));
        assertEquals(inst.arg, reinst.arg);
    }

    protected int unsafeOuterCall ()
    {
        return 42;
    }

    protected static byte[] flatten (Object object)
        throws IOException
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(object);
        return bout.toByteArray();
    }

    protected static Object unflatten (byte[] data)
        throws IOException, ClassNotFoundException
    {
        return new ObjectInputStream(new ByteArrayInputStream(data)).readObject();
    }

    protected static final byte[] WIRE_DATA = StringUtil.unhexlate(
        "ffff0027636f6d2e746872656572696e67732e696f2e53747265616d61626c655465737424576964" +
        "676574017f00617fff7fffffff7fffffffffffffff7f7fffff7fefffffffffffff0101017f010061" +
        "017fff017fffffff017fffffffffffffff017f7fffff017fefffffffffffff000000000000000001" +
        "00036f6e6500ffff000d6d6f6e6b6579206275747465720000000000000000002a01000100010000" +
        "000301000101000000037f020301000000037fff0002000301000000030061006200630100000003" +
        "7fffffff000000020000000301000000037fffffffffffffff000000000000000200000000000000" +
        "0301000000037f7fffff400000004040000001000000037fefffffffffffff400000000000000040" +
        "080000000000000000000000000000fffe0027636f6d2e746872656572696e67732e696f2e537472" +
        "65616d61626c655465737424576f636b65740f7fff400921fb54442d18fffd002a5b4c636f6d2e74" +
        "6872656572696e67732e696f2e53747265616d61626c655465737424576f636b65743b0000000200" +
        "020f7fff400921fb54442d1800020f7fff400921fb54442d18fffc002a5b4c636f6d2e7468726565" +
        "72696e67732e696f2e53747265616d61626c6554657374245769636b65743b000000030001070f7f" +
        "ff400921fb54442d1800000013000000130f7fff400921fb54442d1800000013000000130f7fff40" +
        "0921fb54442d180000001300000013000000000000fffb002c5b4c636f6d2e746872656572696e67" +
        "732e696f2e53747265616d61626c6554657374245761636b61626c653b00000002fffa0027636f6d" +
        "2e746872656572696e67732e696f2e53747265616d61626c6554657374245761636b657401000966" +
        "6f722d697a7a6c65000601000a737572652d697a7a6c6500020f7fff400921fb54442d1800020f7f" +
        "ff400921fb54442d18010000000200020f7fff400921fb54442d1800020f7fff400921fb54442d18" +
        "000100000003fff900116a6176612e6c616e672e496e746567657200000001000700000002000700" +
        "00000300010000000300070000000300070000000200070000000100010000000300070000000400" +
        "07000000050007000000060001000000030007000000060007000000070007000000080001000000" +
        "03000700000007000700000008000700000009000100000003000700000001fff800106a6176612e" +
        "6c616e672e537472696e6700036f6e650007000000020008000374776f0007000000030008000574" +
        "687265650001000000030008000374776f000700000002000800036f6e6500070000000100080005" +
        "746872656500070000000300");
}
