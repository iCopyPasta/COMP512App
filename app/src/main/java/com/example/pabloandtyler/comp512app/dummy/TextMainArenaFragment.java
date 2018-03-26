package com.example.pabloandtyler.comp512app.dummy;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.example.pabloandtyler.comp512app.R;
import com.example.pabloandtyler.comp512app.TextFight;

import java.security.Key;
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
    private EditText type_word;
    private static final String FRIENDLY_NAME = "FRIENDLY_NAME";

    private int level = 4;
    private int tier = 0;
    private String currentWord;

    private static final int MAX_TIER = 1; //the amount of words to complete for the level to increase

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

        if (getArguments() != null) {
            //TODO: set up important information on UI from arguments
            ((TextView) getActivity().findViewById(R.id.friendly_name))
                    .setText(getArguments().getString(FRIENDLY_NAME));
        }

        //TODO: wire up UI with appropriate callbacks, properties, and other elements
        type_word = getActivity().findViewById(R.id.type_word);
        type_word.setOnKeyListener(this); //feedback will be handled within the app as an intermediate step
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

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

        //TODO: have additional logic for display/retrieval of words in the future
        //TODO: ensure the word is correct
        //TODO: have logic if the word is not correct upon enter

        Log.i(TAG, "onKey called");

        if(keyCode == KeyEvent.KEYCODE_ENTER &&
                keyEvent.getAction() == KeyEvent.ACTION_DOWN){
            //send a message to our parent activity
            String message = type_word.getText().toString();
            //TODO: test these methods to ensure proper behavior
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

        ((TextView) getActivity().findViewById(R.id.debug_message))
                .setText(message);
    }

    public void updateValues() {
        //updates the displayed current tier and current level
        Log.i(TAG,"updateValues() setting current level to " + String.valueOf(level) + " and current tier to " +String.valueOf(tier));

       // ((TextView) getActivity().findViewById(R.id.currentPlayerLevel))
        //        .setText(String.valueOf(level));

        ((TextView) getActivity().findViewById(R.id.currentTier))
                .setText(String.valueOf(tier));
    }

    public void correctEntry() {
        //updates the current word text view after incrementing tier, and level, if necessary
        //called after the user submits the correct word
        Log.i(TAG,"correctEntry()");
        ((TextView) getActivity().findViewById(R.id.passOrFail))
                .setText("");

        tier++;
        if (tier == MAX_TIER) {
            tier = 0;
            level++;
            updateMyState();
            if (level == 16) {
                victory();
            }

        }

        updateValues();

        getNewWord();

    }

    public void updateMyState() {
        TextFight.myState.setLevelOfPeer(level);
        mListener.onBroadcastState();
    }

    private void incorrectEntry() {
        //called after the user submits the incorrect word
        Log.i(TAG,"incorrectEntry()");

        ((TextView) getActivity().findViewById(R.id.passOrFail))
                .setText("Incorrect!");
    }

    public void getNewWord() {
        //returns a new word based on current level, be sure level is correct before invoking
        Log.i(TAG,"getNewWord()");
        Resources res = getResources();
        String[] words = {"AN ERROR OCCURRED, please report this bug to sukmoon@psu.edu."};

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



        int randomIndex = new Random().nextInt(words.length);
        currentWord = words[randomIndex];

        ((TextView) getActivity().findViewById(R.id.currentWord))
                .setText(currentWord);
    }

    public void victory() {
        //this player has won, what should be done?
        Log.i(TAG,"victory()");

        ((TextView) getActivity().findViewById(R.id.currentWord))
                .setText("YOU WON! CONGRATULATIONS!");

    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnTextMainFragmentInteractionListener {
        void onTextMainFragmentInteraction(String message);
        void onBroadcastState();
    }
}
