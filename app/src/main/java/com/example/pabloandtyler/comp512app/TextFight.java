package com.example.pabloandtyler.comp512app;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.pabloandtyler.comp512app.dummy.PeerDataItem;
import com.example.pabloandtyler.comp512app.dummy.TextMainArenaFragment;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TextFight extends AppCompatActivity
        implements
        PeerListItemsFragment.OnPeerClickedListener,
        JoinPeerAlert.JoinPeerAlertListener,
        TextMainArenaFragment.OnTextMainFragmentInteractionListener,
        BonusRoundFragment.BonusRoundFragmentListener{

    private static final String TAG = "2FT: TextFight";

    private static String mode = null;
    private PeerListItemsFragment peerListItemsFragment = null;
    private TextMainArenaFragment textMainArenaFragment = null;
    private BonusRoundFragment bonusRoundFragment = null;
    private FragmentManager fragmentManager = null;

    private HashMap<String, String> peersMap = null; //maps endpointId to friendly names
    private HashMap<String, String> peersColorMap = null; //maps endpointId to an assigned color
    private String[] colors = null;
    private String myFriendlyName;

    // Our handle to Nearby Connections
    private ConnectionsClient connectionsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_fight);

        connectionsClient = Nearby.getConnectionsClient(this);
        myFriendlyName = CodenameGenerator.generate();


        //Determine which fragment to insert first
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // From MainActivity, gather if we are a host or a peer
        Intent callerIntent = getIntent();
        if(callerIntent != null){

            Bundle info = callerIntent.getExtras();
            if(info != null){
                mode = info.getString(MainActivity.MODE);
            }
            else{
                mode = "";
            }

            if(mode.equals(MainActivity.MODE_PEER)){
                peerListItemsFragment = PeerListItemsFragment.newInstance(1);
                fragmentTransaction.add(R.id.multi_fragments, peerListItemsFragment);
                fragmentTransaction.commit();
            }
            if(mode.equals(MainActivity.MODE_HOST)){
                textMainArenaFragment = TextMainArenaFragment.newInstance();
                fragmentTransaction.add(R.id.multi_fragments, textMainArenaFragment);
                fragmentTransaction.commit();
            }
        }

        //Instantiate the color and peers map
        peersMap = new HashMap<>();
        peersColorMap = new HashMap<>();
        colors = getResources().getStringArray(R.array.opponentColors);

        //Store a reference to our third fragment, the bonus round
        bonusRoundFragment = BonusRoundFragment.newInstance();
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(mode.equals(MainActivity.MODE_HOST)){
            startAdvertising();
        }
        if(mode.equals(MainActivity.MODE_PEER)){
            startDiscovery();
        }

    }

    @Override
    protected void onStop(){
        super.onStop();

        connectionsClient.stopAdvertising();
        connectionsClient.stopDiscovery();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        connectionsClient.stopDiscovery();
        connectionsClient.stopAdvertising();
        connectionsClient = null;
    }

    //CALLBACKS FOR THE NEARBY CONNECTIONS API-----------------------------------------------------
    // Callbacks for receiving payloads
    //TODO: Incoporate architecture for different type of messages
    private final PayloadCallback payloadCallback =
        new PayloadCallback() {
            @Override
            public void onPayloadReceived(String endpointId, Payload payload) {
                Log.i(TAG, "onPayloadReceived called");
                if(payload == null){
                    Log.e(TAG, "payload is null");
                }
                else{
                    try{
                        byte[] message = payload.asBytes();
                        if(message != null){

                            String[] messageParts = (new String(message, UTF_8)).split(":");

                            //Message self-identification check:
                            switch(messageParts[0]){
                                case "L": //upon receiving a new map, see if our map matches the incoming map
                                    break;
                                case "B": //bonus round has started, use the index in the message for the current sentence
                                    break;
                                case "A"://
                                    break;
                                case "P":
                                    break;
                                case "D":
                                    break;
                                case "E":
                                    break;
                                case "F":
                                    break;
                                case "R":
                                    break;
                                case "U":
                                    break;
                                default:
                                    Log.e(TAG, "no suitable deconstruction found");
                                    break;

                            }

                            //textMainArenaFragment.updateDebugMessage(text);
                        }

                    } catch(NullPointerException e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(TextFight.this, "Null PTE", Toast.LENGTH_SHORT).show();

                    }catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(TextFight.this, "We died?", Toast.LENGTH_SHORT).show();
                    }

                }

            }

            @Override
            public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {

            }
        };

    // Broadcasts our presence using Nearby Connections so other players can find us.
    private void startAdvertising() {
        Log.i(TAG, "advertising started");

        // Note: Advertising may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startAdvertising(
                myFriendlyName, getPackageName(), connectionLifecycleCallback, new AdvertisingOptions(MainActivity.STRATEGY))
        .addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (mode.equals(MainActivity.MODE_HOST)){
                            textMainArenaFragment.updateFriendlyName(
                                    myFriendlyName);

                        }
                        Log.i(TAG, "we are successfully advertising");
                    }
                }
        )
        .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //TODO: add logic depending on the scenario
                        Log.e(TAG, "onFailure: " + e.getClass() + " " + e.getMessage());
                        Log.e(TAG, "We were unable to start advertising");
                    }
                }
        );
    }

    /** Starts looking for other players using Nearby Connections. */
    private void startDiscovery() {

        Log.i(TAG, "startDiscovery: finding peers");
        // Note: Discovery may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startDiscovery(
                getPackageName(), endpointDiscoveryCallback, new DiscoveryOptions(MainActivity.STRATEGY))
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG, "onSuccess: " + getPackageName());
                                Log.i(TAG, "We're looking for someone to play against");
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //TODO: potential logic for failure, restart, potentially?
                                Log.e(TAG, "onFailure: " + e.getClass() + " " + e.getMessage());
                                Log.e(TAG, "Could not discover");

                            }
                        }
                );
        Log.i(TAG, "end separate called to startDiscovery");
    }

    // Callbacks for finding other devices
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {

                //find peers and update GUI here
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {

                    Nearby.getConnectionsClient(getApplicationContext()).requestConnection(
                            myFriendlyName,
                            endpointId,
                            connectionLifecycleCallback)
                            .addOnSuccessListener(
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //We successfully requested a connection. Now both
                                            // sides must accept before the connection is established
                                            Toast.makeText(TextFight.this,
                                                    "requested connection",
                                                    Toast.LENGTH_SHORT).show();
                                            Log.i(TAG, "requested connection: both must accept");
                                        }
                                    }
                            )
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //Nearby Connections failed to request the connection
                                            //TODO: potential logic for failure, restart, potentially?
                                            Log.e(TAG, "onFailure: " + e.getClass() + " " + e.getMessage());
                                            Log.e(TAG, "failed to request connection");

                                        }
                                    }
                            );

                    Log.i(TAG, "peer found");
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    Log.i(TAG, "endpoint lost");
                }
            };

    // Callbacks for connections to other devices
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
        new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                Log.i(TAG, "onConnectionInitiated: accepting connection");
                Toast.makeText(TextFight.this, "accepting peer?", Toast.LENGTH_SHORT).show();

                if(mode.equals(MainActivity.MODE_HOST)){
                    // In host mode, auto accept an incoming connection

                    Nearby.getConnectionsClient(TextFight.this).
                            acceptConnection(endpointId, payloadCallback);

                    peersMap.put(endpointId, connectionInfo.getEndpointName());

                    insertColorForPeer(endpointId);
                    Log.d(TAG, "onConnectedInitiated, MODE = HOST");
                }

                else if(mode.equals(MainActivity.MODE_PEER)){

                    // add new peers to list of found peers, but do not auto-accept
                    peerListItemsFragment.insertPeer(
                            connectionInfo.getAuthenticationToken(),
                            endpointId,
                            connectionInfo.getEndpointName());

                    Log.i(TAG, "onConnectionInitiated, MODE = PEER");

                }
            }

            @Override
            public void onConnectionResult(String endpointId, ConnectionResolution result) {
                switch (result.getStatus().getStatusCode()){
                    case ConnectionsStatusCodes.STATUS_OK:
                        //we're connected! can now start sending and receiving data
                        Log.i(TAG, "onConnectionResult: connection successful");
                        insertColorForPeer(endpointId);

                        Toast.makeText(TextFight.this, "accepted peer!", Toast.LENGTH_SHORT).show();
                        break;

                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        // The connection was rejected by one or both sides.
                        Toast.makeText(TextFight.this, "peer rejected", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onConnectionResult: connection failed");
                        break;

                    case ConnectionsStatusCodes.STATUS_ERROR:
                        //the connection broke before it was able to be accepted
                        Log.e(TAG, "status error for onConnectionResult");
                        Toast.makeText(getApplicationContext(), "Error happened", Toast.LENGTH_SHORT).show();
                        break;
                }

            }

            @Override
            public void onDisconnected(String endpointId) {

                Log.i(TAG, "onDisconnected: disconnected from the opponent");
                Toast.makeText(TextFight.this, "disconnected", Toast.LENGTH_SHORT).show();

                attemptReconnection(endpointId);
            }
        };


    private void sendPayload(String endpointId, String message){
        Log.i(TAG, "sendPayload called");
        connectionsClient.sendPayload(
                endpointId,
                Payload.fromBytes(message.getBytes())
        );
    }

    // CALLBACKS FROM BonusRoundFragment.java-------------------------------------------------------
    public void onBonusRoundProgressUpdate(int progress){
        Log.i(TAG, "sending message to all peers");
        Toast.makeText(this, "to implement, yet!", Toast.LENGTH_SHORT).show();
    }

    public String getPeerColor(String endpointId){
        String tmp = peersColorMap.get(endpointId);
        if(tmp == null)
            return "#000000";
        return tmp;
    }

    public List<String> getPeerEndpointIds(){
        return new ArrayList<>(peersMap.keySet());
    }


    //CALLBACKS FROM PeerListItemsFragment.java
    // A callback from the fragment that a the user wants to potentially join a peer!
    @Override
    public void onPeerClicked(PeerDataItem item) {
        //TODO: initialize connection here with new peer, and display alert dialog.


        JoinPeerAlert alert = JoinPeerAlert.newInstance(item);
        alert.setmListener(this);
        alert.show(getSupportFragmentManager(), "uniqueStringTaglel");

    }

    //CALLBACKS FOR JoinPeerAlert.java

    @Override
    public void onAlertPositiveClick(PeerDataItem item) {

        Nearby.getConnectionsClient(this)
                .acceptConnection(item.getEndpointId(),payloadCallback);

        // allow others to connect to us automatically now that we're in the network
        mode = MainActivity.MODE_HOST;

        // stop looking for someone to join, let others join us, now.
        //connectionsClient.stopDiscovery();
        startAdvertising();

        //update a new peer to our peers map
        peersMap.put(item.getEndpointId(), item.getFriendlyName());

        textMainArenaFragment = TextMainArenaFragment.newInstance(
                item.getFriendlyName()
        );

        fragmentManager.beginTransaction()
                .replace(R.id.multi_fragments,
                        textMainArenaFragment)

                .commit();

        //.addToBackStack("PeersListFragment")

    }

    @Override
    public void onAlertNegativeClick() {
        Toast.makeText(this, "stay in fragment!", Toast.LENGTH_SHORT).show();
    }

    //CALLBACKS FROM TextMainArenaFragment.java
    @Override
    public void onTextMainFragmentInteraction(String message) {
        Log.i(TAG, "received callback: message: " + message);

        for(String endpointId: peersMap.keySet()){
            Log.i(TAG, "sending " + message + " to " + peersMap.get(endpointId));
            sendPayload(endpointId, message);
        }

        Log.i(TAG, "onTextMainFragmentInteraction completed");
    }

    //Local methods
    public void insertColorForPeer(String endpointId){

        boolean completedRandomColorAssignment = false;
        String assignedColor = "";

        while(!completedRandomColorAssignment){

            int randomIndex = new Random().nextInt(colors.length);

            assignedColor = colors[randomIndex];
            if(assignedColor == null){
                Log.e(TAG, "assignedColor was null");
            }
            else{
                Log.i(TAG, "insertColorForPeer: "+ assignedColor);
            }
            completedRandomColorAssignment = true;

            for(String colorAssigned: peersColorMap.keySet()){
                if(assignedColor.equals(colorAssigned)){
                    completedRandomColorAssignment = false;
                }
            }
        }

        peersColorMap.put(endpointId, assignedColor);
    }

    public void onBonus(View view) {
        fragmentManager.beginTransaction()
                .replace(R.id.multi_fragments,
                        bonusRoundFragment)
                .addToBackStack("TextFightArenaFragment")
                .commit();
    }
/*
    private class PeerBattleInfo{
        private String peerColor;
        private String peerFriendlyName;
        private String peerLevel;
        private String endpointId;

        public String getPeerColor() {
            return peerColor;
        }

        public void setPeerColor(String peerColor) {
            this.peerColor = peerColor;
        }

        public String getPeerFriendlyName() {
            return peerFriendlyName;
        }

        public void setPeerFriendlyName(String peerFriendlyName) {
            this.peerFriendlyName = peerFriendlyName;
        }

        public String getPeerLevel() {
            return peerLevel;
        }

        public void setPeerLevel(String peerLevel) {
            this.peerLevel = peerLevel;
        }

        public String getEndpointId() {
            return endpointId;
        }

        public void setEndpointId(String endpointId) {
            this.endpointId = endpointId;
        }

    }*/

    private void attemptReconnection(String endpointId){


        //reattempt connection only once
        Nearby.getConnectionsClient(getApplicationContext()).requestConnection(
                myFriendlyName,
                endpointId,
                connectionLifecycleCallback)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //We successfully requested a connection. Now both
                                // sides must accept before the connection is established
                                Toast.makeText(TextFight.this,
                                        "attemptReconnection: requested connection",
                                        Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "attemptReconnection: both must accept");
                                Toast.makeText(getApplicationContext(), "reconnect!", Toast.LENGTH_SHORT).show();
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "stop reconnect!", Toast.LENGTH_SHORT).show();

                                Log.e(TAG, "attemptReconnection: " + e.getClass() + " " + e.getMessage());
                                Log.e(TAG, "attemptReconnection: will stop trying action of reattempts");

                            }
                        }
                );

    }

    public void onDisconnectTest(View view) {
        for(String el: peersMap.keySet()){
            Log.i(TAG, "onDisconnectTest: " + el);
            connectionsClient.disconnectFromEndpoint(el);
        }
    }
}
