package com.example.pabloandtyler.comp512app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Alert dialog to confirm if the user wishes to join another peer
 */
public class JoinPeerAlert extends DialogFragment {

    JoinPeerAlertListener mListener = null;

    public interface JoinPeerAlertListener{
        void onAlertPositiveClick(PeerDataItem peer);
        void onAlertNegativeClick();
    }

    public JoinPeerAlert(){}

    protected void setmListener(JoinPeerAlertListener listener){
        mListener = listener;
    }

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment JoinPeerAlert.
     */
    public static JoinPeerAlert newInstance(PeerDataItem item) {

        Bundle args = new Bundle();
        JoinPeerAlert fragment = new JoinPeerAlert();
        args.putString("TOKEN", item.getAuthToken());
        args.putString("FRIENDLY_NAME", item.getFriendlyName());
        args.putString("ENDPOINT_ID", item.getEndpointId());
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * onCreateDialog is called by Android in order to create a copy of this dialog
     * @param savedInstanceState Bundle object containing information about a state to potentially revert
     * @return Dialog instance for reference within class
     */
    @NonNull
    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState){

        final String token = getArguments().getString("TOKEN");
        final String friendly_name = getArguments().getString("FRIENDLY_NAME");
        final String endpointId = getArguments().getString("ENDPOINT_ID");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Accept connection to " + token + "?")
                .setMessage("Confirm the token appears on the other device you wish to connect")
                .setPositiveButton("Join", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onAlertPositiveClick(new PeerDataItem(token, endpointId, friendly_name));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onAlertNegativeClick();
                    }
                });

        return builder.create();
    }


}
