package com.example.pabloandtyler.comp512app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class PeerDataItem{

    private String authToken;
    private String endpointId;
    private String friendlyName;

    public PeerDataItem(String authToken, String endpointId, String friendlyName) {
        this.authToken = authToken;
        this.endpointId = endpointId;
        this.friendlyName = friendlyName;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getFriendlyName(){return friendlyName;}

    @Override
    public String toString(){
        return getFriendlyName();
        //return getEndpointId() + "," + getAuthToken() + "," + getFriendlyName();
    }


}
