package com.example.pabloandtyler.comp512app;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the {@link OnPeerClickedListener}
 * interface.
 */
public class PeerListItemsFragment extends Fragment {

    // constants for our application
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String TAG = "2FT: PeerListFragment";

    // local variables for usage
    private int mColumnCount = 1;
    private OnPeerClickedListener mListener;
    private ArrayAdapter<PeerDataItem> peersAdapter;

    /**
     * update a new local peer into the ListView
     * @param authToken String containing the authentication token belonging to a new peer
     * @param endpointId String containing the endpointId token belonging to a new peer
     * @param friendlyName String containing the friendlyName token belonging to a new peer
     */
    public void insertPeer(String authToken, String endpointId, String friendlyName){
        Log.i(TAG, "insertPeer");
        PeerDataItem tmp = new PeerDataItem(authToken, endpointId, friendlyName);
        Log.i(TAG, tmp.toString());
        peersAdapter.add(tmp);

    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PeerListItemsFragment() {
    }

    @SuppressWarnings("unused")
    public static PeerListItemsFragment newInstance(int columnCount) {
        PeerListItemsFragment fragment = new PeerListItemsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        //((TextView) getActivity().findViewById(R.id.)).setText("Search for peers");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.peers_list_fragment_item_list, container, false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPeerClickedListener) {
            mListener = (OnPeerClickedListener) context;


        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        // Set the adapter
        peersAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_activated_1);

        ((ListViewCompat) getActivity().findViewById(R.id.list)).setAdapter(peersAdapter);
        ((ListViewCompat) getActivity().findViewById(R.id.list)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG, "onListItemClick");
                mListener.onPeerClicked(peersAdapter.getItem(i));
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnPeerClickedListener {

        void onPeerClicked(PeerDataItem item);
    }
}


