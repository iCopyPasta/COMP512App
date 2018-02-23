package com.example.pabloandtyler.comp512app;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.w3c.dom.Text;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TextFight extends AppCompatActivity {

    private static final String TAG = "2FT: TextFight";

    // Our handle to Nearby Connections
    private ConnectionsClient connectionsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_fight);
        connectionsClient = Nearby.getConnectionsClient(this);

        EditText wordSpace = (EditText) findViewById(R.id.type_space);
        wordSpace.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }

    @Override
    protected void onStart(){
        super.onStart();
        startAdvertising();
    }


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

    // Callbacks for connections to other devices
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
        new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                Log.i(TAG, "onConnectionInitiated: accepting connection");
                Toast.makeText(TextFight.this, "accepting peer", Toast.LENGTH_SHORT).show();

                //auto accept client
                Nearby.getConnectionsClient(TextFight.this).acceptConnection(endpointId, payloadCallback);
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


}
