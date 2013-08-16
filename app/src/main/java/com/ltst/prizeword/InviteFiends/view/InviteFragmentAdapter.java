package com.ltst.prizeword.InviteFiends.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ltst.prizeword.R;
import com.ltst.prizeword.InviteFiends.model.IInviteFriendsDataModel;
import com.ltst.prizeword.InviteFiends.model.InviteFriendsData;

import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.lists.SlowSourceAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class InviteFragmentAdapter extends SlowSourceAdapter<InviteFragmentAdapter.ViewHolder, InviteFriendsData, Bitmap>
{
    private final @Nonnull IInviteFriendsDataModel mModel;
    private @Nullable IListenerVoid mRefreshHandler;


    public InviteFragmentAdapter(@Nonnull Context context, @Nonnull IInviteFriendsDataModel model)
    {
        super(context, model.getSource());
        mModel = model;
        updateByInternet();
    }

    public void updateByInternet()
    {
        mModel.updateDataByInternet(new IListenerVoid()
        {
            public void handle()
            {
                if (mRefreshHandler != null)
                    mRefreshHandler.handle();
                setSlowSource(mModel.getSource());
            }
        });


    }



    public void setRefreshHandler(@Nonnull IListenerVoid handler)
    {
        this.mRefreshHandler = handler;
    }

    //==== SlowSourceAdapter implementation ===================================

    @Override protected @Nonnull ViewHolder createViewHolderOfView(@Nonnull View view)
    {
        TextView nameView = (TextView) view.findViewById(R.id.invite_item_name_textview);
        TextView surnameView = (TextView) view.findViewById(R.id.invite_item_surname_textview);
        ImageView imageView = (ImageView) view.findViewById(R.id.invite_item_ava);
        Button inviteBtn = (Button) view.findViewById(R.id.invite_add_btn);
        if (nameView == null || surnameView == null || imageView == null || inviteBtn == null)
        {
            //Log.w(getClass(), "Elements titleView or imageView or dateView of NewsList Item was null, but they must not be"); //$NON-NLS-1$
            throw new NullPointerException("Elements titleView or imageView or dateView of NewsList Item was null, but they must not be"); //$NON-NLS-1$
        }
        return new ViewHolder(nameView, surnameView, imageView, inviteBtn);
    }


    @Override
    protected void appendQuickDataToView(@Nonnull ViewHolder viewHolder, @Nonnull InviteFriendsData quick, @Deprecated @Nonnull View view, @Deprecated int position)
    {
        final String id = quick.id;
        final String provider = quick.providerName;

            viewHolder.nameView.setText(quick.firstName);
            viewHolder.surnameView.setText(quick.lastName);
        if(quick.status.equals("already_registered")){
                viewHolder.inviteBtn.setEnabled(false);
            viewHolder.inviteBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override public void onClick(View v)
                {
                    switch(v.getId()){
                        case R.id.invite_add_btn:
                            mModel.sendInviteFriends(id,provider,null);
                            break;
                    }
                }
            });
            }
        else if (quick.status.equals("uninvited")||quick.status.equals("invite_sent")){
            viewHolder.inviteBtn.setEnabled(true);
        }
    }

    @Override
    protected void appendSlowDataToView(@Nonnull ViewHolder viewHolder, @Nonnull Bitmap slow, @Deprecated @Nonnull View view, @Deprecated int position)
    {
            viewHolder.imageView.setImageBitmap(slow);
        }

    @Override protected int getItemViewResId()
    {
        return R.layout.invite_simple_item;
    }


    //=========================================================================
    protected static class ViewHolder
    {
        final @Nonnull TextView nameView;
        final @Nonnull TextView surnameView;
        final @Nonnull ImageView imageView;
        final @Nonnull Button inviteBtn;

        private ViewHolder(@Nonnull TextView nameView, @Nonnull TextView surnameView,
                           @Nonnull ImageView imageView, @Nonnull Button inviteBtn)
        {
            this.nameView = nameView;
            this.surnameView = surnameView;
            this.imageView = imageView;
            this.inviteBtn = inviteBtn;
        }
    }

}
