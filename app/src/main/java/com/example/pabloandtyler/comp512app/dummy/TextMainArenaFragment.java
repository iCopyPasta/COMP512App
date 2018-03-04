package com.example.pabloandtyler.comp512app.dummy;

import android.content.Context;
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

import java.security.Key;

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

    private static final String TAG = "TextMainFragment";
    private OnTextMainFragmentInteractionListener mListener;
    private EditText type_word;
    private static final String FRIENDLY_NAME = "FRIENDLY_NAME";

    public TextMainArenaFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TextMainArenaFragment.
     */
    // TODO: Rename and change types and number of parameters
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

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        //TODO: have additional logic for display/retrieval of words in the future
        Log.i(TAG, "onKey called");

        if(keyCode == KeyEvent.KEYCODE_ENTER &&
                keyEvent.getAction() == KeyEvent.ACTION_DOWN){
            //send a message to our parent activity
            String message = type_word.getText().toString();
            //TODO: test these methods to ensure proper behavior
            type_word.setText(""); //clear out the composed text thus far
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .showSoftInput(type_word, InputMethodManager.SHOW_FORCED);
            Log.i(TAG, "back to TextFight with message: " + message);
            mListener.onTextMainFragmentInteraction(message);

            return true;

        }
        return false;
    }

    public void updateFriendlyName(String friendly_name){
        ((TextView) getActivity().findViewById(R.id.friendly_name))
                .setText(friendly_name);
    }

    public void updateDebugMessage(String message){
        Log.i(TAG,"we received: " + message);

        ((TextView) getActivity().findViewById(R.id.debug_message))
                .setText(message);
    }


    //CODE specific to this TextMainArenaFragment

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnTextMainFragmentInteractionListener {
        // TODO: Update argument type and name
        void onTextMainFragmentInteraction(String message);
    }
}
