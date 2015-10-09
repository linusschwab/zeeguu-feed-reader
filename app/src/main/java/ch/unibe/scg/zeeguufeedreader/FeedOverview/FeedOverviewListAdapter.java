package ch.unibe.scg.zeeguufeedreader.FeedOverview;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.R;

public class FeedOverviewListAdapter extends BaseExpandableListAdapter {

    private ArrayList<Category> categories;
    private ArrayList<Category> categoriesVisible;

    private LayoutInflater inflater;

    private FeedOverviewCallbacks callback;

    public FeedOverviewListAdapter(Activity activity, ArrayList<Category> categories) {
        this.categories = categories;
        this.categoriesVisible = categories;
        this.inflater = activity.getLayoutInflater();

        // Make sure that the interface is implemented in the fragment
        try {
            callback = (FeedOverviewCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement FeedOverviewCallbacks");
        }
    }

    @Override
    public int getGroupCount() {
        return categoriesVisible.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return categoriesVisible.get(groupPosition).getFeedCount(callback.getFeedlyAccount().showUnreadOnly());
    }

    @Override
    public Category getGroup(int groupPosition) {
        return categoriesVisible.get(groupPosition);
    }

    @Override
    public Feed getChild(int groupPosition, int childPosition) {
        return categoriesVisible.get(groupPosition).getFeed(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return categoriesVisible.get(groupPosition).getId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return categoriesVisible.get(groupPosition).getFeed(childPosition).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final Category category = categoriesVisible.get(groupPosition);

        View categoryView = category.getView(inflater, convertView, parent, callback.getFeedlyAccount().showUnreadOnly());
        TextView categoryName = (TextView) categoryView.findViewById(R.id.category_name);

        if (!(category instanceof DefaultCategory)) {
            categoryName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.displayFeedEntryList(category, null);
                }
            });
        }

        return categoryView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return categoriesVisible.get(groupPosition).getFeed(childPosition).getView(inflater, convertView, parent, callback.getFeedlyAccount().showUnreadOnly());
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
        Category category = categoriesVisible.get(groupPosition);
        if (!category.isExpanded()) {
            category.expand();
            callback.getFeedlyAccount().saveCategory(category);
        }

        setBorder(groupPosition, true);
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
        Category category = categoriesVisible.get(groupPosition);
        category.collapse();
        callback.getFeedlyAccount().saveCategory(category);

        setBorder(groupPosition, false);
    }

    private void setBorder(int position, boolean border) {
        if (categoriesVisible.size() > position+1)
            categoriesVisible.get(position+1).setBorder(border);
    }

    @Override
    public void notifyDataSetChanged() {
        hideEmptyCategories();

        super.notifyDataSetChanged();
    }

    private void hideEmptyCategories() {
        categoriesVisible = new ArrayList<>(categories);

        // Remove empty categories
        if (callback.getFeedlyAccount().showUnreadOnly()) {
            for (Category category : categories)
                if (category.getUnreadCount() == 0 && !(category instanceof DefaultCategory))
                    categoriesVisible.remove(category);
        }
        else {
            for (Category category : categories)
                if (category.getEntriesCount() == 0 && !(category instanceof DefaultCategory))
                    categoriesVisible.remove(category);
        }

        // Update borders
        for (Category category : categoriesVisible) {
            if (category.isExpanded())
                setBorder(categoriesVisible.indexOf(category), true);
            else
                setBorder(categoriesVisible.indexOf(category), false);
        }
    }

    // Getter-/Setter
    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    public ArrayList<Category> getCategories() {
        return categoriesVisible;
    }
}
