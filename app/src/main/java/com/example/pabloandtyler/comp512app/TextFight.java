package com.example.pabloandtyler.comp512app;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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
import com.google.gson.Gson;

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
    private static boolean shouldBeFirstTokenHolder = false;
    private PeerListItemsFragment peerListItemsFragment = null;
    private TextMainArenaFragment textMainArenaFragment = null;
    private BonusRoundFragment bonusRoundFragment = null;
    private FragmentManager fragmentManager = null;

    private HashMap<String, String> peersMap = null; //maps endpointId to friendly names
    private HashMap<String, String> peersColorMap = null; //maps endpointId to an assigned color
    private String[] colors = null;
    private String myFriendlyName = null;

    public static GameStateContainer theState;
    public static PeerState myState;

    public static synchronized boolean isBonusRoundTokenHolder() {
        return bonusRoundTokenHolder;
    }

    public static synchronized void setBonusRoundTokenHolder(boolean result) {
        bonusRoundTokenHolder = result;
    }

    public static synchronized boolean isMakeNextWordBonusInitiator() {
        return makeNextWordBonusInitiator;
    }

    public static synchronized void setMakeNextWordBonusInitiator(boolean result) {
        makeNextWordBonusInitiator = result;
    }

    private static boolean bonusRoundTokenHolder = false;
    private static boolean makeNextWordBonusInitiator = false;

    // Our handle to Nearby Connections
    private ConnectionsClient connectionsClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_fight);

        myFriendlyName = CodenameGenerator.generate();
        initializeState(); //TODO: save state and maybe put this somewhere else

        connectionsClient = Nearby.getConnectionsClient(this);

        Log.i(TAG, "onCreate: myFriendlyName = " + myFriendlyName);


        //Determine which fragment to insert first
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // From MainActivity, gather if we are a host or a peer
        Intent callerIntent = getIntent();
        if(callerIntent != null){

            Bundle info = callerIntent.getExtras();
            if(info != null){
                mode = info.getString(MainActivity.MODE);
                shouldBeFirstTokenHolder = info.getBoolean("FIRST_NODE");
                TextFight.setBonusRoundTokenHolder(shouldBeFirstTokenHolder);
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
                            String messageString = (new String(message, UTF_8));
                            Log.i(TAG, "onPayloadReceived: messageString is: " + messageString);

                            Gson gson = new Gson();
                            GameStateContainer incomingGameContainer =
                                    gson.fromJson(messageString, GameStateContainer.class
                                    );

                            /*for(PeerState el: incomingGameContainer.getPeersLevel()){
                                Log.i(TAG, "onPayloadReceived: " + el.toString());
                            }*/

                            //check equality of the incoming game container with the
                            // local copy we have


                            // if game state is normal, do the following:
                            // Connection Tasks
                            // 1. iterating over the PeerState elements
                            //  if we don't have them in our map
                            //   if the peer's friendly name does not match our's
                            //    attempt to connect
                            //   if it does match our's, simply update our local copy of PeerState with the endpointId


                            //Game Logic Tasks
                            //1. iterate through all peers
                            //  check if the entry exists in our local copy
                            //  if not in our copy: add
                            //  else: check if their new incoming level is higher than our copy
                            //    if it is, update that value

                            //  update GUI elements as necessary (i.e. progress)


                            //if not equal: else just skip



                            Log.i(TAG, "onPayloadReceived: comparing incomingGameContainer to our state");
                            if (! incomingGameContainer.equals(theState)) {
                                Log.i(TAG, "onPayloadReceived: game containers they were not equal");

                                //determine the game state, i.e. normal, bonus, win, etc.
                                if (incomingGameContainer.getTypeOfGame().equals("N") ||
                                        incomingGameContainer.getTypeOfGame().equals("T")) {
                                    Log.i(TAG, "onPayloadReceived: the type of incoming game is " + incomingGameContainer.getTypeOfGame());


                                    // T is for token holder update
                                    // if we aren't the token holder and it's our turn, set our internal flag to represent that
                                    if(!TextFight.isBonusRoundTokenHolder() && incomingGameContainer.getTypeOfGame().equals("T")){
                                        TextFight.setBonusRoundTokenHolder(true);
                                        new BonusRoundAsyncTask().execute();
                                    }

                                    //CONNECTION TASKS
                                    List<PeerState> incomingPeers = incomingGameContainer.getPeersLevel();
                                    Log.i(TAG, "onPayloadReceived: starting for-each incoming peers");
                                    for (PeerState peer: incomingPeers) {
                                        Log.i(TAG, "onPayloadReceived: in iteration of peers");
                                        Log.i(TAG, "onPayloadReceived: peer.getFriendlyName() =  " + peer.getFriendlyName());
                                        if(! myState.getFriendlyName().equals(peer.getFriendlyName()) ){
                                            Log.i(TAG, "onPayloadReceived: the friendlyNames were not equal");

                                            //if XD was found, don't dare connect to it lol
                                            if(peer.getEndpointId().equals("XD")){

                                                peer.setEndpointId(endpointId);
                                            }


                                            if(! peersMap.containsKey(peer.getEndpointId()) )  {

                                                Log.i(TAG, "onPayloadReceived: we did not find the peer in our map, should try to connect and then add");
                                                //the peer isn't in my list, attempt to connect and add to list
                                                connectionsClient.requestConnection(myFriendlyName,
                                                        peer.getEndpointId(),
                                                        connectionLifecycleCallback);

                                            }
                                            if ( (! theState.contains(peer))) {
                                                Log.i(TAG, "Adding new peer: "+peer.getEndpointId());

                                                theState.getPeersLevel().add(peer);
                                            }
                                            else { //peer is already in my list, check if there is truly an update
                                                //GAME LOGIC TASKS
                                                Log.i(TAG, "onPayloadReceived: peer is already in my list, check if there is truly an update");
                                                PeerState myLocalPeer = null;

                                                for (PeerState localPeer: theState.getPeersLevel()) {
                                                    if (localPeer.getEndpointId().equals(peer.getEndpointId())) {
                                                        myLocalPeer = localPeer;

                                                        break;
                                                    }
                                                }

                                                if (peer.getLevelOfPeer() > myLocalPeer.getLevelOfPeer()) {
                                                    myLocalPeer.setLevelOfPeer(peer.getLevelOfPeer());
                                                    Log.e(TAG, "setting local peer to network level " + String.valueOf(peer.getLevelOfPeer()));
                                                }

                                            }

                                        }
                                        else{
                                            if ( (! theState.getPeersLevel().contains(peer))) {

                                                Log.i(TAG, "Adding myself to my peer state from received message: "+peer.getEndpointId());
                                                myState.setEndpointId(peer.getEndpointId());
                                            }
                                        }
                                    }

                                    //UPDATE THE GUI
                                    textMainArenaFragment.updateProgressBars();

                                }




                                //If game state is a 'W', the player is declaring victory.

                                if (incomingGameContainer.getTypeOfGame().equals("W")) {
                                    //TODO: UPDATE THE GUI


                                    String winnerName = incomingGameContainer.getPeerWithEndpointId(endpointId).getFriendlyName();

                                    Toast.makeText(TextFight.this, winnerName + " Has Won.", Toast.LENGTH_LONG).show();

                                    //TODO: whatever happens after you lose
                                    finish();


                                }

                                Log.i(TAG, "onPayloadReceived: setting inject to values");
                                // go through peers, match them to the correct GUI element, update the progress bar with some logic
                                ((TextView) findViewById(R.id.opponent1TextView2)).setText(gson.toJson(theState));




                                //at the end, sendBroadcast
                                Log.i(TAG, "onPayloadReceived: sending broadcast after having our local game container updated");
                                onBroadcastState();

                            }
                            else{
                                Log.i(TAG, "onPayloadReceived: CONTAINERS ARE EQUAL");
                                ((TextView) findViewById(R.id.opponent1TextView2)).setText(gson.toJson(theState));
                            }
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

    private void initializeState() {
        myState = new PeerState();
        myState.setEndpointId("XD");
        myState.setLevelOfPeer(4);
        myState.setFriendlyName(myFriendlyName);

        theState = new GameStateContainer();

        theState.getPeersLevel().add(myState);
    }

    // Broadcasts our presence using Nearby Connections so other players can find us.
    private void startAdvertising() { // let someone else connect to me
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
    private void startDiscovery() { //connect to someone else

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

    public void onBroadcastState() {
        String send = (new Gson()).toJson(theState);
        //List<String> list = new ArrayList<String>(peersMap.keySet());


        for(String endpointId: peersMap.keySet()){
            Log.i(TAG, "sending " + send + " to " + peersMap.get(endpointId));
            sendPayload(endpointId, send);
        }
        Gson gson = new Gson();

        ((TextView) findViewById(R.id.opponent1TextView2)).setText(gson.toJson(theState));

        Log.i(TAG, "onBroadcastState: finished sending to all peers");

    }

    public static class BonusRoundAsyncTask extends AsyncTask<String,Void,Boolean>{

        @Override
        protected Boolean doInBackground(String... strings) {

            try{
                //sleep for a 45secs to minute before allowing a bonus word
                Thread.sleep(120000);
                Log.i(TAG, "doInBackground: done sleeping, should return true");

            } catch(InterruptedException e){
                Log.e(TAG, "doInBackground: " + e.getMessage() );
                return false;

            } catch(Exception e){
                Log.e(TAG, "doInBackground: GENERAL EXCEPTION:  " + e.getMessage() );
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result){
            if (result){
                Log.i(TAG, "onPostExecute: setting the next word as the bonus round creator");
                setMakeNextWordBonusInitiator(result);
            }

        }
    }
}
