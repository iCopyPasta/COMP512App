package com.example.pabloandtyler.comp512app;

import android.content.Context;
import android.content.res.Resources;
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
import android.widget.Toast;

import java.util.List;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TextMainArenaFragment.OnTextMainFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TextMainArenaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TextMainArenaFragment extends Fragment
    implements View.OnKeyListener{

    // constants for the application
    private static final String TAG = "2FT: TextMainFrag";
    private static final String FRIENDLY_NAME = "FRIENDLY_NAME";

    // our handle back to TextFight, for use with callbacks
    private OnTextMainFragmentInteractionListener mListener;
    // variables for keeping track of a player's progress
    public int level = 4;

    private int tier = 0;
    private static final int MAX_TIER = 4; //the amount of words to complete for the level to increase

    // variables related to seeing and typing a word
    private String currentWord;
    public EditText type_word;

    // enemy progress bars
    private ProgressBar ENEMY1PB;
    private ProgressBar ENEMY2PB;
    private ProgressBar ENEMY3PB;

    // enemy text views
    private TextView ENEMY1TV;
    private TextView ENEMY2TV;
    private TextView ENEMY3TV;

    public TextMainArenaFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TextMainArenaFragment.
     */
    public static TextMainArenaFragment newInstance() {
        return new TextMainArenaFragment();
    }

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     * @param friendly_name is to be automatically displayed when entering this fragment
     * @return A new instance of fragment TextMainArenaFragment.
     */
    public static TextMainArenaFragment newInstance(String friendly_name) {
        TextMainArenaFragment fragment = new TextMainArenaFragment();
        Bundle args = new Bundle();
        args.putString(FRIENDLY_NAME, friendly_name);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * onActivityCreated is called by Android in the fragment lifecycle.
     * We set references appropriately and display parts of the screen.
     * @param savedInstanceState a Bundle object that may provide information about an instance to revert to
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        // display our local name when coming to this fragment
        ((TextView) getActivity().findViewById(R.id.friendly_name)).setText(TextFight.myState.getFriendlyName());

        // user-input management and setup for the EditText to type in
        type_word = getActivity().findViewById(R.id.type_word);
        type_word.setOnKeyListener(this); //feedback will be handled within the app as an intermediate step
        type_word.setInputType(
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD); //disable auto-correct
        type_word.setText(""); //clear out the composed text thus far
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

        //display the keyboard if not already displayed again
        type_word.callOnClick();
        type_word.setEnabled(true);

    }

    /**
     * onCreate method is called by Android when fragment is made for the first time.
     * @param savedInstanceState variable to restore variables' contents in the fragment lifecycle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        return inflater.inflate(R.layout.fragment_text_main_arena, container, false);
    }

    /**
     * onAttach is called by Android to attach a fragment to an activity in the fragment lifecycle
     * @param context the activity who implements our interface for our fragment
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTextMainFragmentInteractionListener) {
            mListener = (OnTextMainFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * onResume is called by Android to graphically display a fragment in the fragment lifecycle.
     * We set appropriate variables instances here
     */
    @Override
    public void onResume(){
        super.onResume();

        // update the user for a new word
        getNewWord();
        updateValues();

        // set variables for reference again
        ENEMY1PB = getActivity().findViewById(R.id.ENEMY1PB);
        ENEMY2PB = getActivity().findViewById(R.id.ENEMY2PB);
        ENEMY3PB = getActivity().findViewById(R.id.ENEMY3PB);

        ENEMY1TV = getActivity().findViewById(R.id.ENEMY1TV);
        ENEMY2TV = getActivity().findViewById(R.id.ENEMY2TV);
        ENEMY3TV = getActivity().findViewById(R.id.ENEMY3TV);


        // default creation/progress
        ((ProgressBar) getActivity().findViewById(R.id.YOURPB))
                .setProgress( (int) (level * 6.25));

        // if we came back or are here for the first time and we have a token, start our timer
        if(TextFight.isBonusRoundTokenHolder()){
            Log.i(TAG, "onResume: executing background task");
            mListener.startBToken();
        }

        // update UI
        updateProgressBars();

        // if we came back and are of high enough level, start the voting process
        if (level >= 16) {
            claimVictory();
        }

        // update and enable UI elements
        ((EditText) getActivity().findViewById(R.id.type_word)).setEnabled(true);
        ((EditText) getActivity().findViewById(R.id.type_word)).setText("");
        ((TextView) getActivity().findViewById(R.id.currentWord))
                .setTextColor(Color.parseColor("#000000"));
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

    /**
     * onKey is called by Android for the EditText, this fragment implements the callback
     * our usage is to ensure we spelled a word correctly
     * @param view the graphical view this key listener is using
     * @param keyCode the keyCode representation of what the user inputted
     * @param keyEvent object that contains specific details regarding a user action
     * @return whether or not we consumed the event this listener caught
     */
    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

        Log.i(TAG, "onKey called");

        // check if the user pressed 'enter' to finish typing a word
        if(keyCode == KeyEvent.KEYCODE_ENTER &&
                keyEvent.getAction() == KeyEvent.ACTION_DOWN){

            // grab the current word to type
            String message = type_word.getText().toString();

            type_word.setText(""); //clear out the composed text thus far
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .showSoftInput(type_word, InputMethodManager.SHOW_FORCED);


            // perform logic if they spelled the word correctly
            if (message.equals(currentWord)) {
                correctEntry();
            }
            else {
                incorrectEntry();
            }

            // notify android we consumed the event
            return true;

        }
        return false;
    }

    /**
     * update the GUI to reflect our friendly name to the user
     * @param friendly_name the peer's randomly generated plain-text name
     */
    public void updateFriendlyName(String friendly_name){
        ((TextView) getActivity().findViewById(R.id.friendly_name))
                .setText(friendly_name);
    }

    /**
     * update our local copy of the progress bar when correctly moving up a level
     */
    public void updateValues() {
        //updates the displayed current tier and current level
        Log.i(TAG,"updateValues() setting current level to " + String.valueOf(level) + " and current tier to " +String.valueOf(tier));

        ((ProgressBar) getActivity().findViewById(R.id.YOURPB))
              .setProgress( (int) (TextFight.myState.getLevelOfPeer() * 6.25));
    }

    /**
     * when spelling a word correctly, update our level and check if we need to initiate a bonus round
     */
    public void correctEntry() {

        if(TextFight.theState.getTypeOfGame().equals("W")){
            return;
        }

        // clear out message if the user spelled a word incorrectly last time
        ((TextView) getActivity().findViewById(R.id.passOrFail))
                .setText("");

        // update the tier and level, checking for win conditions and bonus round conditions
        tier++;
        if (tier == MAX_TIER) {
            tier = 0;
            level++;
            updateMyState();
            if (level >= 16) {
                updateValues();
                claimVictory();
            }
            else {
                updateValues();

                if(TextFight.isMakeNextWordBonusInitiator() && TextFight.isBonusRoundTokenHolder()){
                    //upon a correct spelling of a bonus-round initiator, we should start the bonus round
                    Toast.makeText(getActivity(), "BONUS ROUND INITIATE", Toast.LENGTH_SHORT).show();
                    mListener.onBonusRoundTransition();

                }
                getNewWord();
            }

        }
        else{ //get a normal word
            if(TextFight.isMakeNextWordBonusInitiator() && TextFight.isBonusRoundTokenHolder()){
                //upon a correct spelling of a bonus-round initiator, we should start the bonus round
                Toast.makeText(getActivity(), "BONUS ROUND INITIATE", Toast.LENGTH_SHORT).show();

                mListener.onBonusRoundTransition();

            }
            getNewWord();
        }



    }

    /**
     * updates the local game state container with our knowledge of our new state,
     * letting others know as well
     */
    public void updateMyState() {
        TextFight.myState.setLevelOfPeer(level);
        mListener.onBroadcastState();
    }

    /**
     * when a user types a word incorrectly, check any bonus round logic and update an incorrect entry
     */
    private void incorrectEntry() {

        Log.i(TAG,"incorrectEntry()");

        // check if we hold a token and pass along if necessary
        if(TextFight.isMakeNextWordBonusInitiator()){


            TextFight.setBonusRoundTokenHolder(false);
            TextFight.setMakeNextWordBonusInitiator(false);

            mListener.onSendToken();

            //clear our visual display of the bonus color
            ((TextView) getActivity().findViewById(R.id.currentWord))
                    .setTextColor(Color.parseColor("#000000"));
        }

        // let the user know they misspelled the word
        ((TextView) getActivity().findViewById(R.id.passOrFail))
                .setText(R.string.incorrect_entry);
    }

    /**
     * retrieves and sets a new word to the user depending on the currently set level of the player
     */
    public void getNewWord() {

        Resources res = getResources();
        // lol
        String[] words = {"AN ERROR OCCURRED, please report all bugs to sukmoon@psu.edu."};

        if (level ==4) {
            words = res.getStringArray(R.array.fourDigitList);
        }
        else if (level == 5) {
            words = res.getStringArray(R.array.fiveDigitList);
        }
        else if (level == 6) {
            words = res.getStringArray(R.array.sixDigitList);
        }
        else if (level == 7) {
            words = res.getStringArray(R.array.sevenDigitList);
        }
        else if (level == 8) {
            words = res.getStringArray(R.array.eightDigitList);
        }
        else if (level == 9) {
            words = res.getStringArray(R.array.nineDigitList);
        }
        else if (level == 10) {
            words = res.getStringArray(R.array.tenDigitList);
        }
        else if (level == 11) {
            words = res.getStringArray(R.array.elevenDigitList);
        }
        else if (level == 12) {
            words = res.getStringArray(R.array.twelveDigitList);
        }
        else if (level == 13) {
            words = res.getStringArray(R.array.thirteenDigitList);
        }
        else if (level == 14) {
            words = res.getStringArray(R.array.fourteenDigitList);
        }
        else if (level == 15) {
            words = res.getStringArray(R.array.fifteenDigitList);
        }
        else if(level >= 16)
            words = new String[]{""};

        int randomIndex = new Random().nextInt(words.length);
        currentWord = words[randomIndex];

        ((TextView) getActivity().findViewById(R.id.currentWord))
                .setText(currentWord);
    }

    /**
     * start the voting process for this local peer in the game
     */
    public void claimVictory(){
        mListener.onDisableInput();
        TextFight.theState.setTypeOfGame("N-W-P");
        TextFight.claimWinner = true;
        mListener.onSetWinnerSnapshot();
        mListener.onBroadcastState();
    }

    /**
     * PRECONDITION: must have gathered all votes and be called in TextFight
     * broadcasts to the network this peer has won the game;
     * updates the local GUI to show victory
     */
    public void victory() {
        Log.i(TAG,"victory()");

        ((TextView) getActivity().findViewById(R.id.currentWord))
                .setText(R.string.win_message);

        ((TextView) getActivity().findViewById(R.id.currentWord))
                .setTextColor(Color.parseColor("#ffb900"));

        TextFight.theState.setTypeOfGame("W");
        mListener.onBroadcastState();

    }

    /**
     * graphically updates the GUI regarding enemy progress and text
     */
    public void updateProgressBars(){
        List<PeerState> temp = TextFight.theState.getPeersLevel();

        int counter = 1;

        for(PeerState el: temp){
            //only update progress bars and text for other people
            if(!el.equals(TextFight.myState)){
                //only one other enemy
                if(counter ==1){

                    //first slot is open
                    ENEMY1PB.setVisibility(View.VISIBLE);
                    ENEMY1TV.setText(el.getFriendlyName());
                    ENEMY1PB.setProgress((int) (el.getLevelOfPeer() * 6.25));
                    counter++;

                }

                //two enemies
                else if(counter == 2){

                        //second slot is open
                        ENEMY2PB.setVisibility(View.VISIBLE);
                        ENEMY2TV.setText(el.getFriendlyName());
                        ENEMY2PB.setProgress((int) (el.getLevelOfPeer() * 6.25));
                        counter++;
                }

                //more than or equal to 3
                else if(counter >=3){
                        //third slot is open
                        ENEMY3PB.setVisibility(View.VISIBLE);
                        ENEMY3TV.setText(el.getFriendlyName());
                        ENEMY3PB.setProgress((int) (el.getLevelOfPeer() * 6.25));

                }
            }
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnTextMainFragmentInteractionListener {
        void onSendToken();
        void onBroadcastState();
        void onBonusRoundTransition();
        void onSetWinnerSnapshot();
        void onDisableInput();
        void startBToken();
    }
}
