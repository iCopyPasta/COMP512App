package com.example.pabloandtyler.comp512app;


import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import static java.lang.Math.max;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BonusRoundFragment.BonusRoundFragmentListener} interface
 * to handle interaction events.
 */
public class BonusRoundFragment extends Fragment {

    // constants for our application
    private static final String TAG = "2FT: BonusRoundFrag";
    private static final int THRESHOLD = 10;


    /**
     * public interface for callbacks
     */
    public interface BonusRoundFragmentListener{
        void onBroadcastState();
        void onClear();
        void onBStart();
        void bonusRoundEnd();
        void onDisableInput();
        void onSetWinnerSnapshot();
    }

    // our handle back to TextFight, for use with callbacks
    private BonusRoundFragmentListener mListener;

    // variables regarding GUI
    private ProgressBar ENEMY1PB = null;
    private ProgressBar ENEMY2PB = null;
    private ProgressBar ENEMY3PB = null;

    private TextView ENEMY1TV = null;
    private TextView ENEMY2TV = null;
    private TextView ENEMY3TV = null;

    public EditText type_word = null;

    // variables for a player's level
    private String battleWord;
    public int highestProgress = 0;
    public int thresholdPercentage = 0;

    public BonusRoundFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BonusRoundFragment.
     */
    public static BonusRoundFragment newInstance() {
        return new BonusRoundFragment();
    }

    /**
     * onCreateView is called by Android to actually inflate the fragment with our predefined layout
     * @param inflater - android provided callback variable
     * @param container the ViewGroup container to store our layout in
     * @param savedInstanceState variable to restore variables' contents in the fragment lifecycle
     * @return View with updated elements
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bonus_round, container, false);
    }

    /**
     * onResume is called by Android to graphically display a fragment in the fragment lifecycle.
     * We set appropriate variables instances here
     */
    @Override
    public void onResume(){
        super.onResume();

        // local variable reference
        ENEMY1TV = getActivity().findViewById(R.id.opponent1TextView);
        ENEMY1PB = getActivity().findViewById(R.id.progressBarOpponent1);
        ENEMY1PB.setVisibility(View.INVISIBLE);
        ENEMY2TV = getActivity().findViewById(R.id.opponent2TextView);
        ENEMY2PB = getActivity().findViewById(R.id.progressBarOpponent2);
        ENEMY2PB.setVisibility(View.INVISIBLE);
        ENEMY3TV = getActivity().findViewById(R.id.opponent3TextView);
        ENEMY3PB = getActivity().findViewById(R.id.progressBarOpponent3);
        ENEMY3PB.setVisibility(View.INVISIBLE);

        // clear out initial redisplay
        ENEMY1TV.setText("");
        ENEMY2TV.setText("");
        ENEMY3TV.setText("");

        ENEMY1PB.setProgress(0);
        ENEMY2PB.setProgress(0);
        ENEMY3PB.setProgress(0);

        // user input management
        Resources res = getResources();

        // set the current sentence and appropriate references
        TextView typeSentence = getActivity().findViewById(R.id.type_sentence);
        Log.i(TAG, "onResume: " +  TextFight.theState.getBonusRoundArrayIndex());
        battleWord = res.getStringArray(R.array.bonusDigitList)[TextFight.theState.getBonusRoundArrayIndex()];
        ((TextView) getActivity().findViewById(R.id.BFriendlyName)).setText(TextFight.myState.getFriendlyName());
        typeSentence.setText(battleWord);

        
        // set our listener for the bonus round keyboard
        type_word = (EditText) getActivity().findViewById(R.id.bonusRoundTypeSpace);
        type_word.addTextChangedListener(new TextWatcher() {
                                             @Override
                                             public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                             }

                                             @Override
                                             public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

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
        updateProgressBars();

    }

    /**
     * broadcast when appropriate and updates local values upon a substring's correct match to a correct sentence
     * @param progress int representing percentage of completion
     */
    public void correctSoFar(int progress) {
        Log.i(TAG, "correct so far with progress: " + progress);
        updateMyBar(progress);
        updateMyState(progress);
        //limit messages sent in bonus round for faster computation/efficiency
        if(progress > thresholdPercentage + THRESHOLD){
            Log.i(TAG, "correctSoFar: PROGRESS IS: " + progress + " > " + thresholdPercentage + THRESHOLD);
            thresholdPercentage += THRESHOLD;
            mListener.onBroadcastState();
        }

        ((TextView)getActivity().findViewById(R.id.NOTIFY)).setText("");
    }

    /**
     * upon misspelling a sentence, alert the user about a typo
     */
    public void incorrectSoFar() {
        ((TextView)getActivity().findViewById(R.id.NOTIFY)).setText(R.string.typo_in_sentence);
    }

    /**
     * update our information regarding the progress in the bonus round
     * @param progress int representing percentage of completion
     */
    public void updateMyState(int progress) {
        Log.i(TAG, "Updating my state with: " + progress);
        TextFight.myState.setPositionInBonusRound(progress);
    }

    /**
     * graphically updates the GUI regarding enemy progress and text
     */
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



    /**
     * start the voting process for this local peer in the game
     */
    public void claimVictory() {
        mListener.onDisableInput();
        TextFight.claimWinner = true;
        TextFight.theState.setTypeOfGame("N-W-P");
        mListener.onSetWinnerSnapshot();
        mListener.onBroadcastState();
        TextFight.theState.setTypeOfGame("BD");

    }

    /**
     * PRECONDITION: must have gathered all votes and be called in TextFight
     * broadcasts to the network this peer has won the game;
     * updates the local GUI to show victory
     */
    public void Victory() {
        Log.i(TAG, "Victory: FROM BONUS ROUND FRAGMENT");
        Toast.makeText(getActivity(), "BONUS WINNER", Toast.LENGTH_SHORT).show();
        TextFight.theState.setTypeOfGame("BD");
        Log.i(TAG, "Victory: Bonus Round setting level to "+TextFight.myState.getLevelOfPeer()+ "To " + TextFight.myState.getLevelOfPeer()+2);
        TextFight.myState.setLevelOfPeer(TextFight.myState.getLevelOfPeer()+3);
        Log.i(TAG, "New level:" +TextFight.myState.getLevelOfPeer());
        resetVictoryParams();
        TextFight.setBonusRoundTokenHolder(true);
        mListener.bonusRoundEnd();
    }

    /**
     * update our local copy of the progress bar when correctly moving up a level
     */
    public void updateMyBar(int progress) {
        Log.i(TAG,"updateMyBar() setting current BR progress to " + progress );

        ((ProgressBar) getActivity().findViewById(R.id.BYOURPB))
                .setProgress(progress);
    }

    /**
     * callback to TextFight to reset variables
     */
    public void resetVictoryParams(){
        Log.i(TAG,"resetting victory parameters");
        mListener.onClear();
    }


    /**
     * onAttach is called by Android to attach a fragment to an activity in the fragment lifecycle
     * @param context the activity who implements our interface for our fragment
     */
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

    /**
     * onDetach is called by Android to destroy the graphical fragment and remove it from an activity
     * in the fragment lifecycle
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


}
