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

    private static final String TAG = "2FT: TextMainFrag";
    private OnTextMainFragmentInteractionListener mListener;
    public EditText type_word;
    private static final String FRIENDLY_NAME = "FRIENDLY_NAME";

    public int level = 4; //TODO: reset back to level 4
    private int tier = 0;
    private String currentWord;

    //TODO: toggle for presentation and Null demonstration
    private static final int MAX_TIER = 1; //the amount of words to complete for the level to increase

    //enemy progress bars
    private ProgressBar ENEMY1PB;
    private ProgressBar ENEMY2PB;
    private ProgressBar ENEMY3PB;

    //enemy text views
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

    public static TextMainArenaFragment newInstance(String friendly_name) {
        TextMainArenaFragment fragment = new TextMainArenaFragment();
        Bundle args = new Bundle();
        args.putString(FRIENDLY_NAME, friendly_name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        //display our local name when coming to this fragment
        ((TextView) getActivity().findViewById(R.id.friendly_name)).setText(TextFight.myState.getFriendlyName());

        //TODO: wire up UI with appropriate callbacks, properties, and other elements
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_text_main_arena, container, false);
    }

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

    //TODO: think of appropriate logic and tasks to perform when resuming this fragment
    @Override
    public void onResume(){
        super.onResume();
        getNewWord();
        updateValues();

        ENEMY1PB = getActivity().findViewById(R.id.ENEMY1PB);
        ENEMY2PB = getActivity().findViewById(R.id.ENEMY2PB);
        ENEMY3PB = getActivity().findViewById(R.id.ENEMY3PB);

        ENEMY1TV = getActivity().findViewById(R.id.ENEMY1TV);
        ENEMY2TV = getActivity().findViewById(R.id.ENEMY2TV);
        ENEMY3TV = getActivity().findViewById(R.id.ENEMY3TV);


        //default creation/progress
        ((ProgressBar) getActivity().findViewById(R.id.YOURPB))
                .setProgress( (int) (level * 6.25));

        if(TextFight.isBonusRoundTokenHolder()){
            Log.i(TAG, "onResume: executing background task");
            mListener.startBToken();
        }

        updateProgressBars();


        if (level >= 16) {
            claimVictory();
        }

        ((EditText) getActivity().findViewById(R.id.type_word)).setEnabled(true);
        ((EditText) getActivity().findViewById(R.id.type_word)).setText("");
        ((TextView) getActivity().findViewById(R.id.currentWord))
                .setTextColor(Color.parseColor("#000000"));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

        Log.i(TAG, "onKey called");

        if(keyCode == KeyEvent.KEYCODE_ENTER &&
                keyEvent.getAction() == KeyEvent.ACTION_DOWN){
            //send a message to our parent activity
            String message = type_word.getText().toString();
            type_word.setText(""); //clear out the composed text thus far
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .showSoftInput(type_word, InputMethodManager.SHOW_FORCED);
            //Log.i(TAG, "back to TextFight with message: " + message);
            //mListener.onTextMainFragmentInteraction(message);

            if (message.equals(currentWord)) {
                correctEntry();
            }
            else {
                incorrectEntry();
            }

            return true;

        }
        return false;
    }

    //CODE specific to this TextMainArenaFragment

    public void updateFriendlyName(String friendly_name){
        ((TextView) getActivity().findViewById(R.id.friendly_name))
                .setText(friendly_name);
    }

    public void updateDebugMessage(String message){

        Log.i(TAG,"we received: " + message);

        //((TextView) getActivity().findViewById(R.id.debug_message))
        //        .setText(message);
    }

    public void updateValues() {
        //updates the displayed current tier and current level
        Log.i(TAG,"updateValues() setting current level to " + String.valueOf(level) + " and current tier to " +String.valueOf(tier));

        ((ProgressBar) getActivity().findViewById(R.id.YOURPB))
              .setProgress( (int) (TextFight.myState.getLevelOfPeer() * 6.25));
    }

    public void correctEntry() {
        //updates the current word text view after incrementing tier, and level, if necessary
        //called after the user submits the correct word

        if(TextFight.theState.getTypeOfGame().equals("W")){
            return;
        }

        Log.i(TAG,"correctEntry()");
        ((TextView) getActivity().findViewById(R.id.passOrFail))
                .setText("");

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

    public void updateMyState() {
        TextFight.myState.setLevelOfPeer(level);
        mListener.onBroadcastState();
    }

    private void incorrectEntry() {
        //called after the user submits the incorrect word
        Log.i(TAG,"incorrectEntry()");

        if(TextFight.isMakeNextWordBonusInitiator()){
            //upon an incorrect spelling of a bonus-round initiator,
            // we should pass along the token holder roll
            //Toast.makeText(getContext(), "PASS ALONG WORD", Toast.LENGTH_SHORT).show();
            TextFight.setBonusRoundTokenHolder(false);
            TextFight.setMakeNextWordBonusInitiator(false);
            mListener.onSendToken();

            //clear our visual display of the bonus color
            ((TextView) getActivity().findViewById(R.id.currentWord))
                    .setTextColor(Color.parseColor("#000000"));


        }

        ((TextView) getActivity().findViewById(R.id.passOrFail))
                .setText(R.string.incorrect_entry);
    }

    public void getNewWord() {
        //returns a new word based on current level, be sure level is correct before invoking
        Log.i(TAG,"getNewWord()");
        Resources res = getResources();
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

    public void claimVictory(){
        mListener.onDisableInput();
        TextFight.theState.setTypeOfGame("N-W-P");
        TextFight.claimWinner = true;
        mListener.onSetWinnerSnapshot();
        mListener.onBroadcastState();
    }

    public void victory() {
        Log.i(TAG,"victory()");

        ((TextView) getActivity().findViewById(R.id.currentWord))
                .setText(R.string.win_message);

        ((TextView) getActivity().findViewById(R.id.currentWord))
                .setTextColor(Color.parseColor("#ffb900"));

        TextFight.theState.setTypeOfGame("W");
        mListener.onBroadcastState();

    }

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
