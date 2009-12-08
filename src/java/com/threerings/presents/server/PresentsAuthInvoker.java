//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2009 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.presents.server;

import java.util.concurrent.Executor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * A separate invoker thread on which we perform client authentication. This allows the normal
 * server operation to proceed even in the event that our authentication services have gone down
 * and attempts to authenticate cause long timeouts and blockage.
 */
@Singleton
public class PresentsAuthInvoker extends ReportingInvoker
{
    @Inject public PresentsAuthInvoker (PresentsDObjectMgr omgr, ReportManager repmgr)
    {
        super("presents.AuthInvoker", (Executor)omgr, repmgr);
        setDaemon(true);
    }
}
