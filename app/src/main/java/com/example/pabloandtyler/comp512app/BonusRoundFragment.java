package com.example.pabloandtyler.comp512app;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.lang.Math.max;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BonusRoundFragment.BonusRoundFragmentListener} interface
 * to handle interaction events.
 */
public class BonusRoundFragment extends Fragment {

    private static final String TAG = "2FT: BonusRoundFrag";
    private static final int THRESHOLD = 10;


    public interface BonusRoundFragmentListener{
        void onBroadcastState();
        void onClear();
        void onBStart();
        void bonusRoundEnd();
        void onDisableInput();
        void onSetWinnerSnapshot();
    }

    private BonusRoundFragmentListener mListener;
    private ProgressBar ENEMY1PB = null;
    private ProgressBar ENEMY2PB = null;
    private ProgressBar ENEMY3PB = null;


    private TextView ENEMY1TV = null;
    private TextView ENEMY2TV = null;
    private TextView ENEMY3TV = null;


    private Map<String, Integer> opponentMap = null;
    public EditText type_word = null;

    private TextView typeSentence;

    private String battleWord;
    public int highestProgress = 0;
    public int thresholdPercentage = 0;

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

        ENEMY1TV = getActivity().findViewById(R.id.opponent1TextView);
        ENEMY1PB = getActivity().findViewById(R.id.progressBarOpponent1);
        ENEMY1PB.setVisibility(View.INVISIBLE);
        ENEMY2TV = getActivity().findViewById(R.id.opponent2TextView);
        ENEMY2PB = getActivity().findViewById(R.id.progressBarOpponent2);
        ENEMY2PB.setVisibility(View.INVISIBLE);
        ENEMY3TV = getActivity().findViewById(R.id.opponent3TextView);
        ENEMY3PB = getActivity().findViewById(R.id.progressBarOpponent3);
        ENEMY3PB.setVisibility(View.INVISIBLE);

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

        Resources res = getResources();

        typeSentence = getActivity().findViewById(R.id.type_sentence);
        Log.i(TAG, "onActivityCreated: " +  TextFight.theState.getBonusRoundArrayIndex());
        battleWord = res.getStringArray(R.array.bonusDigitList)[TextFight.theState.getBonusRoundArrayIndex()];
        ((TextView) getActivity().findViewById(R.id.BFriendlyName)).setText(TextFight.myState.getFriendlyName());
        typeSentence.setText(battleWord);

        
        //set our listener for the bonus round keyboard
        type_word = (EditText) getActivity().findViewById(R.id.bonusRoundTypeSpace);
        type_word.addTextChangedListener(new TextWatcher() {
                                             @Override
                                             public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                                 //Toast.makeText(getContext(), "BEFORE TEXT CHANGED", Toast.LENGTH_SHORT).show();

                                             }

                                             @Override
                                             public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                                 //Toast.makeText(getContext(), "ON TEXT CHANGED", Toast.LENGTH_SHORT).show();
                                                 String tmp = type_word.getText().toString();
                                                 Log.i(TAG, "onTextChanged: onTextChanged: " + tmp);

                                                 if (battleWord.substring(0,tmp.length()).equals(tmp)) {

                                                     if(tmp.equals(battleWord)) {
                                                         Log.i(TAG, "string complete. Victory claimed.");
                                                         claimVictory();
                                                     }
                                                     else {
                                                         highestProgress = max(highestProgress,(int) ((tmp.length() / (float) battleWord.length()) * 100));
                                                         correctSoFar(highestProgress);
                                                     }
                                                 }
                                                 else {
                                                     incorrectSoFar();
                                                 }

                                             }

                                             @Override
                                             public void afterTextChanged(Editable editable) {
                                                 //Toast.makeText(getContext(), "AFTER TEXT CHANGED", Toast.LENGTH_SHORT).show();

                                             }
                                         }
        );
        //type_word.setOnKeyListener(BonusRoundFragment.this); //feedback will be handled within the fragment as an intermediate step
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

        mListener.onBStart();

        //display the keyboard if not already displayed
        type_word.callOnClick();

        //clear out initial redisplay

        ENEMY1TV.setText("");
        ENEMY2TV.setText("");
        ENEMY3TV.setText("");

        ENEMY1PB.setProgress(0);
        ENEMY2PB.setProgress(0);
        ENEMY3PB.setProgress(0);

        updateProgressBars();

    }

    public void correctSoFar(int progress) {
        Log.i(TAG, "correct so far with progress: " + progress);
        updateMyBar(progress);
        updateMyState(progress);
        //limit messages sent in bonus round for faster computation/efficiency
        if(progress > thresholdPercentage + THRESHOLD){
            thresholdPercentage += THRESHOLD;
            mListener.onBroadcastState();
        }

        ((TextView)getActivity().findViewById(R.id.NOTIFY)).setText("");
    }

    public void incorrectSoFar() {
        ((TextView)getActivity().findViewById(R.id.NOTIFY)).setText("There is a typo in your sentence!");
    }

    public void updateMyState(int progress) {
        Log.i(TAG, "Updating my state with: " + progress);
        TextFight.myState.setPositionInBonusRound(progress);
    }

    public void updateProgressBars() {
        List<PeerState> temp = TextFight.theState.getPeersLevel();

        int counter = 1;

        for(PeerState el: temp){
            //only update progress bars and text for other people
            if(!el.equals(TextFight.myState)){
                //only one other enemy
                if(counter == 1){
                    //first slot is open
                    ENEMY1PB.setVisibility(View.VISIBLE);
                    ENEMY1TV.setText(el.getFriendlyName());
                    ENEMY1PB.setProgress(el.getPositionInBonusRound());
                    counter++;
                }

                //two enemies
                else if(counter == 2){
                    //second slot is open
                    ENEMY2PB.setVisibility(View.VISIBLE);
                    ENEMY2TV.setText(el.getFriendlyName());
                    ENEMY2PB.setProgress(el.getPositionInBonusRound());
                    counter++;
                }

                //more than or equal to 3
                else if(counter >= 3){
                    //third slot is open
                    ENEMY3PB.setVisibility(View.VISIBLE);
                    ENEMY3TV.setText(el.getFriendlyName());
                    ENEMY3PB.setProgress(el.getPositionInBonusRound());
                    counter++;
                }
            }
        }
    }



    public void claimVictory() {
        mListener.onDisableInput();
        TextFight.claimWinner = true;
        TextFight.theState.setTypeOfGame("N-W-P");
        mListener.onSetWinnerSnapshot();
        mListener.onBroadcastState();
        TextFight.theState.setTypeOfGame("B");

    }

    public void Victory() {
        Log.i(TAG, "Victory: FROM BONUS ROUND FRAGMENT");
        TextFight.theState.setTypeOfGame("BD");
        Log.i(TAG, "Victory: Bonus Round setting level to "+TextFight.myState.getLevelOfPeer()+ "To " + TextFight.myState.getLevelOfPeer()+2);
        TextFight.myState.setLevelOfPeer(TextFight.myState.getLevelOfPeer()+2);

        Log.i(TAG, "New level:" +TextFight.myState.getLevelOfPeer());
        resetVictoryParams();
        TextFight.setBonusRoundTokenHolder(true);
        mListener.bonusRoundEnd();
    }

    public void updateMyBar(int progress) {
        Log.i(TAG,"updateMyBar() setting current BR progress to " + progress );

        ((ProgressBar) getActivity().findViewById(R.id.BYOURPB))
                .setProgress(progress);
    }

    public void resetVictoryParams(){
        Log.i(TAG,"resetting victory parameters");
        mListener.onClear();
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
