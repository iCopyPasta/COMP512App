package com.example.pabloandtyler.comp512app;


import android.content.Context;
import android.graphics.Color;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class BonusRoundFragment extends Fragment {

    private static final String TAG = "2FT: BonusRoundFrag";

    public interface BonusRoundFragmentListener{
        String getPeerColor(String endpointId);
        List<String> getPeerEndpointIds();
    }

    private BonusRoundFragmentListener mListener;
    private TextView opponent1TextView = null;
    private TextView opponent2TextView = null;
    private TextView opponent3TextView = null;
    private ProgressBar opponent1ProgressBar = null;
    private ProgressBar opponent2ProgressBar = null;
    private ProgressBar opponent3ProgressBar = null;
    private Map<String, Integer> opponentMap = null;



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

        }

    }

    public void updateOpponentProgressBar(String endpointId, int progress){
        int i = opponentMap.get(endpointId);
        switch(i){
            case 1:
                opponent1ProgressBar.setProgress(progress);
                break;
            case 2:
                opponent2ProgressBar.setProgress(progress);
                break;
            case 3:
                opponent3ProgressBar.setProgress(progress);
                break;
            default:
                Log.e(TAG, "Unexpected switch statement position passed");
                break;
        }
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
