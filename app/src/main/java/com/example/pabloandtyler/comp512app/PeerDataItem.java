package com.example.pabloandtyler.comp512app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An item that is clickable within the ListView, for usage in PeerListItemsFragment
 */
public class PeerDataItem{

    // class variables
    private String authToken;
    private String endpointId;
    private String friendlyName;

    // constructor method
    public PeerDataItem(String authToken, String endpointId, String friendlyName) {
        this.authToken = authToken;
        this.endpointId = endpointId;
        this.friendlyName = friendlyName;
    }

    // getter methods
    public String getEndpointId() {
        return endpointId;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getFriendlyName(){return friendlyName;}

    /**
     *
     * @return String containing the friendly name a peers uses for identification
     */
    @Override
    public String toString(){
        return getFriendlyName();

    }


}
