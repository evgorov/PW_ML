package org.omich.velo.lists;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.omich.velo.handlers.IListenerInt;
import org.omich.velo.lists.ISlowSource.Item;

import android.content.Context;
import android.view.View;
import android.widget.SectionIndexer;

public abstract class GroupedSlowSourceAdapter<ViewHolder, Quick, Slow, GroupHeader> extends SlowAdapter<ViewHolder> implements SectionIndexer
{

	private @Nonnull ISlowSource<Quick, Slow> mSource;
	
	public static final int TYPE_ITEM = 0;
    public static final int TYPE_GROUP_HEADER = 1;
    private static final int TYPE_MAX_COUNT = 2;

	public GroupedSlowSourceAdapter (@Nonnull Context context,
			@Nonnull ISlowSource<Quick, Slow> slowSource)
	{
		super(context);
		mSource = slowSource;
	}
	
	//==== protected interface ===============================================
	
	abstract protected @Nullable Object[] getGroups();
	abstract protected int getGroupForPosition(int position);
	abstract protected int getPositionForGroup(int group);
	abstract protected boolean isGroupHeader(int position);
	abstract protected int getCountWithGroups();
	
	abstract protected @Nullable GroupHeader getGroupHeader(int position);
	abstract protected @Nullable Object getGroupItem(int position);
	abstract protected int getGroupHeaderId(int position);
	abstract protected int getGroupItemId(int position);
	
	abstract protected int getSourceItemPosition(int position);
	
	abstract protected void appendQuickDataToItem(@Nonnull ViewHolder viewHolder,
			@Nonnull Quick quick, @Nonnull View view, int position);
	abstract protected void appendSlowDataToItem(@Nonnull ViewHolder viewHolder,
			@Nonnull Slow slow, @Nonnull View view, int position);
	
	abstract protected void appendDataToHeader(@Nonnull ViewHolder viewHolder,
			@Nonnull GroupHeader header, @Nonnull View view, int position);

	
	final protected void setSlowSource (@Nonnull ISlowSource<Quick, Slow> source)
	{
		mSource = source;
		notifyDataSetChanged();
	}
 	
	//==== SlowAdapter =======================================================
	@Override
	final protected void fillViewWithoutSlowData (@Nonnull ViewHolder viewHolder, 
			@Deprecated @Nonnull View view,
			int position)
    {
		if(!isGroupHeader(position))
		{
			int sourceItemPosition = getSourceItemPosition(position);
			Item<Quick, Slow> item = mSource.getItem(sourceItemPosition);
			appendQuickDataToItem(viewHolder, item.quick, view, position);
			Slow slow = item.slow;
			if(slow == null)
			{
				mSource.startLoadSlowData(position, mpOnSlowLoadedListener);
			}
			else
			{
				appendSlowDataToItem(viewHolder, slow, view, position);
			}
		}
		else
		{
			GroupHeader header = getGroupHeader(position);
			if(header != null)
				appendDataToHeader(viewHolder, header, view, position);
		}
		
    }
	
	@Override
	final protected void updateViewBySlowData (@Nonnull ViewHolder viewHolder, 
			@Deprecated @Nonnull View view,
			int position)
	{
		if(!isGroupHeader(position))
		{
			int sourceItemPosition = getSourceItemPosition(position);
			Item<Quick, Slow> item = mSource.getItem(sourceItemPosition);
			Slow slow = item.slow;
			if(slow != null)
			{
				appendSlowDataToItem(viewHolder, slow, view, position);
			}
		}
	}

	@Override
	final protected void releaseSlowData (int position)
	{
		mSource.releaseSlowData(position);
	}
	
	//==== SectionIndexer =====================================================
	
	@Override
	public Object[] getSections()
	{
		return getGroups();
	}

	@Override
	public int getPositionForSection(int section)
	{
		return getPositionForGroup(section);
	}

	@Override
	public int getSectionForPosition(int position)
	{
		return getGroupForPosition(position);
	}
	
	@Override
    public int getItemViewType(int position) 
	{
		return (isGroupHeader(position)) ? TYPE_GROUP_HEADER : TYPE_ITEM;
    }

	@Override
    public int getViewTypeCount() 
	{
        return TYPE_MAX_COUNT;
    }
	
	
	//==== BaseAdapter ========================================================
	@Override
	public int getCount () 
	{
		return getCountWithGroups();
	}

	@Override
	public long getItemId (int position) 
	{
		if(isGroupHeader(position))
			return getGroupHeaderId(position);
		return getGroupItemId(position);
	}
	
	public Object getItem (int position)
	{
		if(isGroupHeader(position))
			return getGroupHeader(position);
		return getGroupItem(position);
	}
	
	private final @Nonnull IListenerInt mpOnSlowLoadedListener = new IListenerInt()
	{
		public void handle (int position){onSlowDataLoaded(position);}
	};
	
	
}
