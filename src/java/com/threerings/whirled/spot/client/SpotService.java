//
// $Id: SpotService.java,v 1.4 2001/12/16 21:02:18 mdb Exp $

package com.threerings.whirled.spot.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationDirector;

import com.threerings.whirled.client.SceneDirector;

import com.threerings.whirled.spot.Log;

/**
 * Provides a mechanism by which the client can request to move between
 * locations within a scene and between scenes (taking exit and entry
 * locations into account). These services should not be used directly,
 * but instead should be accessed via the {@link SpotSceneDirector}.
 */
public class SpotService implements SpotCodes
{
    /**
     * Requests to traverse the specified portal.
     */
    public static void traversePortal (
        Client client, int sceneId, int portalId, int sceneVer,
        SceneDirector rsptarget)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Object[] args = new Object[] {
            new Integer(sceneId), new Integer(portalId),
            new Integer(sceneVer) };
        invdir.invoke(MODULE_NAME, TRAVERSE_PORTAL_REQUEST, args, rsptarget);
        Log.info("Sent traversePortal request [sceneId=" + sceneId +
                 ", portalId=" + portalId + ", sceneVer=" + sceneVer + "].");
    }

    /**
     * Requests that this client's body be made to occupy the specified
     * location.
     */
    public static void changeLoc (Client client, int sceneId, int locationId,
                                  SpotSceneDirector rsptarget)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Object[] args = new Object[] { new Integer(sceneId),
                                       new Integer(locationId) };
        invdir.invoke(MODULE_NAME, CHANGE_LOC_REQUEST, args, rsptarget);
        Log.info("Sent changeLoc request [sceneId=" + sceneId +
                 ", locId=" + locationId + "].");
    }

    /**
     * Requests that the supplied message be delivered to listeners in the
     * cluster to which the specified location belongs.
     */
    public static void clusterSpeak (
        Client client, int sceneId, int locationId, String message,
        SpotSceneDirector rsptarget)
    {
        InvocationDirector invdir = client.getInvocationDirector();
        Object[] args = new Object[] {
            new Integer(sceneId), new Integer(locationId), message };
        invdir.invoke(MODULE_NAME, CLUSTER_SPEAK_REQUEST, args, rsptarget);
        Log.info("Sent clusterSpeak request [sceneId=" + sceneId +
                 ", locId=" + locationId + ", message=" + message + "].");
    }
}
