package com.example.pabloandtyler.comp512app;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class GameStateContainer {

    @SerializedName("typeOfGame")
    @Expose
    private String typeOfGame;
    @SerializedName("peersLevel")
    @Expose
    private List<PeerState> peersLevel = null;

    public GameStateContainer(){
        this.typeOfGame = "N";
        this.peersLevel = new ArrayList<>();
    }

    public String getTypeOfGame() {
        return typeOfGame;
    }

    public void setTypeOfGame(String typeOfGame) {
        this.typeOfGame = typeOfGame;
    }

    public List<PeerState> getPeersLevel() {
        return peersLevel;
    }

    public void setPeersLevel(List<PeerState> peersLevel) {
        this.peersLevel = peersLevel;
    }

    public PeerState getPeerWithFriendlyName(String friendlyName) {
        for (PeerState element: peersLevel) {
            if (element.getFriendlyName().equals(friendlyName)) {
                return element;
            }
        }
        return null;
    }

    public PeerState getPeerWithEndpointId(String endpointId) {
        for (PeerState element: peersLevel) {
            if (element.getEndpointId().equals(endpointId)) {
                return element;
            }
        }
        return null;
    }

    public boolean contains(PeerState peer) {
        for (PeerState element: peersLevel) {
            if (element.getEndpointId().equals(peer.getEndpointId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object other) {
        if (this.getTypeOfGame() == ((GameStateContainer) other).getTypeOfGame()) {
            for (PeerState peer: this.peersLevel) {
                if (! ((GameStateContainer) other).contains(peer)) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }


}