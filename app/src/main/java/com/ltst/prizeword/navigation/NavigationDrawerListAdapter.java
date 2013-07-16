package com.ltst.prizeword.navigation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ltst.prizeword.app.NavigationDrawerItem;

import org.omich.velo.handlers.IListenerInt;

import javax.annotation.Nonnull;

public class NavigationDrawerListAdapter extends ArrayAdapter<NavigationDrawerItem>
{
    private int mItemResourceId;
    private int mTextViewResourceId;
    private @Nonnull Context mContext;
    private @Nonnull IListenerInt mItemClickHandler;

    public NavigationDrawerListAdapter(@Nonnull INavigationDrawerActivity<NavigationDrawerItem> drawerActivity)
    {
        super(drawerActivity.getContext(), drawerActivity.getDrawerItemResourceId(), drawerActivity.getDrawerItemTextViewResourceId(), drawerActivity.getNavigationDrawerItems());
        mItemResourceId = drawerActivity.getDrawerItemResourceId();
        mItemClickHandler = drawerActivity.getDrawerItemClickHandler();
        mTextViewResourceId = drawerActivity.getDrawerItemTextViewResourceId();
        mContext = drawerActivity.getContext();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mItemResourceId, parent, false);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(mTextViewResourceId);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        final NavigationDrawerItem item = getItem(position);
        if(item != null)
        {
            if(!item.isHidden())
                holder.textView.setText(item.getTitle());
        }
        convertView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!item.isHidden())
                    mItemClickHandler.handle(position);
            }
        });
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return getView(position, convertView, parent);
    }

    protected static class ViewHolder
    {
        public @Nonnull TextView textView;
    }
}
