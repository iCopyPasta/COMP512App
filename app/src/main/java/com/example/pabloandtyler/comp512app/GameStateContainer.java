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

    @SerializedName("bonusRoundArrayIndex")
    @Expose
    private int bonusRoundArrayIndex;

    public GameStateContainer(){
        this.typeOfGame = "N";
        this.peersLevel = new ArrayList<>();
        bonusRoundArrayIndex = 0;
    }

    public int getBonusRoundArrayIndex() {
        return bonusRoundArrayIndex;
    }

    public void setBonusRoundArrayIndex(int bonusRoundArrayIndex) {
        this.bonusRoundArrayIndex = bonusRoundArrayIndex;
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

    public PeerState getPeerWithEndpointId(String endpointId) {
        for (PeerState element: peersLevel) {
            if (element.getEndpointId().equals(endpointId)) {
                return element;
            }
        }
        return null;
    }

    public boolean trueCompare(PeerState peer) {
        for (PeerState element: peersLevel) {
            if (element.equals(peer)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(PeerState peer) {
        for (PeerState element: peersLevel) {
            if (element.getFriendlyName().equals(peer.getFriendlyName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object other) {
        if( other instanceof GameStateContainer ){

            if (this.getTypeOfGame().equals( ((GameStateContainer) other).getTypeOfGame()) && this.getBonusRoundArrayIndex() == ((GameStateContainer) other).getBonusRoundArrayIndex() ) {

                for (PeerState peer: this.peersLevel) {
                    if(! ((GameStateContainer) other).trueCompare(peer)){
                        return false;
                    }
                }

                for (PeerState peer: ((GameStateContainer) other).getPeersLevel()) {
                    if(! this.trueCompare(peer)){
                        return false;
                    }
                }
                return true;
            }
        }

        return false;
    }


}