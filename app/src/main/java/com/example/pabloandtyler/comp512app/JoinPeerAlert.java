package com.example.pabloandtyler.comp512app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


/**
 * Created by peo5032 on 3/2/18.
 */

public class JoinPeerAlert extends DialogFragment {

    JoinPeerAlertListener mListener = null;


    public interface JoinPeerAlertListener{
        void onAlertPositiveClick();
        void onAlertNegativeClick();
    }

    public JoinPeerAlert(){}

    protected void setmListener(JoinPeerAlertListener listener){
        mListener = listener;
    }

    public static JoinPeerAlert newInstance(String authToken) {

        Bundle args = new Bundle();
        JoinPeerAlert fragment = new JoinPeerAlert();
        args.putString("TOKEN", authToken);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState){

        String token = getArguments().getString("TOKEN");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Accept connection to " + token + "?")
                .setMessage("Confirm the token appears on the other device you wish to connect")
                .setPositiveButton("Join", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onAlertPositiveClick();
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
