package com.example.pabloandtyler.comp512app;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.pabloandtyler.comp512app.dummy.PeerDataItem;
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
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TextFight extends AppCompatActivity implements PeerListItemsFragment.OnPeerClickedListener,
        JoinPeerAlert.JoinPeerAlertListener{

    private static final String TAG = "2FT: TextFight";
    private static String mode = null;
    private PeerListItemsFragment peerListItemsFragment = null;


    // Our handle to Nearby Connections
    private ConnectionsClient connectionsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_fight);
        connectionsClient = Nearby.getConnectionsClient(this);

        //EditText wordSpace = (EditText) findViewById(R.id.type_space);
        //wordSpace.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        //Determine which fragment to insert first
        FragmentManager fragmentManager = getSupportFragmentManager();
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
                //fragmentTransaction.add(R.id.multi_fragments, new INSERT_YOUR_TEXT_FIGHT_FRAGMENT_HERE);
                //fragmentTransaction.commit();
                Log.d(TAG, "requires you to input 'TextFight' fragment ");
            }
        }

    }

    @Override
    protected void onStart(){
        super.onStart();

        if(mode.equals(MainActivity.MODE_HOST)){
            startAdvertising();
        }
        if(mode.equals(MainActivity.MODE_PEER)){
            startDiscovery();
        }
    }


    //CALLBACKS FOR THE NEARBY CONNECTIONS API-----------------------------------------------------
    // Callbacks for receiving payloads
    private final PayloadCallback payloadCallback =
        new PayloadCallback() {
            @Override
            public void onPayloadReceived(String endpointId, Payload payload) {
                String text = new String(payload.asBytes(), UTF_8);
                Toast.makeText(TextFight.this, text, Toast.LENGTH_SHORT).show();
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
                "host", getPackageName(), connectionLifecycleCallback, new AdvertisingOptions(MainActivity.STRATEGY))
        .addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "we are successfully advertising");
                    }
                }
        )
        .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
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
                                Log.i(TAG, "We're looking for someone to SMASH");
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Could not discover");

                            }
                        }
                );
        Log.i(TAG, "end separate called to startDiscovery");
    }

    // Callbacks for finding other devices
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                    //find peers and update GUI here

                    Nearby.getConnectionsClient(getApplicationContext()).requestConnection(
                            "client",
                            endpointId,
                            connectionLifecycleCallback)
                            .addOnSuccessListener(
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //We successfully requested a connection. Now both
                                            // sides must accept before the connection is established
                                            Toast.makeText(TextFight.this,
                                                    "onSuccess: requested connection",
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
                    //auto accept client
                    Nearby.getConnectionsClient(TextFight.this).acceptConnection(endpointId, payloadCallback);
                    Log.d(TAG, "onConnectedInitiated, MODE = HOST");
                }

                else if(mode.equals(MainActivity.MODE_PEER)){
                    //TODO: add new peers to list but do not auto-accepet
                    peerListItemsFragment.insertPeer(
                            connectionInfo.getAuthenticationToken(),
                            endpointId,
                            connectionInfo.getEndpointName());

                    Log.i(TAG, "onConnectionInitiatd, MODE = PEER");
                }

            }

            @Override
            public void onConnectionResult(String endpointId, ConnectionResolution result) {
                switch (result.getStatus().getStatusCode()){
                    case ConnectionsStatusCodes.STATUS_OK:
                        //we're connected! can now start sending and receiving data
                        Log.i(TAG, "onConnectionResult: connection successful");

                        Toast.makeText(TextFight.this, "accepted peer!", Toast.LENGTH_SHORT).show();
                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        // The connection was rejected by one or both sides.
                        Toast.makeText(TextFight.this, "peer rejected", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onConnectionResult: connection failed");
                        break;
                    case ConnectionsStatusCodes.STATUS_ERROR:
                        //the connection broke before it was able to be accepted
                        break;
                }

            }

            @Override
            public void onDisconnected(String endpointId) {
                Log.i(TAG, "onDisconnected: disconnected from the opponent");
                Toast.makeText(TextFight.this, "disconnected", Toast.LENGTH_SHORT).show();

            }
        };



    // A callback from the fragment that a the user wants to potentially join a peer!
    @Override
    public void onPeerClicked(PeerDataItem item) {
        //TODO: initialize connection here with new peer, and display alert dialog.
        Toast.makeText(this, "Peer clicked", Toast.LENGTH_LONG).show();

        JoinPeerAlert alert = JoinPeerAlert.newInstance(item);
        alert.setmListener(this);
        alert.show(getSupportFragmentManager(), "uniqueStringTaglel");

    }

    @Override
    public void onAlertPositiveClick(PeerDataItem item) {

        Toast.makeText(this, "next fragment!", Toast.LENGTH_SHORT).show();

        //TODO: Accept connection to new peer
        //TODO: replace fragment with TextMainArena
        Nearby.getConnectionsClient(this)
                .acceptConnection(item.getEndpointId(),payloadCallback);
    }

    @Override
    public void onAlertNegativeClick() {
        Toast.makeText(this, "stay in fragment!", Toast.LENGTH_SHORT).show();

    }
}
