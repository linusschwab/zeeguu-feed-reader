package ch.unibe.scg.zeeguufeedreader.FeedOverview;

import android.app.Activity;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.Core.ContextManager;
import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyAccount;
import ch.unibe.scg.zeeguufeedreader.R;

public class FeedOverviewListAdapter extends BaseExpandableListAdapter {

    private ArrayList<Category> categories;
    private LayoutInflater inflater;

    private FeedOverviewListAdapterCallbacks callback;

    public interface FeedOverviewListAdapterCallbacks {
        void displayFeedEntryList(Category category);
        FeedlyAccount getFeedlyAccount();
    }

    public FeedOverviewListAdapter(Activity activity, Fragment fragment, ArrayList<Category> categories) {
        this.categories = categories;
        this.inflater = activity.getLayoutInflater();

        // Make sure that the interface is implemented in the fragment
        try {
            callback = (FeedOverviewListAdapterCallbacks) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException("Fragment must implement FeedOverviewListAdapterCallbacks");
        }
    }

    @Override
    public int getGroupCount() {
        return categories.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return categories.get(groupPosition).getFeedCount();
    }

    @Override
    public Category getGroup(int groupPosition) {
        return categories.get(groupPosition);
    }

    @Override
    public Feed getChild(int groupPosition, int childPosition) {
        return categories.get(groupPosition).getFeed(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return categories.get(groupPosition).getId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return categories.get(groupPosition).getFeed(childPosition).getId();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final Category category = categories.get(groupPosition);

        View categoryView = category.getView(inflater, convertView, parent, callback.getFeedlyAccount().showUnreadOnly());
        TextView categoryName = (TextView) categoryView.findViewById(R.id.category_name);

        if (!(category instanceof DefaultCategory)) {
            categoryName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.displayFeedEntryList(category);
                }
            });
        }

        return categoryView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return categories.get(groupPosition).getFeed(childPosition).getView(inflater, convertView, parent, callback.getFeedlyAccount().showUnreadOnly());
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
        Category category = categories.get(groupPosition);
        if (!category.isExpanded()) {
            category.expand();
            callback.getFeedlyAccount().saveCategory(category);
        }

        // Border
        if (categories.size() > groupPosition+1)
            categories.get(groupPosition+1).setBorder(true);
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
        Category category = categories.get(groupPosition);
        category.collapse();
        callback.getFeedlyAccount().saveCategory(category);

        // Border
        if (categories.size() > groupPosition+1)
            categories.get(groupPosition+1).setBorder(false);
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }
}
