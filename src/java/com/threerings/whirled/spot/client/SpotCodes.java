//
// $Id: SpotCodes.java,v 1.3 2001/12/16 21:02:18 mdb Exp $

package com.threerings.whirled.spot.client;

import com.threerings.crowd.chat.ChatCodes;
import com.threerings.whirled.client.SceneCodes;

/**
 * Contains codes used by the Spot invocation services.
 */
public interface SpotCodes extends ChatCodes, SceneCodes
{
    /** The module name for the Spot services. */
    public static final String MODULE_NAME = "whirled!spot";

    /** The message identifier for a traversePortal request. Such a
     * request generates a moveTo response rather than a specialized
     * response. */
    public static final String TRAVERSE_PORTAL_REQUEST = "TraversePortal";

    /** An error code indicating that the portal specified in a
     * traversePortal request does not exist. */
    public static final String NO_SUCH_PORTAL = "m.no_such_portal";

    /** The message identifier for a changeLoc request. */
    public static final String CHANGE_LOC_REQUEST = "ChangeLoc";

    /** The response identifier for a successful changeLoc request. This
     * is mapped by the invocation services to a call to {@link
     * SpotSceneDirector#handleChangeLocSucceeded}. */
    public static final String CHANGE_LOC_SUCCEEDED_RESPONSE =
        "ChangeLocSucceeded";

    /** The response identifier for a failed changeLoc request. This is
     * mapped by the invocation services to a call to {@link
     * SpotSceneDirector#handleChangeLocFailed}. */
    public static final String CHANGE_LOC_FAILED_RESPONSE = "ChangeLocFailed";

    /** An error code indicating that a location is occupied. Usually
     * generated by a failed changeLoc request. */
    public static final String LOCATION_OCCUPIED = "m.location_occupied";

    /** The message identifier for a cluster speak request. */
    public static final String CLUSTER_SPEAK_REQUEST = "ClusterSpeak";
}
