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

    public boolean contains(PeerState peer) {
        for (PeerState element: peersLevel) {
            if (element.getEndpointId().equals(peer.getEndpointId())) {
                return true;
            }
        }
        return false;
    }


}