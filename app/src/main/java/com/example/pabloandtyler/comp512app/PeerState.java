package com.example.pabloandtyler.comp512app;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PeerState implements Comparable<PeerState>{

    @SerializedName("friendlyName")
    @Expose
    private String friendlyName;
    @SerializedName("levelOfPeer")
    @Expose
    private Integer levelOfPeer;
    @SerializedName("endpointId")
    @Expose
    private String endpointId;

    @SerializedName("positionInBonusRound")
    @Expose
    private int positionInBonusRound;

    public PeerState(){
        this.friendlyName = "";
        this.levelOfPeer = 4;
        this.endpointId = "";
        positionInBonusRound = 0;
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

    public int getPositionInBonusRound() {
        return positionInBonusRound;
    }

    public void setPositionInBonusRound(int positionInBonusRound) {
        this.positionInBonusRound = positionInBonusRound;
    }

    @Override
    public String toString(){
        return "endpointId: " + endpointId + " friendly_name = " + friendlyName
                + " levelOfPeer = " + levelOfPeer;
    }

    @Override
    public boolean equals(Object peerState){

        if(peerState instanceof PeerState){
            return this.getLevelOfPeer().equals( ((PeerState) peerState).getLevelOfPeer())
                    && this.getFriendlyName().equals(((PeerState) peerState).getFriendlyName())
                    && this.getEndpointId().equals(((PeerState) peerState).getEndpointId())
                    && this.getPositionInBonusRound() == ((PeerState) peerState).getPositionInBonusRound();
        }

        return false;

    }

    @Override
    public int compareTo(@NonNull PeerState peerState) {
        return getFriendlyName().compareTo(peerState.getFriendlyName());
    }
}