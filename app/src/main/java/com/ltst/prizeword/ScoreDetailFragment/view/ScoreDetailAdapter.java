package com.ltst.prizeword.scoredetailfragment.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ltst.prizeword.R;
import com.ltst.prizeword.scoredetailfragment.model.IScoreDataModel;
import com.ltst.prizeword.scoredetailfragment.model.ScoreFriendsData;

import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.lists.SlowSourceAdapter;
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class ScoreDetailAdapter extends SlowSourceAdapter<ScoreDetailAdapter.ViewHolder, ScoreFriendsData, Bitmap>
{
    private final @Nonnull IScoreDataModel mFriendsModel;
    private @Nullable IListenerVoid mRefreshHandler;
    private @Nonnull Context mContext;

    public ScoreDetailAdapter(@Nonnull Context context, @Nonnull IScoreDataModel friendsModel)
    {
        super(context, friendsModel.getSource());
        mContext = context;
        mFriendsModel = friendsModel;
        updateByInternet();
    }

    public void updateByInternet()
    {
        mFriendsModel.updateDataByInternet(new IListenerVoid()
        {
            public void handle()
            {
                if (mRefreshHandler != null)
                    mRefreshHandler.handle();
                setSlowSource(mFriendsModel.getSource());
            }
        });
    }

    public void setRefreshHandler(@Nonnull IListenerVoid handler)
    {
        this.mRefreshHandler = handler;
    }

    @Override
    protected void appendQuickDataToView(@Nonnull ViewHolder viewHolder, @Nonnull ScoreFriendsData data, @Deprecated @Nonnull View view, @Deprecated int position)
    {
        viewHolder.mNameView.setText(data.firstName+" "+data.lastName);
        viewHolder.mScoreTextView.setText("400");
    }

    @Override
    protected void appendSlowDataToView(@Nonnull ViewHolder viewHolder, @Nonnull Bitmap bitmap, @Deprecated @Nonnull View view, @Deprecated int position)
    {
        viewHolder.mUserPic.setImageBitmap(bitmap);
    }

    @Override protected ViewHolder createViewHolderOfView(@Nonnull View view)
    {
        TextView nameView = (TextView) view.findViewById(R.id.score_item_name_textview);
        TextView scoreView = (TextView) view.findViewById(R.id.score_score_item);
        ImageView userPic = (ImageView) view.findViewById(R.id.score_item_image);
        if (nameView == null  ||scoreView == null || userPic == null )
        {
            Log.w("Elements of ListItem was null, but they must not be"); //$NON-NLS-1$
            throw new NullPointerException("Elements of ListItem was null, but they must not be"); //$NON-NLS-1$
        }

        return new ViewHolder(nameView, scoreView, userPic);
    }

    @Override protected int getItemViewResId()
    {
        return R.layout.score_friends_item;
    }


    //=========================================================================
    protected static class ViewHolder
    {
        final @Nonnull TextView mNameView;
        final @Nonnull TextView mScoreTextView;
        final @Nonnull ImageView mUserPic;

        public ViewHolder(@Nonnull TextView mNameView, @Nonnull TextView mScoreTextView, @Nonnull ImageView mUserPic)
        {
            this.mNameView = mNameView;
            this.mScoreTextView = mScoreTextView;
            this.mUserPic = mUserPic;
        }
    }

}
