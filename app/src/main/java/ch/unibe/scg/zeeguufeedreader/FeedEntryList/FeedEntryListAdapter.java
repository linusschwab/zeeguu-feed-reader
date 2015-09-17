package ch.unibe.scg.zeeguufeedreader.FeedEntryList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;

public class FeedEntryListAdapter extends BaseAdapter implements ListAdapter {

    private ArrayList<FeedEntry> entries;
    private LayoutInflater inflater;

    public FeedEntryListAdapter(Activity activity, ArrayList<FeedEntry> entries) {
        this.entries = entries;
        this.inflater = activity.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public FeedEntry getItem(int position) {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return entries.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return entries.get(position).getView(inflater, convertView, parent);
    }
}
