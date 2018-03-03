package com.example.pabloandtyler.comp512app.dummy;

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

    public PeerDataItem(String authToken, String endpointId) {
        this.authToken = authToken;
        this.endpointId = endpointId;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public String getAuthToken() {
        return authToken;
    }

    @Override
    public String toString(){
        return getEndpointId() + "," + getAuthToken();
    }


}
