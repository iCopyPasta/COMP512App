package com.example.pabloandtyler.comp512app;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PeerState{

    @SerializedName("friendlyName")
    @Expose
    private String friendlyName;
    @SerializedName("levelOfPeer")
    @Expose
    private Integer levelOfPeer;
    @SerializedName("endpointId")
    @Expose
    private String endpointId;

    public PeerState(){
        this.friendlyName = "";
        this.levelOfPeer = 4;
        this.endpointId = "";
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public Integer getLevelOfPeer() {
        return levelOfPeer;
    }

    public void setLevelOfPeer(Integer levelOfPeer) {
        this.levelOfPeer = levelOfPeer;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    @Override
    public String toString(){
        return "endpointId: " + endpointId + " friendly_name = " + friendlyName
                + " levelOfPeer = " + levelOfPeer;
    }




}