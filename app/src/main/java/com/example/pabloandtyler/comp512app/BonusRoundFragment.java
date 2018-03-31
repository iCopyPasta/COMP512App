package com.example.pabloandtyler.comp512app;


import android.content.Context;
import android.graphics.Color;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BonusRoundFragment.BonusRoundFragmentListener} interface
 * to handle interaction events.
 */
public class BonusRoundFragment extends Fragment
    implements View.OnKeyListener{

    private static final String TAG = "2FT: BonusRoundFrag";


    public interface BonusRoundFragmentListener{
        String getPeerColor(String endpointId);
        List<String> getPeerEndpointIds();
        void onBonusRoundProgressUpdate(int progress);
    }

    private BonusRoundFragmentListener mListener;
    private ProgressBar ENEMY1PB = null;
    private ProgressBar ENEMY2PB = null;
    private ProgressBar ENEMY3PB = null;


    private TextView ENEMY1TV = null;
    private TextView ENEMY2TV = null;
    private TextView ENEMY3YV = null;


    private Map<String, Integer> opponentMap = null;
    private EditText type_word = null;



    public BonusRoundFragment() {
        // Required empty public constructor
    }

    public static BonusRoundFragment newInstance() {
        return new BonusRoundFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bonus_round, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        TextView opponent1TextView = getActivity().findViewById(R.id.opponent1TextView);
        ENEMY1PB = getActivity().findViewById(R.id.progressBarOpponent1);
        TextView opponent2TextView = getActivity().findViewById(R.id.opponent2TextView);
        ENEMY2PB = getActivity().findViewById(R.id.progressBarOpponent2);
        TextView opponent3TextView = getActivity().findViewById(R.id.opponent3TextView);
        ENEMY3PB = getActivity().findViewById(R.id.progressBarOpponent3);

        //TODO: set colors if we have time
        /*
        // populate the TextViews with colors
        List<String> peers = mListener.getPeerEndpointIds();

        Iterator<String> iterator = peers.iterator();
        opponentMap = new HashMap<>();
        String tmp_opponent;
        String tmp_color;

        if(iterator.hasNext()){
            tmp_opponent = iterator.next();
            tmp_color = mListener.getPeerColor(tmp_opponent);
            opponent1TextView.setTextColor(Color.parseColor(
                    tmp_color));

            opponentMap.put(tmp_opponent, 1);
        }

        if(iterator.hasNext()){
            tmp_opponent = iterator.next();
            tmp_color = mListener.getPeerColor(tmp_opponent);
            opponent2TextView.setTextColor(Color.parseColor(
                    tmp_color));

            opponentMap.put(tmp_opponent, 2);

        }

        if(iterator.hasNext()){
            tmp_opponent = iterator.next();
            tmp_color = mListener.getPeerColor(tmp_opponent);

            opponent3TextView.setTextColor(Color.parseColor(
                    tmp_color));

            opponentMap.put(tmp_opponent, 3);

        }*/

        //set our listener for the bonus round keyboard
        //TODO: wire up UI with appropriate callbacks, properties, and other elements
        type_word = getActivity().findViewById(R.id.type_sentence);
        type_word.setOnKeyListener(BonusRoundFragment.this); //feedback will be handled within the app as an intermediate step
        type_word.setInputType(
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD); //disable auto-correct
        type_word.requestFocus();

        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null){
            Log.i(TAG, "forcing keyboard to appear");
            imm.toggleSoftInput(
                    InputMethodManager.SHOW_FORCED,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
            );

        }

        //display the keyboard if not already displayed
        type_word.callOnClick();

    }


    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

        if(keyCode == KeyEvent.KEYCODE_SPACE &&
                keyEvent.getAction() == KeyEvent.ACTION_DOWN){

            Log.i(TAG, "onKey if called");

            //TODO: determine the progress for the current sentence here
            String tmp = type_word.getText().toString();

            int progress = 20;

            Log.i(TAG, "back to TextFight with progress: " + progress);
            mListener.onBonusRoundProgressUpdate(progress);

            return true;

        }



        return false;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BonusRoundFragmentListener) {
            mListener = (BonusRoundFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


}
