//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

package com.threerings.bureau.server;

import com.google.inject.Guice;
import com.google.inject.Injector;

import com.threerings.presents.server.PresentsServer;

import static com.threerings.bureau.Log.log;

/**
 * Extends a presents server to include a bureau registry.
 */
public class TestServer extends PresentsServer
{
    /**
     * The bureau registry for the server. Will be null until <code>init</code> is called.
     */
    public static BureauRegistry breg;

    /**
     * Creates a new server and runs it.
     */
    public static void main (String[] args)
    {
        Injector injector = Guice.createInjector(new Module());
        TestServer server = injector.getInstance(TestServer.class);
        try {
            server.init(injector);
            setClientTarget("bureau-runclient");
            server.run();

        } catch (Exception e) {
            log.warning("Unable to initialize server.", e);
        }
    }

    @Override // from PresentsServer
    public void init (Injector injector)
        throws Exception
    {
        super.init(injector);
        breg = new BureauRegistry("localhost:47624", invmgr, omgr, invoker);
    }
    
    public static void setClientTarget (String target)
    {
        breg.setCommandGenerator("test", antCommandGenerator(target));
    }
    
    public static BureauRegistry.CommandGenerator antCommandGenerator (
        final String target)
    {
        return new BureauRegistry.CommandGenerator() {
            public String[] createCommand (
                String serverNameAndPort, 
                String bureauId, 
                String token) {
                
                int colon = serverNameAndPort.indexOf(':');
                String [] cmd = {"ant", 
                    "-DserverName=" + serverNameAndPort.substring(0, colon),
                    "-DserverPort=" + serverNameAndPort.substring(colon + 1),
                    "-DbureauId=" + bureauId,
                    "-Dtoken=" + token,
                    target};

                return cmd;
            }
        };        
    }
}
