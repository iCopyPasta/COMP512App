package com.example.pabloandtyler.comp512app;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
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
import com.google.android.gms.nearby.connection.PayloadTransferUpdate.Status;
import com.google.android.gms.nearby.connection.Strategy;

import java.util.Random;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MainActivity extends AppCompatActivity {

    // constants for our application
    private static final String TAG = "2FT:MainActivity";
    public static final String MODE_HOST = "HOST";
    public static final String MODE_PEER = "PEER";
    public static final String MODE = "MODE";
    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;
    public static final Strategy STRATEGY = Strategy.P2P_CLUSTER;

    // Android specific permissions for use with Nearby Connections API
    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    /**
     * onCreate is called by Android when creating this activity for the first time
     * @param savedInstanceState Bundle object containing information about a state to potentially revert
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * onStart is called by Android when the activity will be visible to the user.
     * We check if we have permissions, and if not, request them
     */
    @Override
    protected void onStart() {
        super.onStart();

        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            //Log.i(TAG, "does not have permissions");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //Log.i(TAG, "requesting permissions");
                requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
            }
        }
    }

    /**
     * onStop is called by Android when the activity will no longer be visible to the user.
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    /** Returns true if the app was granted all the permissions. Otherwise, returns false. */
    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /** Handles user acceptance (or denial) of our permission request. */
    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "needs permissions to continue", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        recreate();
    }

    /** Finds an opponent to play the game with using Nearby Connections API. */
    public void findOpponent(View view) {
        //Log.i(TAG, "new activity: MODE_PEER passed in");
        Intent intent = new Intent(this, TextFight.class);
        intent.putExtra(MODE, MODE_PEER);
        intent.putExtra("FIRST_NODE", false);
        startActivity(intent);

    }

    /** Hosts a game for a peer using the Nearby Connections API*/
    public void hostGame(View view){
        //Log.i(TAG, "new activity: MODE_HOST passed in");
        Intent intent = new Intent(this, TextFight.class);
        intent.putExtra(MODE, MODE_HOST);
        intent.putExtra("FIRST_NODE", true);
        startActivity(intent);

    }
}
