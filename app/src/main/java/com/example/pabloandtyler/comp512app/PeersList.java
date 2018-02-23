package com.example.pabloandtyler.comp512app;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PeersList extends AppCompatActivity {

    private static final String TAG = "2FT: PeersList";

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter<String> adapter;

    // Our handle to Nearby Connections
    private ConnectionsClient connectionsClient;

    // Callbacks for receiving payloads
    private final PayloadCallback payloadCallback =
        new PayloadCallback() {
            @Override
            public void onPayloadReceived(String endpointId, Payload payload) {
                String text = new String(payload.asBytes(), UTF_8);
                Toast.makeText(PeersList.this, text, Toast.LENGTH_SHORT).show();

            }


            @Override
            public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {

            }
        };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peers_list);

        //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
        ArrayList<String> listItems=new ArrayList<String>();

        Log.i(TAG, "setting adapter to simple_list_item_1");
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,
                listItems);

        connectionsClient = Nearby.getConnectionsClient(this);

    }

    @Override
    protected void onStart(){
        super.onStart();
        startDiscovery(); //find peers to join
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
                                        Toast.makeText(PeersList.this, "onSuccess: requested connection", Toast.LENGTH_SHORT).show();
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

                adapter.add(endpointId);
                adapter.notifyDataSetChanged();

                Log.i(TAG, "peer found");
            }

            @Override
            public void onEndpointLost(String endpointId) {
                Log.i(TAG, "endpoint lost");
            }
        };

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


    // Callbacks for connections to other devices
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
        new ConnectionLifecycleCallback() {

            @Override
            public void onConnectionInitiated(final String endpointId, ConnectionInfo connectionInfo) {

                new AlertDialog.Builder(PeersList.this)
                        .setTitle("Accept connection to " + connectionInfo.getEndpointName())
                        .setMessage("Confirm the code " + connectionInfo.getAuthenticationToken() +
                                " is also displayed on the over device")
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //The user confirmed, so we can accept the connection
                                Nearby.getConnectionsClient(getApplicationContext()).
                                        acceptConnection(endpointId, payloadCallback );
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // The user cancelled, so we sould reject the connection.
                                Nearby.getConnectionsClient(getApplicationContext()).rejectConnection(endpointId);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }

            @Override
            public void onConnectionResult(String endpointId, ConnectionResolution result) {
                if (result.getStatus().isSuccess()) {
                    Log.i(TAG, "onConnectionResult: connection successful");

                    Toast.makeText(PeersList.this, "accepted peer!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(PeersList.this, "peer rejected", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "onConnectionResult: connection failed");
                }
            }

            @Override
            public void onDisconnected(String endpointId) {
                Log.i(TAG, "onDisconnected: disconnected from the opponent");
                Toast.makeText(PeersList.this, "disconnected", Toast.LENGTH_SHORT).show();

            }
        };

}
