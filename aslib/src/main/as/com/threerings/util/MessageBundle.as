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

package com.threerings.util {

import mx.resources.IResourceBundle;
import mx.resources.IResourceManager;
import mx.resources.ResourceManager;

/**
 * A message bundle provides an easy mechanism by which to obtain
 * translated message strings from a resource bundle. It uses the {@link
 * MessageFormat} class to substitute arguments into the translation
 * strings. Message bundles would generally be obtained via the {@link
 * MessageManager}, but could be constructed individually if so desired.
 */
public class MessageBundle
{
    /**
     * Initializes the message bundle which will obtain localized messages
     * from the supplied resource bundle. The path is provided purely for
     * reporting purposes.
     */
    public function init (msgmgr :MessageManager, path :String, parent :MessageBundle) :void
    {
        _msgmgr = msgmgr;
        _path = path;
        _parent = parent;
    }

    /**
     * Get the path to this bundle.
     */
    public function getPath () :String
    {
        return _path;
    }

    /**
     * Returns true if we have a translation mapping for the supplied key,
     * false if not.
     */
    public function exists (key :String) :Boolean
    {
        return getResourceString(key, false) != null;
    }

    /**
     * Get all the messages that begin with the specified prefix.
     *
     * @param includeParent if true, messages from our parent bundle (and its
     * parent bundle, all the way up the chain will be included).
     */
    public function getAll (prefix :String, includeParent :Boolean = true) :Array
    {
        var messages :Array = []
        for each (var value :Object in getAllMapped(prefix, includeParent)) {
            messages.push(value);
        }
        return messages;
    }

    /**
     * Get all the messages and their keys that begin with the specified prefix.
     */
    public function getAllMapped (prefix :String, includeParent :Boolean = true) :Object
    {
        var mgr :IResourceManager = ResourceManager.getInstance();

        // search all locales, using the first one first, but including any
        // non-overridden keys from backing locales
        var messages :Object = {};
        var key :String;
        for each (var locale :String in mgr.getLocales()) {
            var bundle :IResourceBundle = mgr.getResourceBundle(locale, _path);
            if (bundle != null) {
                for (key in bundle.content) {
                    // preserve the message found in an earlier locale
                    if (StringUtil.startsWith(key, prefix) && !(key in messages)) {
                        messages[key] = bundle.content[key];
                    }
                }
            }
        }

        if (includeParent && _parent != null) {
            // don't let parent messages overwrite any we've found
            var parentMap :Object = _parent.getAllMapped(prefix, includeParent);
            for (key in parentMap) {
                if (!(key in messages)) {
                    messages[key] = parentMap[key];
                }
            }
        }
        return messages;
    }

    /**
     * Get a String from the resource bundle, or null if there was an
     * error.
     *
     * @param key the resource key.
     * @param reportMissing whether or not the method should log an error
     * if the resource didn't exist.
     */
    protected function getResourceString (key :String, reportMissing :Boolean = true) :String
    {
        var value :String = ResourceManager.getInstance().getString(_path, key);

        if (value == null) {
            // if we have a parent, try getting the string from them
            if (_parent != null) {
                value = _parent.getResourceString(key, false);
            }
            // if we didn't find it in our parent, we want to fall
            // through and report missing appropriately
            if (value == null && reportMissing) {
                Log.getLog(this).warning("Missing translation message",
                    "bundle", _path, "key", key);
            }
        }

        return value;
    }

    /**
     * Obtains the translation for the specified message key. The
     * specified arguments are substituted into the translated string.
     *
     * <p> If the first argument in the array is an {@link Integer}
     * object, a translation will be selected accounting for plurality in
     * the following manner. Assume a message key of
     * <code>m.widgets</code>, the following translations should be
     * defined:
     * <pre>
     * m.widgets.0 = no widgets.
     * m.widgets.1 = {0} widget.
     * m.widgets.n = {0} widgets.
     * </pre>
     *
     * The specified argument is substituted into the translated string as
     * appropriate. Consider using:
     *
     * <pre>
     * m.widgets.n = {0,number,integer} widgets.
     * </pre>
     *
     * to obtain proper insertion of commas and dots as appropriate for
     * the locale.
     *
     * <p> See {@link MessageFormat} for more information on how the
     * substitution is performed. If a translation message does not exist
     * for the specified key, an error is logged and the key itself (plus
     * the arguments) is returned so that the caller need not worry about
     * handling a null response.
     */
    public function get (key :String, ... args) :String
    {
        // if this string is tainted, we don't translate it, instead we
        // simply remove the taint character and return it to the caller
        if (key.charAt(0) === TAINT_CHAR) {
            return key.substring(1);
        }

        args = Util.unfuckVarargs(args);

        // if this is a qualified key, we need to pass the buck to the
        // appropriate message bundle
        if (key.indexOf(QUAL_PREFIX) == 0) {
            var qbundle :MessageBundle = _msgmgr.getBundle(getBundle(key));
            return qbundle.get(getUnqualifiedKey(key), args);
        }

        // Select the proper suffix if our first argument can be coaxed into an integer
        var suffix :String = getSuffix(args);
        var msg :String = getResourceString(key + suffix, false);

        if (msg == null) {
            if (suffix != "") {
                // Try the original key
                msg = getResourceString(key, false);
            }

            // if the msg is still missing, we have a problem
            if (msg == null) {
                Log.getLog(this).warning("Missing translation message",
                    "bundle", _path, "key", key);

                // return something bogus
                return (key + args);
            }
        }

        return StringUtil.substitute(msg, args);
    }

    /**
     * A helper function for {@link #get(String,Object[])} that allows us
     * to automatically perform plurality processing if our first argument
     * can be coaxed to an {@link Integer}.
     */
    protected function getSuffix (args :Array) :String
    {
        if (args.length > 0 && args[0] != null) {
            try {
                var count :int = (args[0] is int) ? int(args[0]) :
                    StringUtil.parseInteger(String(args[0]));
                switch (count) {
                    case 0: return ".0";
                    case 1: return ".1";
                    default: return ".n";
                }
            } catch (err :ArgumentError) {
                // fall out
            }
        }
        return "";
    }

    /**
     * Obtains the translation for the specified compound message key. A
     * compound key contains the message key followed by a tab separated
     * list of message arguments which will be subsituted into the
     * translation string.
     *
     * <p> See {@link MessageFormat} for more information on how the
     * substitution is performed. If a translation message does not exist
     * for the specified key, an error is logged and the key itself (plus
     * the arguments) is returned so that the caller need not worry about
     * handling a null response.
     */
    public function xlate (compoundKey :String) :String
    {
        // if this is a qualified key, we need to pass the buck to the
        // appropriate message bundle; we have to do it here because we
        // want the compound arguments of this key to be translated in the
        // context of the containing message bundle qualification
        if (compoundKey.indexOf(QUAL_PREFIX) == 0) {
            var qbundle :MessageBundle = _msgmgr.getBundle(
                getBundle(compoundKey));
            return qbundle.xlate(getUnqualifiedKey(compoundKey));
        }

        // to be more efficient about creating unnecessary objects, we
        // do some checking before splitting
        var tidx :int = compoundKey.indexOf("|");
        if (tidx == -1) {
            return get(compoundKey);

        } else {
            var key :String = compoundKey.substring(0, tidx);
            var argstr :String = compoundKey.substring(tidx+1);
            var args :Array = argstr.split("|");
            // unescape and translate the arguments
            for (var ii :int = 0; ii < args.length; ii++) {
                // if the argument is tainted, do no further translation
                // (it might contain |s or other fun stuff)
                if (args[ii].indexOf(TAINT_CHAR) == 0) {
                    args[ii] = unescape(args[ii].substring(1));
                } else {
                    args[ii] = xlate(unescape(args[ii]));
                }
            }
            return get(key, args);
        }
    }

    /**
     * Call this to "taint" any string that has been entered by an entity
     * outside the application so that the translation code knows not to
     * attempt to translate this string when doing recursive translations
     * (see {@link #xlate}).
     */
    public static function taint (text :Object) :String
    {
        return TAINT_CHAR + text;
    }

    /**
     * Composes a message key with an array of arguments. The message can
     * subsequently be translated in a single call using {@link #xlate}.
     */
    public static function compose (key :String, ... args) :String
    {
        args = Util.unfuckVarargs(args);

        var s :String = key + "|";
        for (var ii :int = 0; ii < args.length; ii++) {
            if (ii > 0) {
                s += "|";
            }
            var arg :String = String(args[ii]);
            for (var p :int = 0; p < arg.length; p++) {
                var ch :String = arg.charAt(p);
                if (ch == "|") {
                    s += "\\!";
                } else if (ch == "\\") {
                    s += "\\\\";
                } else {
                    s += ch;
                }
            }
        }
        return s;
    }

    /**
     * A convenience method for calling {@link #compose(String,Object[])}
     * with a single argument that will be automatically tainted (see
     * {@link #taint}).
     */
    public static function tcompose (key :String, ... args) :String
    {
        args = Util.unfuckVarargs(args);

        for (var ii :int = 0; ii < args.length; ii++) {
            args[ii] = taint(args[ii]);
        }
        return compose(key, args);
    }

    /**
     * Returns a fully qualified message key which, when translated by
     * some other bundle, will know to resolve and utilize the supplied
     * bundle to translate this particular key.
     */
    public static function qualify (bundle :String, key :String) :String
    {
        if (bundle.indexOf(QUAL_PREFIX) != -1 ||
                bundle.indexOf(QUAL_SEP) !=  -1) {
            throw new Error("Message bundle may not contain " + QUAL_PREFIX +
                " or " + QUAL_SEP);
        }

        return QUAL_PREFIX + bundle + QUAL_SEP + key;
    }

    /**
     * Returns the bundle name from a fully qualified message key.
     *
     * @see #qualify
     */
    public static function getBundle (qualifiedKey :String) :String
    {
        if (qualifiedKey.indexOf(QUAL_PREFIX) != 0) {
            throw new Error(qualifiedKey +
                " is not a fully qualified message key.");
        }
        var qsidx :int = qualifiedKey.indexOf(QUAL_SEP);
        if (qsidx == -1) {
            throw new Error(qualifiedKey +
                " is not a valid fully qualified key.");
        }

        return qualifiedKey.substring(QUAL_PREFIX.length, qsidx);
    }

    /**
     * Returns the unqualified portion of the key from a fully qualified
     * message key.
     *
     * @see #qualify
     */
    public static function getUnqualifiedKey (qualifiedKey :String) :String
    {
        if (qualifiedKey.indexOf(QUAL_PREFIX) != 0) {
            throw new Error(qualifiedKey +
                " is not a fully qualified message key.");
        }
        var qsidx :int = qualifiedKey.indexOf(QUAL_SEP);
        if (qsidx == -1) {
            throw new Error(qualifiedKey +
                " is not a fully qualified message key.");
        }
        return qualifiedKey.substring(qsidx + 1);
    }

    /**
     * Unescapes characters that are escaped in a call to compose.
     */
    public static function unescape (val :String) :String
    {
        var bsidx :int = val.indexOf("\\");
        if (bsidx == -1) {
            return val;
        }

        var s :String = "";
        for (var ii :int = 0; ii < val.length; ii++) {
            var ch :String = val.charAt(ii);
            if (ch != "\\" || ii == val.length-1) {
                s += ch;
            } else {
                // look at the next character
                ch = val.charAt(++ii);
                s += (ch == "!") ? "|" : ch;
            }
        }
        return s;
    }

    /** The message manager via whom we'll resolve fully qualified
     * translation strings. */
    protected var _msgmgr :MessageManager;

    /** The path that identifies the resource bundle we are using to
     * obtain our messages. */
    protected var _path :String;

    /** Our parent bundle if we're not the global bundle. */
    protected var _parent :MessageBundle;

    protected static const TAINT_CHAR :String = "~";
    protected static const QUAL_PREFIX :String = "%";
    protected static const QUAL_SEP :String = ":";
}
}
