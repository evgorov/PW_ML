package com.ltst.przwrd.rating.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ltst.przwrd.R;
import com.ltst.przwrd.rating.model.IUsersListModel;
import com.ltst.przwrd.rating.model.UsersList;

import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.lists.SlowSourceAdapter;
import org.omich.velo.log.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RatingAdapter extends SlowSourceAdapter<RatingAdapter.ViewHolder, UsersList.User, Bitmap>
{
    private @Nonnull Context mContext;
    private @Nonnull IUsersListModel mModel;
    private @Nullable IListenerVoid mRefreshHandler;

    public RatingAdapter(@Nonnull Context context, @Nonnull IUsersListModel model)
    {
        super(context, model.getSource());
        mContext = context;
        mModel = model;
    }


    public void update()
    {
        mModel.updateDataByInternet(new IListenerVoid()
        {
            @Override
            public void handle()
            {
                setSlowSource(mModel.getSource());
                if (mRefreshHandler != null)
                    mRefreshHandler.handle();
            }
        });
    }

    public void setRefreshHandler(@Nonnull IListenerVoid refreshHandler)
    {
        mRefreshHandler = refreshHandler;
    }

    //==== SlowSourceAdapter implementation ===================================
    @Override
    protected void appendQuickDataToView(@Nonnull ViewHolder viewHolder, @Nonnull UsersList.User user, @Deprecated @Nonnull View view, @Deprecated int position)
    {
        if (position == 0)
        {
            viewHolder.mCellBorderLayoutView.setBackgroundResource(R.drawable.rating_cell_border_first);
        } else if (position == getCount() - 1)
        {
            viewHolder.mCellBorderLayoutView.setBackgroundResource(R.drawable.rating_cell_border_last);
        } else
            viewHolder.mCellBorderLayoutView.setBackgroundResource(R.drawable.rating_cell_border);
        int positionBgRes = 0;
        switch (user.position)
        {
            case 1:
                positionBgRes = R.drawable.rating_cell_first_place;
                break;
            case 2:
                positionBgRes = R.drawable.rating_cell_second_place;
                break;
            case 3:
                positionBgRes = R.drawable.rating_cell_third_place;
                break;
            default:
                positionBgRes = R.drawable.rating_cell_other_place_bg;
                break;
        }

        viewHolder.mNameView.setText(user.name);
        viewHolder.mSurnameView.setText(user.surname);
        viewHolder.mSolvedTextView.setText(String.valueOf(user.solved));
        viewHolder.mScoreTextView.setText(String.valueOf(user.monthScore));
        if (user.position > 3)
        {
            viewHolder.mPositionBgView.setBackgroundResource(positionBgRes);
            viewHolder.mPositionTextView.setText(String.valueOf(user.position));
            viewHolder.mPositionTextView.setVisibility(View.VISIBLE);
            viewHolder.mDynamicsPic.setVisibility(View.VISIBLE);

        } else if (user.position > 0)
        {
            viewHolder.mDynamicsPic.setVisibility(View.GONE);
            viewHolder.mPositionBgView.setBackgroundResource(positionBgRes);
            viewHolder.mPositionTextView.setVisibility(View.GONE);
        }

        if (user.dynamics > 0)
        {
            viewHolder.mDynamicsPic.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_cell_move_up));
        } else if (user.dynamics < 0)
        {
            viewHolder.mDynamicsPic.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_cell_move_down));
        } else if (user.dynamics == 0)
        {
            viewHolder.mDynamicsPic.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_cell_move_none));
        }

        viewHolder.mCellLayoutView.setBackgroundResource(user.me ? R.drawable.rating_cell_me_bg : R.drawable.rating_cell_bg);
        viewHolder.mUserPic.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_userpic_none));
    }

    @Override
    protected void appendSlowDataToView(@Nonnull ViewHolder viewHolder, @Nonnull Bitmap
            bitmap, @Deprecated @Nonnull View view, @Deprecated int position)
    {
        viewHolder.mUserPic.setImageBitmap(bitmap);
    }

    @Nonnull
    @Override
    protected ViewHolder createViewHolderOfView(@Nonnull View view)
    {
        TextView nameView = (TextView) view.findViewById(R.id.rating_item_name_textview);
        TextView surnameView = (TextView) view.findViewById(R.id.rating_item_surname_textview);
        TextView solvedView = (TextView) view.findViewById(R.id.rating_item_solved);
        TextView scoreView = (TextView) view.findViewById(R.id.rating_item_score);
        TextView positionView = (TextView) view.findViewById(R.id.rating_item_position_textview);
        ImageView userPic = (ImageView) view.findViewById(R.id.rating_item_image);
        ImageView dynamicsPic = (ImageView) view.findViewById(R.id.rating_item_dynamics);
        View positionBgView = view.findViewById(R.id.rating_position_background_layout);
        View cellView = view.findViewById(R.id.rating_cell_layout);
        View cellBorderView = view.findViewById(R.id.rating_cell_border);
        if (positionBgView == null || nameView == null || surnameView == null || solvedView == null ||
                scoreView == null || positionView == null || userPic == null || dynamicsPic == null
                || cellView == null || cellBorderView == null)
        {
            Log.w("Elements of ListItem was null, but they must not be"); //$NON-NLS-1$
            throw new NullPointerException("Elements of ListItem was null, but they must not be"); //$NON-NLS-1$
        }

        return new ViewHolder(nameView, surnameView, solvedView, scoreView, positionView, userPic, dynamicsPic, positionBgView, cellView, cellBorderView);
    }

    @Override
    protected int getItemViewResId()
    {
        return R.layout.rating_simple_item;
    }

    protected static class ViewHolder
    {
        final @Nonnull TextView mNameView;
        final @Nonnull TextView mSurnameView;
        final @Nonnull TextView mSolvedTextView;
        final @Nonnull TextView mScoreTextView;
        final @Nonnull TextView mPositionTextView;
        final @Nonnull ImageView mUserPic;
        final @Nonnull ImageView mDynamicsPic;
        final @Nonnull View mPositionBgView;
        final @Nonnull View mCellLayoutView;
        final @Nonnull View mCellBorderLayoutView;

        public ViewHolder(@Nonnull TextView nameView, @Nonnull TextView surnameView, @Nonnull TextView solvedTextView,
                          @Nonnull TextView scoreTextView, @Nonnull TextView positionTextView, @Nonnull ImageView userPic,
                          @Nonnull ImageView dynamicsPic, @Nonnull View positionBgView, @Nonnull View cellLayoutView, @Nonnull View cellBorderLayoutView)
        {
            mNameView = nameView;
            mSurnameView = surnameView;
            mSolvedTextView = solvedTextView;
            mScoreTextView = scoreTextView;
            mPositionTextView = positionTextView;
            mUserPic = userPic;
            mDynamicsPic = dynamicsPic;
            mPositionBgView = positionBgView;
            mCellLayoutView = cellLayoutView;
            mCellBorderLayoutView = cellBorderLayoutView;
        }
    }

}