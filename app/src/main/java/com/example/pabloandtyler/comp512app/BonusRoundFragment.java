package com.example.pabloandtyler.comp512app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BonusRoundFragment.BonusRoundFragmentListener} interface
 * to handle interaction events.
 */
public class BonusRoundFragment extends Fragment {

    public interface BonusRoundFragmentListener{
        String getPeerColor(String endpointId);
        Set<String> getPeerEndpointIds();

    }

    private BonusRoundFragmentListener mListener;
    private TextView opponent1TextView = null;
    private TextView opponent2TextView = null;
    private TextView opponent3TextView = null;
    private ProgressBar opponent1ProgressBar = null;
    private ProgressBar opponent2ProgressBar = null;
    private ProgressBar opponent3ProgressBar = null;


    public BonusRoundFragment() {
        // Required empty public constructor
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

        opponent1TextView = getActivity().findViewById(R.id.opponent1TextView);
        opponent1ProgressBar = getActivity().findViewById(R.id.progressBarOpponent1);
        opponent2TextView = getActivity().findViewById(R.id.opponent2TextView);
        opponent2ProgressBar = getActivity().findViewById(R.id.progressBarOpponent2);
        opponent3TextView = getActivity().findViewById(R.id.opponent3TextView);
        opponent3ProgressBar = getActivity().findViewById(R.id.progressBarOpponent3);



        // populate the TextViews with colors
        Set<String> colors = mListener.getPeerEndpointIds();

        Iterator<String> iterator = colors.iterator();

        if(iterator.hasNext())
            opponent1TextView.setTextColor(Color.parseColor(iterator.next()));

        if(iterator.hasNext())
            opponent2TextView.setTextColor(Color.parseColor(iterator.next()));

        if(iterator.hasNext())
            opponent3TextView.setTextColor(Color.parseColor(iterator.next()));


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
