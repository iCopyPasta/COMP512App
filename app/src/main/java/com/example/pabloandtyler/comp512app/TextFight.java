package com.example.pabloandtyler.comp512app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TextFight extends AppCompatActivity
        implements
        PeerListItemsFragment.OnPeerClickedListener,
        JoinPeerAlert.JoinPeerAlertListener,
        TextMainArenaFragment.OnTextMainFragmentInteractionListener,
        BonusRoundFragment.BonusRoundFragmentListener{

    // constants for our application
    private static final String TAG = "2FT: TextFight";
    private static String mode = null;

    // local references to child fragments
    private PeerListItemsFragment peerListItemsFragment = null;
    private TextMainArenaFragment textMainArenaFragment = null;
    private BonusRoundFragment bonusRoundFragment = null;
    private FragmentManager fragmentManager = null;

    // local representation of who we know exists
    private HashMap<String, String> peersMap = null; //maps endpointId to friendly names

    // local reference to object representing the global game state
    public static GameStateContainer theState;
    public static PeerState myState;
    private String myFriendlyName = null;

    // BONUS ROUND VARIABLES
    public static boolean inBonus = false;
    private static boolean bonusRoundTokenHolder = false;
    private static boolean makeNextWordBonusInitiator = false;

    // NORMAL-ROUND VOTING VARIABLES
    private static boolean alreadyLost = false;
    public static boolean claimWinner = false;
    public static int votesSnapshot = 0;
    public static int runningVotes = 0;

    // Our handle to Nearby Connections
    private ConnectionsClient connectionsClient;

    // array for ordering 'history' of incomers for GUI
    public static ArrayList<String> peerHistory = null;

    // our handler to our asyncTask
    private BonusRoundAsyncTask task;

    // synchronized getter and setter methods
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


    /**
     * onCreate is called by Android when creating this activity for the first time.
     * We set references to variables where appropriate, here.
     * @param savedInstanceState Bundle object containing information about a state to potentially revert
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_fight);

        myFriendlyName = CodenameGenerator.generate();
        initializeState();
        connectionsClient = Nearby.getConnectionsClient(this);

        //Log.i(TAG, "onCreate: myFriendlyName = " + myFriendlyName);

        // determine which fragment to insert first
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // from MainActivity, gather if we are a host or a peer and perform logic
        Intent callerIntent = getIntent();
        if(callerIntent != null){

            Bundle info = callerIntent.getExtras();
            if(info != null){
                mode = info.getString(MainActivity.MODE);
                boolean shouldBeFirstTokenHolder = info.getBoolean("FIRST_NODE");
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

        // instantiate the peers map - used for local knowledge of which peers we can actually speak to
        peersMap = new HashMap<>();

        // store a reference to our third fragment, the bonus round
        bonusRoundFragment = BonusRoundFragment.newInstance();
    }

    /**
     * onStart is called by Android when the activity will be visible to the user.
     * We check if we have permissions, and if not, request them
     */
    @Override
    protected void onStart(){
        super.onStart();
    }

    /**
     * onResume is called by Android to graphically display a fragment in the fragment lifecycle.
     * We discover and/or advertise where necessary based on a role
     */
    @Override
    protected void onResume(){
        super.onResume();
        peerHistory = new ArrayList<>();


        if(mode.equals(MainActivity.MODE_HOST)){
            startAdvertising();
        }
        if(mode.equals(MainActivity.MODE_PEER)){
            startDiscovery();
        }

    }

    /**
     * onStop is called by Android when the activity will no longer be visible to the user.
     * We stop discovering and advertising.
     */
    @Override
    protected void onStop(){
        super.onStop();
        connectionsClient.stopAdvertising();
        connectionsClient.stopDiscovery();
    }

    /**
     * onDestroy is called by Android when the activity is to be marked for garbage collection.
     * We deference variables and force garbage collection
     */
    @Override
    protected void onDestroy(){
        super.onDestroy();
        setBonusRoundTokenHolder(false);
        setMakeNextWordBonusInitiator(false);
        theState = null;
        myState = null;
        textMainArenaFragment = null;
        bonusRoundFragment = null;
        peerListItemsFragment = null;
        peersMap = null;
        if(task != null)
            task.cancel(true);
        connectionsClient.stopAllEndpoints();
        connectionsClient.stopDiscovery();
        connectionsClient.stopAdvertising();
        connectionsClient = null;
        System.gc();
        //Log.i(TAG, "ON DESTROY CALLED");
    }

    //CALLBACKS FOR THE NEARBY CONNECTIONS API-----------------------------------------------------

    // Callbacks for receiving payloads
    /**
     * payloadCallback is a variable to save within the Connections API.
     * We handle a new message here, with appropriate logic inside.
     */
    private final PayloadCallback payloadCallback =
        new PayloadCallback() {
            @Override
            public void onPayloadReceived(String endpointId, Payload payload) {

                if(payload == null){
                    //Log.e(TAG, "payload is null");
                }
                else{
                    try{
                        byte[] message = payload.asBytes();
                        if(message != null){
                            String messageString = (new String(message, UTF_8));
                            //Log.i(TAG, "onPayloadReceived: messageString is: " + messageString + " from " + peersMap.get(endpointId));

                            Gson gson = new Gson();
                            GameStateContainer incomingGameContainer =
                                    gson.fromJson(messageString, GameStateContainer.class
                                    );


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


                            if (! incomingGameContainer.equals(theState)) {

                                //determine the game state, i.e. normal, bonus, win, etc.
                                if (incomingGameContainer.getTypeOfGame().equals("N") ||
                                        incomingGameContainer.getTypeOfGame().equals("T") ||incomingGameContainer.getTypeOfGame().equals("BD")) {
                                    //Log.i(TAG, "onPayloadReceived: the type of incoming game is " + incomingGameContainer.getTypeOfGame());
                                    //Log.i(TAG, "onPayloadReceived: the type of our current game is " + theState.getTypeOfGame());

                                    //logic for BD, bonus round end
                                    if (theState.getTypeOfGame().equals("B") && incomingGameContainer.getTypeOfGame().equals("BD")) { //we are in bonus round, end the bonus round
                                        //Log.i(TAG, "onPayloadReceived: IS B and GOT BD");
                                        theState.setTypeOfGame("BD");
                                        if(inBonus)
                                            bonusRoundEnd();


                                        return;
                                    }
                                    else if (theState.getTypeOfGame().equals("BD") && incomingGameContainer.getTypeOfGame().equals("BD")) { //bonus round end buffer switching game state back to normal
                                        //Log.i(TAG, "onPayloadReceived: IS BD and GOT BD");
                                        theState.setTypeOfGame("N");
                                        if(inBonus)
                                            bonusRoundEnd();


                                        return;
                                    } else if(theState.getTypeOfGame().equals("BD") && incomingGameContainer.getTypeOfGame().equals("N")){
                                        //Log.i(TAG, "onPayloadReceived: IS BD and GOT N");
                                        theState.setTypeOfGame("N");
                                        if(inBonus)
                                            bonusRoundEnd();


                                        return;
                                    }

                                    // T is for token holder update
                                    // if we aren't the token holder and it's our turn, set our internal flag to represent that
                                    if(!TextFight.isBonusRoundTokenHolder() && incomingGameContainer.getTypeOfGame().equals("T")){
                                        incomingGameContainer.setTypeOfGame("N");
                                        TextFight.setBonusRoundTokenHolder(true);
                                        startBToken();
                                    }

                                    // CONNECTION TASKS
                                    List<PeerState> incomingPeers = incomingGameContainer.getPeersLevel();

                                    for (PeerState peer: incomingPeers) {

                                        if(! myState.getFriendlyName().equals(peer.getFriendlyName()) ){


                                            if(!peerHistory.contains(peer.getFriendlyName())){
                                                peerHistory.add(peer.getFriendlyName());
                                            }



                                            //if XD was found, don't dare connect to it lol
                                            if(peer.getEndpointId().equals("XD")){

                                                peer.setEndpointId(endpointId);
                                            }


                                            if(! peersMap.containsKey(peer.getEndpointId()) )  {

                                                //the peer isn't in my list, attempt to connect and add to list
                                                connectionsClient.requestConnection(myFriendlyName,
                                                        peer.getEndpointId(),
                                                        connectionLifecycleCallback);

                                            }
                                            if ( (! theState.contains(peer))) {

                                                theState.getPeersLevel().add(peer);
                                            }
                                            else { // peer is already in my list, check if there is truly an update
                                                // GAME LOGIC TASKS
                                                PeerState myLocalPeer = null;

                                                for (PeerState localPeer: theState.getPeersLevel()) {
                                                    if (localPeer.getEndpointId().equals(peer.getEndpointId())) {
                                                        myLocalPeer = localPeer;

                                                        break;
                                                    }
                                                }

                                                if (peer.getLevelOfPeer() >= myLocalPeer.getLevelOfPeer()) {
                                                    myLocalPeer.setLevelOfPeer(peer.getLevelOfPeer());
                                                    }

                                            }

                                        }
                                        else{
                                            if ( (! theState.getPeersLevel().contains(peer))) {

                                                myState.setEndpointId(peer.getEndpointId());
                                            }
                                        }
                                    }

                                    // UPDATE THE GUI
                                    textMainArenaFragment.updateProgressBars();

                                    onBroadcastState();


                                }

                                if (incomingGameContainer.getTypeOfGame().equals("B") && !theState.getTypeOfGame().equals("BD")) {
                                    //Log.i(TAG, "onPayloadReceived: INCOMING IS B AND WE DON'T HAVE BD");
                                    if (!inBonus){

                                        if (! isBonusRoundTokenHolder()){
                                            //Log.i(TAG, "onPayloadReceived: SETTING INCOMING ARRAY INDEX");
                                            theState.setBonusRoundArrayIndex(incomingGameContainer.getBonusRoundArrayIndex());
                                        }

                                        onBonusRoundTransition();
                                    }

                                    if (claimWinner) {
                                        theState.setTypeOfGame("N-W-P");
                                        onBroadcastState();
                                        theState.setTypeOfGame("BD");
                                        return;
                                    }

                                    // CONNECTION TASKS
                                    List<PeerState> incomingPeers = incomingGameContainer.getPeersLevel();

                                    for (PeerState peer: incomingPeers) {

                                        if(! myState.getFriendlyName().equals(peer.getFriendlyName()) ){


                                            if(!peerHistory.contains(peer.getFriendlyName())){
                                                peerHistory.add(peer.getFriendlyName());
                                            }

                                            // if XD was found, don't dare connect to it lol
                                            if(peer.getEndpointId().equals("XD")){

                                                peer.setEndpointId(endpointId);
                                            }

                                            if(! peersMap.containsKey(peer.getEndpointId()) )  {

                                                // the peer isn't in my list, attempt to connect and add to list
                                                connectionsClient.requestConnection(myFriendlyName,
                                                        peer.getEndpointId(),
                                                        connectionLifecycleCallback);

                                            }
                                            if ( (! theState.contains(peer))) {
                                                //Log.i(TAG, "Adding new peer: "+peer.getEndpointId());
                                                theState.getPeersLevel().add(peer);
                                            }
                                            else { //peer is already in my list, check if there is truly an update
                                                // GAME LOGIC TASKS
                                                //Log.i(TAG, "onPayloadReceived: GAME LOGIC TASKS");
                                                PeerState myLocalPeer = null;

                                                for (PeerState localPeer: theState.getPeersLevel()) {
                                                    if (localPeer.getEndpointId().equals(peer.getEndpointId())) {
                                                        myLocalPeer = localPeer;
                                                        break;
                                                    }
                                                }

                                                if (peer.getLevelOfPeer() > myLocalPeer.getLevelOfPeer()) {
                                                    myLocalPeer.setLevelOfPeer(peer.getLevelOfPeer());
                                                    //Log.i(TAG, "setting local peer to level " + String.valueOf(peer.getLevelOfPeer()));
                                                }

                                                if (peer.getPositionInBonusRound() > myLocalPeer.getPositionInBonusRound()) {
                                                    myLocalPeer.setPositionInBonusRound(peer.getPositionInBonusRound());
                                                    //Log.i(TAG, "setting local peer to bonus round progress " + peer.getPositionInBonusRound());
                                                }

                                            }

                                        }
                                        else{
                                            if ( (! theState.getPeersLevel().contains(peer))) {

                                                myState.setEndpointId(peer.getEndpointId());
                                            }

                                            if(peer.getLevelOfPeer() > myState.getLevelOfPeer()){
                                                myState.setLevelOfPeer(peer.getLevelOfPeer());
                                            }
                                        }
                                    }

                                    // UPDATE THE GUI

                                    bonusRoundFragment.updateProgressBars();
                                    //onBroadcastState(); <-- headache line that causes broadcast storm


                                }

                                if(incomingGameContainer.getTypeOfGame().equals("N-W-P")){
                                    //Log.i(TAG, "onPayloadReceived: GOT N-W-P");
                                    if(alreadyLost){ // if we already lost from some prior, let the claimed winner have a vote
                                        theState.setTypeOfGame("N-W-C");
                                        //Log.i(TAG, "onPayloadReceived: alreadyLost");
                                        String send = (new Gson()).toJson(theState);
                                        sendPayload(endpointId, send);

                                        if (inBonus)
                                            theState.setTypeOfGame("BD");
                                        return;
                                    }

                                    if(claimWinner){
                                        peerSelfSort(endpointId, peersMap.get(endpointId));
                                    } else{
                                        alreadyLost = true;
                                        setBonusRoundTokenHolder(false);
                                        setMakeNextWordBonusInitiator(false);
                                        onDisableInput();
                                        theState.setTypeOfGame("N-W-C");
                                        String send = (new Gson()).toJson(theState);
                                        sendPayload(endpointId, send);

                                        if (inBonus)
                                            theState.setTypeOfGame("BD");

                                    }

                                }

                                if(incomingGameContainer.getTypeOfGame().equals("N-W-L")){
                                    //Log.i(TAG, "onPayloadReceived: GOT N-W-L");
                                    alreadyLost = true;
                                    setBonusRoundTokenHolder(false);
                                    setMakeNextWordBonusInitiator(false);
                                    onDisableInput();
                                    theState.setTypeOfGame("N-W-C");
                                    String send = (new Gson()).toJson(theState);
                                    sendPayload(endpointId, send);

                                    if (inBonus)
                                        theState.setTypeOfGame("BD");

                                }

                                if(incomingGameContainer.getTypeOfGame().equals("N-W-C")){
                                    //Log.i(TAG, "onPayloadReceived: GOT N-W-C");
                                    if (claimWinner)
                                        gatherVotes();
                                }


                                //If game state is a 'W', the player is declaring victory.
                                if (incomingGameContainer.getTypeOfGame().equals("W")) {

                                    String winnerName = incomingGameContainer.getPeerWithEndpointId(endpointId).getFriendlyName();

                                    Toast.makeText(TextFight.this, winnerName + " Has Won.", Toast.LENGTH_LONG).show();

                                    finish();
                                }

                            }
                            else{
                                //Log.i(TAG, "onPayloadReceived: CONTAINERS ARE EQUAL");
                            }
                        }

                    } catch(NullPointerException e){
                        //Log.e(TAG, "NULL POINTER EXCEPTION" + e.getMessage());

                    }catch (Exception e){
                        //Log.e(TAG, "GENERAL EXCEPTION " + e.getMessage());

                    }

                }

            }

            @Override
            public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {

            }
        };

    /**
     * instantiate our local information before a game starts
     */
    private void initializeState() {
        myState = new PeerState();
        myState.setEndpointId("XD");
        myState.setLevelOfPeer(4);
        myState.setFriendlyName(myFriendlyName);

        theState = new GameStateContainer();

        theState.getPeersLevel().add(myState);
    }

    /**
     * disables user input regardless of which part of the game a player is in
     */
    public void onDisableInput() {
        if (inBonus) {
            ((EditText) findViewById(R.id.bonusRoundTypeSpace)).setEnabled(false);
        }
        else {
            ((EditText) findViewById(R.id.type_word)).setEnabled(false);
        }
    }

    /**
     * replaces the fragment back to the regular game and calls methods to clear variables
     */
    public void bonusRoundEnd() {
        //Log.i(TAG, "Bonus round terminated");
        //Log.i(TAG, "Current level:" + myState.getLevelOfPeer());
        textMainArenaFragment.level = myState.getLevelOfPeer();
        onClear();
        bonusRoundFragment.type_word.setText("");
        onBroadcastState();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.multi_fragments, textMainArenaFragment)
                .commit();
        Toast.makeText(TextFight.this,"Bonus round over!",Toast.LENGTH_LONG).show();
    }


    /**
     * accepts votes from other peers
     */
    public void gatherVotes(){
        //Log.i(TAG, "GATHERING VOTES");
        runningVotes++;

        if(runningVotes >= votesSnapshot){
            //Log.i(TAG, "gatherVotes: WE WON");
            if (theState.getTypeOfGame().equals("N") || theState.getTypeOfGame().equals("N-W-P"))
                textMainArenaFragment.victory();

            else if (theState.getTypeOfGame().equals("B") || theState.getTypeOfGame().equals("BD")) {
                bonusRoundFragment.Victory();
            }

        }

    }


    /**
     * called to arrange who should be a winner in the presence of two claims to victory
     * @param endpointId String containing the incoming opponent that claims to win
     * @param other String containing the other peer's friendly name
     */
    private void peerSelfSort(String endpointId, String other){
        int result = myFriendlyName.compareTo(other);
        //Log.i(TAG, "peerSelfSort: " + myFriendlyName + " compared to " + other + " = " + result);

        //their endpointId is higher, so they get priority for winning
        if(result < 0){
            alreadyLost = true;
            setBonusRoundTokenHolder(false);
            setMakeNextWordBonusInitiator(false);
            theState.setTypeOfGame("N-W-C");
            String send = (new Gson()).toJson(theState);
            sendPayload(endpointId, send);

            if(inBonus){
                theState.setTypeOfGame("BD");
            }
        }
        else{ //we would win!
            theState.setTypeOfGame("N-W-L");
            String send = (new Gson()).toJson(theState);
            sendPayload(endpointId, send);
            if(inBonus){
                theState.setTypeOfGame("BD");
            }
        }

    }

    /**
     * Broadcasts our presence using Nearby Connections so other players can find us.
     */
    private void startAdvertising() { // let someone else connect to me
        //Log.i(TAG, "advertising started");

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
                        //Log.i(TAG, "we are successfully advertising");
                    }
                }
        )
        .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //TODO: add logic depending on the scenario
                        //Log.e(TAG, "onFailure: " + e.getClass() + " " + e.getMessage());
                        //Log.e(TAG, "We were unable to start advertising");
                    }
                }
        );
    }

    /**
     * Starts looking for other players using Nearby Connections.
     */
    private void startDiscovery() { //connect to someone else

        //Log.i(TAG, "startDiscovery: finding peers");
        // Note: Discovery may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startDiscovery(
                getPackageName(), endpointDiscoveryCallback, new DiscoveryOptions(MainActivity.STRATEGY))
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //Log.i(TAG, "onSuccess: " + getPackageName());
                                //Log.i(TAG, "We're looking for someone to play against");
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //TODO: potential logic for failure, restart, potentially?
                                //Log.e(TAG, "onFailure: " + e.getClass() + " " + e.getMessage());
                                //Log.e(TAG, "Could not discover");

                            }
                        }
                );
        //Log.i(TAG, "end separate called to startDiscovery");
    }


    /**
     * register this callback to Connections API for later reference, called when a new node is found
     */
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {

                //find peers
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

                                            //Log.i(TAG, "requested connection: both must accept");
                                        }
                                    }
                            )
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            //TODO: potential logic for failure, restart, potentially?
                                            //Log.e(TAG, "onFailure: " + e.getClass() + " " + e.getMessage());
                                            //Log.e(TAG, "failed to request connection");

                                        }
                                    }
                            );

                    //Log.i(TAG, "peer found");
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    //Log.i(TAG, "endpoint lost");
                }
            };

    // Callbacks for connections to other devices
    /**
     * register this callback to Connections API for later reference, called when a new node is connecting
     */
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
        new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                //Log.i(TAG, "onConnectionInitiated: accepting connection");
                Toast.makeText(TextFight.this, "accepting peer?", Toast.LENGTH_SHORT).show();

                if(mode.equals(MainActivity.MODE_HOST)){
                    // in host mode, auto accept an incoming connection

                    Nearby.getConnectionsClient(TextFight.this).
                            acceptConnection(endpointId, payloadCallback);

                    peersMap.put(endpointId, connectionInfo.getEndpointName());


                    //Log.d(TAG, "onConnectedInitiated, MODE = HOST");
                }

                else if(mode.equals(MainActivity.MODE_PEER)){

                    // add new peers to list of found peers, but do not auto-accept
                    peerListItemsFragment.insertPeer(
                            connectionInfo.getAuthenticationToken(),
                            endpointId,
                            connectionInfo.getEndpointName());

                    //Log.i(TAG, "onConnectionInitiated, MODE = PEER");

                }
            }

            @Override
            public void onConnectionResult(String endpointId, ConnectionResolution result) {
                switch (result.getStatus().getStatusCode()){
                    case ConnectionsStatusCodes.STATUS_OK:
                        // we're connected! can now start sending and receiving data
                        //Log.i(TAG, "onConnectionResult: connection successful");

                        Toast.makeText(TextFight.this, "accepted peer!", Toast.LENGTH_SHORT).show();
                        if(!inBonus){
                            //Log.i(TAG, "not in bonus, update progress bars ");
                            textMainArenaFragment.updateProgressBars();

                            //TODO: put update peer here, instead? peersMap.put(endpointId, connectionInfo.getEndpointName());

                            // initial GUI update for that new peer that has successfully connected to us
                            onBroadcastState();
                        }
                        break;

                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        // the connection was rejected by one or both sides.
                        Toast.makeText(TextFight.this, "peer rejected", Toast.LENGTH_SHORT).show();
                        //Log.i(TAG, "onConnectionResult: connection failed");
                        break;

                    case ConnectionsStatusCodes.STATUS_ERROR:
                        // the connection broke before it was able to be accepted
                        //Log.e(TAG, "status error for onConnectionResult");
                        Toast.makeText(getApplicationContext(), "Connection Broke", Toast.LENGTH_SHORT).show();
                        break;
                }

            }

            @Override
            public void onDisconnected(String endpointId) {
                if(peersMap != null){
                    peersMap.remove(endpointId);
                    onSetWinnerSnapshot();

                    //Log.i(TAG, "onDisconnected: disconnected from the opponent");

                    if(votesSnapshot > 0){
                        if (claimWinner){
                            if(runningVotes >= votesSnapshot){
                                if(inBonus)
                                    bonusRoundFragment.Victory();
                                else{
                                    textMainArenaFragment.victory();
                                }
                            }
                        }
                    }
                    // attempt a reconnection in the event we lose someone who we had
                    attemptReconnection(endpointId);
                }
            }
        };


    /**
     * sends a message to a specified endpointId in the Connections API
     * @param endpointId String containing the target endpointId
     * @param message String containing the message to send
     */
    private void sendPayload(String endpointId, String message){
        connectionsClient.sendPayload(
                endpointId,
                Payload.fromBytes(message.getBytes())
        );
    }

    // CALLBACKS FROM BonusRoundFragment.java-------------------------------------------------------

    /**
     * resets values pertaining to voting, bonus round, and general cleanup
     */
    public void onClear() {
        //Log.i(TAG, "onClear: BONUS ROUND INDEX" + theState.getBonusRoundArrayIndex());
        TextFight.theState.setBonusRoundArrayIndex(0);
        TextFight.setMakeNextWordBonusInitiator(false);
        TextFight.inBonus = false;
        bonusRoundFragment.highestProgress = 0;
        bonusRoundFragment.thresholdPercentage = 0;
        alreadyLost = false;
        claimWinner = false;
        votesSnapshot = 0;
        runningVotes = 0;

        //0-out all values for all peers
        for (PeerState el: theState.getPeersLevel()){
            el.setPositionInBonusRound(0);
        }
    }

    /**
     * starts the bonus round variables and clears any potential remnants before starting
     */
    public void onBStart() {
        TextFight.theState.setBonusRoundArrayIndex(0);
        TextFight.setMakeNextWordBonusInitiator(false);
        bonusRoundFragment.highestProgress = 0;
        alreadyLost = false;
        claimWinner = false;
        votesSnapshot = 0;
        runningVotes = 0;

        //0-out all values for all peers
        for (PeerState el: theState.getPeersLevel()){
            el.setPositionInBonusRound(0);
        }
    }

    //CALLBACKS FROM PeerListItemsFragment.java-----------------------------------------------------

    /**
     * when a peer item is clicked from the user, we ask the user if they wish to fully connect
     * @param item
     */
    @Override
    public void onPeerClicked(PeerDataItem item) {

        JoinPeerAlert alert = JoinPeerAlert.newInstance(item);
        alert.setmListener(this);
        alert.show(getSupportFragmentManager(), "uniqueStringTaglel");

    }

    //CALLBACKS FOR JoinPeerAlert.java--------------------------------------------------------------

    /**
     * called when a user confirms they wish to connect to a new network node
     * @param item PeerDataItem encapsulating information about a new peer we can add
     */
    @Override
    public void onAlertPositiveClick(PeerDataItem item) {

        Nearby.getConnectionsClient(this)
                .acceptConnection(item.getEndpointId(),payloadCallback);

        // allow others to connect to us automatically now that we're in the network
        mode = MainActivity.MODE_HOST;

        // stop looking for someone to join, let others join us, now.
        //connectionsClient.stopDiscovery();
        startAdvertising();

        //TODO: delete this line and wait until we've confirmed?
        // update a new peer to our peers map
        peersMap.put(item.getEndpointId(), item.getFriendlyName());

        textMainArenaFragment = TextMainArenaFragment.newInstance(
                item.getFriendlyName()
        );

        fragmentManager.beginTransaction()
                .replace(R.id.multi_fragments,
                        textMainArenaFragment)
                .commit();

    }

    /**
     * A user opted not to connect to a new peer
     */
    @Override
    public void onAlertNegativeClick() {
        Toast.makeText(this, "stay in fragment!", Toast.LENGTH_SHORT).show();
    }

    //CALLBACKS FROM TextMainArenaFragment.java----------------------------------------------------

    /**
     * allows a peer to give up their role as a token holder and pass it along to a random new peer
     */
    @Override
    public void onSendToken() {
        //Log.i(TAG, "onSendToken, attempting to determine next");


        Iterator<Map.Entry<String, String>> iter = peersMap.entrySet().iterator();
        Map.Entry<String, String> endpointTarget = null;

        while(iter.hasNext()) {
            endpointTarget = iter.next();
            if (!endpointTarget.getValue().equals(myFriendlyName)) {
                break;
            }

        }

        if(endpointTarget != null){
            //Log.i(TAG, "onSendToken: found a different peer to send over the token");
            theState.setTypeOfGame("T");
            //Log.i(TAG, "onSendToken: broadcasting new state out");
            sendPayload(endpointTarget.getKey(), new Gson().toJson(theState));
            theState.setTypeOfGame("N");
        }

    }

    /**
     * broadcasts our information about the global state, allowing other peers to reject or accept
     * this new information
     */
    public void onBroadcastState() {
        String send = (new Gson()).toJson(theState);
        //List<String> list = new ArrayList<String>(peersMap.keySet());


        for(String endpointId: peersMap.keySet()){
            //Log.i(TAG, "sending " + send + " to " + peersMap.get(endpointId));
            sendPayload(endpointId, send);
        }

    }

    /**
     * transitions the game into the bonus round fragment, with appropriate logic and setting of
     * local variables
     */
    @Override
    public void onBonusRoundTransition() {
        //Log.i(TAG, "onPayloadReceived: MOVING OVER TO BONUS ROUND SCREEN");
        //Toast.makeText(TextFight.this, "BR-BROADCAST", Toast.LENGTH_SHORT).show();

        if(!inBonus){
            inBonus = true;

            theState.setTypeOfGame("B");

            if (isBonusRoundTokenHolder()) {

                Resources res = getResources();

                int randomIndex = new Random().nextInt(res.getStringArray(R.array.bonusDigitList).length);

                //Log.i(TAG, "We are the token holder. Setting the bonus round string index to: " +randomIndex);
                theState.setBonusRoundArrayIndex(randomIndex);
            }

            setBonusRoundTokenHolder(false);
            setMakeNextWordBonusInitiator(false);

            onBroadcastState();

            fragmentManager.beginTransaction()
                    .replace(R.id.multi_fragments,
                            bonusRoundFragment)
                    .commit();
        }

    }

    /**
     * sets the snapshot of votes a player must achieve in order to declare victory
     */
    @Override
    public void onSetWinnerSnapshot() {
        //Log.i(TAG, "onSetWinnerSnapshot: peersMap size is: " + peersMap.size());
        //Log.i(TAG, "onSetWinnerSnapshot: peers map contains: ");
        for(String el: peersMap.values()){
            //Log.i(TAG, el);
        }
        votesSnapshot = peersMap.size();

        if(votesSnapshot <= 0){ //should only occur when alone
            Toast.makeText(TextFight.this,"Lost all players!", Toast.LENGTH_SHORT).show();
        }

    }


    /**
     * attempts one reconnection in the presence of a failure
     * @param endpointId the String containing a target endpointId to reconnect
     */
    private void attemptReconnection(String endpointId){


        if(inBonus){
            bonusRoundEnd();
        }

        //reattempt connection only once
        Nearby.getConnectionsClient(getApplicationContext()).requestConnection(
                myFriendlyName,
                endpointId,
                connectionLifecycleCallback)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // We successfully requested a connection. Now both
                                // sides must accept before the connection is established

                                //Log.i(TAG, "attemptReconnection: both must accept");
                                Toast.makeText(getApplicationContext(), "reconnect!", Toast.LENGTH_SHORT).show();
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "stop reconnect!", Toast.LENGTH_SHORT).show();
                                //Log.e(TAG, "attemptReconnection: " + e.getClass() + " " + e.getMessage());
                                //Log.e(TAG, "attemptReconnection: will stop trying action of reattempts");

                            }
                        }
                );

    }

    /**
     * callback to allow a new timer for the next bonus round word initiator
     */
    public void startBToken(){
        task = new BonusRoundAsyncTask();
        task.execute();
    }


    @SuppressLint("StaticFieldLeak")
    public class BonusRoundAsyncTask extends AsyncTask<String,Void,Boolean>{


        @Override
        protected Boolean doInBackground(String... strings) {

            try{
                //sleep for a 45secs to minute before allowing a bonus word
                Thread.sleep(45_000L);
                //Log.i(TAG, "doInBackground: done sleeping, should return true");

            } catch(InterruptedException e){
                //Log.e(TAG, "doInBackground: " + e.getMessage() );
                return false;

            } catch(Exception e){
                //Log.e(TAG, "doInBackground: GENERAL EXCEPTION:  " + e.getMessage() );
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result){
            if (result){

                if( findViewById(R.id.currentWord) != null){
                    //Log.i(TAG, "onPostExecute: setting the next word as the bonus round creator");
                    setMakeNextWordBonusInitiator(true);
                    ((TextView) findViewById(R.id.currentWord)).setTextColor(Color.parseColor("#ffb900"));

                } else{
                    //Log.i(TAG, "onPostExecute: no null pointer exception here!");
                }
            }
            task = null;
        }
    }
}
