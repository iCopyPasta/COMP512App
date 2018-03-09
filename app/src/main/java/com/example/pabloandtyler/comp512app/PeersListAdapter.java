package com.example.pabloandtyler.comp512app;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pabloandtyler.comp512app.dummy.PeerDataItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PeerDataItem} and makes a call to the
 * specified {@link PeerListItemsFragment.OnPeerClickedListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class PeersListAdapter extends RecyclerView.Adapter<PeersListAdapter.ViewHolder> {

    private final List<PeerDataItem> mValues;
    private final PeerListItemsFragment.OnPeerClickedListener mListener;

    public PeersListAdapter(List<PeerDataItem> items, PeerListItemsFragment.OnPeerClickedListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i("PeersListAdapter", "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.peers_list_fragment_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onPeerClicked(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public PeerDataItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}
