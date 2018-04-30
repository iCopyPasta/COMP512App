package com.example.pabloandtyler.comp512app;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Helper class for the full representation of a peer in the game, containing all the information
 * about a peer such as level and name.
 */
public class PeerState implements Comparable<PeerState>{

    // local variables with annotation for Gson conversion
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

    // constructor
    public PeerState(){
        this.friendlyName = "";
        this.levelOfPeer = 4;
        this.endpointId = "";
        positionInBonusRound = 0;
    }

    // getters and setters
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

    /**
     * toString is overridden to return custom information about a peer object
     * @return a String with the information about the endpointId, friendlyName, and levelOfPeer
     */
    @Override
    public String toString(){
        return "endpointId: " + endpointId + " friendly_name = " + friendlyName
                + " levelOfPeer = " + levelOfPeer;
    }

    /**
     * check the equality of two peerState objects
     * @param peerState peerState object to compared against
     * @return returns true or false depending on exact informational equality
     */
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

    /**
     * lexicographical sorting based on names
     * @param peerState peerState object to compare sort order
     * @return int representing the order of this peer state and another
     */
    @Override
    public int compareTo(@NonNull PeerState peerState) {
        return getFriendlyName().compareTo(peerState.getFriendlyName());
    }
}