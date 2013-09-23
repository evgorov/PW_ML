package com.ltst.prizeword.invitefriends.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ltst.prizeword.R;
import com.ltst.prizeword.invitefriends.model.IInviteFriendsDataModel;
import com.ltst.prizeword.invitefriends.model.InviteFriendsData;
import com.ltst.prizeword.navigation.IFragmentsHolderActivity;
import com.ltst.prizeword.sounds.SoundsWork;

import org.omich.velo.handlers.IListenerVoid;
import org.omich.velo.lists.SlowSourceAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class InviteFragmentAdapter extends SlowSourceAdapter<InviteFragmentAdapter.ViewHolder, InviteFriendsData, Bitmap>
{
    private final @Nonnull IInviteFriendsDataModel mModel;
    private @Nullable IListenerVoid mRefreshHandler;
    private @Nonnull Context mContext;
    private boolean mFbSwitch;
    private boolean mVkSwitch;

    public InviteFragmentAdapter(@Nonnull Context context, @Nonnull IInviteFriendsDataModel model, boolean fbSwitch, boolean vkSwitch)
    {
        super(context, model.getSource());
        mContext = context;
        mModel = model;
        this.mFbSwitch = fbSwitch;
        this.mVkSwitch = vkSwitch;
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

    @Override
    protected
    @Nonnull ViewHolder createViewHolderOfView(@Nonnull View view)
    {
        TextView nameView = (TextView) view.findViewById(R.id.invite_item_name_textview);
        TextView surnameView = (TextView) view.findViewById(R.id.invite_item_surname_textview);
        ImageView imageView = (ImageView) view.findViewById(R.id.invite_item_ava);
        Button inviteBtn = (Button) view.findViewById(R.id.invite_add_btn);
        View layout = view.findViewById(R.id.invite_item_layout);
        if (nameView == null || surnameView == null || imageView == null || inviteBtn == null || layout == null)
        {
            throw new NullPointerException("Elements nameView, surnameView or imageView was null, but they must not be"); //$NON-NLS-1$
        }
        return new ViewHolder(nameView, surnameView, imageView, inviteBtn, layout);
    }


    @Override
    protected void appendQuickDataToView(@Nonnull ViewHolder viewHolder, @Nonnull InviteFriendsData quick, @Deprecated @Nonnull View view, @Deprecated int position)
    {
        final String id = quick.id;
        final String provider = quick.providerName;
        if (provider.equals(InviteFriendsData.NO_PROVIDER))
        {
            if (mVkSwitch)
                viewHolder.layout.setBackgroundResource(R.drawable.invite_fb_header);
            else if (!mVkSwitch)
                viewHolder.layout.setBackgroundResource(R.drawable.invite_fb_header_only);
            else if(!mFbSwitch)
                viewHolder.layout.setVisibility(View.GONE);
            viewHolder.imageView.setVisibility(View.GONE);
            viewHolder.inviteBtn.setVisibility(View.GONE);
            viewHolder.nameView.setVisibility(View.GONE);
            viewHolder.surnameView.setVisibility(View.GONE);
        }
        else
        {
            viewHolder.layout.setBackgroundResource(R.drawable.invite_item_bg);
            viewHolder.imageView.setVisibility(View.VISIBLE);
            viewHolder.inviteBtn.setVisibility(View.VISIBLE);
            viewHolder.nameView.setVisibility(View.VISIBLE);
            viewHolder.surnameView.setVisibility(View.VISIBLE);
        }

        viewHolder.nameView.setText(quick.firstName);
        viewHolder.surnameView.setText(quick.lastName);
        if (quick.status.equals("already_registered"))
        {
            viewHolder.inviteBtn.setEnabled(false);
            viewHolder.inviteBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    SoundsWork.interfaceBtnMusic(mContext);
                    switch (v.getId())
                    {
                        case R.id.invite_add_btn:
                            invite(id,provider,null);
                            break;
                    }
                }
            });
        } else if (quick.status.equals("uninvited") || quick.status.equals("invite_sent"))
        {
            viewHolder.inviteBtn.setEnabled(true);
        }
        viewHolder.imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_userpic_none));
    }

    @Override
    protected void appendSlowDataToView(@Nonnull ViewHolder viewHolder, @Nonnull Bitmap slow, @Deprecated @Nonnull View view, @Deprecated int position)
    {
        viewHolder.imageView.setImageBitmap(slow);
    }

    @Override
    protected int getItemViewResId()
    {
        return R.layout.invite_simple_item;
    }

    public void invite(@Nonnull String id,@Nonnull String provider, @Nullable IListenerVoid handler)
    {
        mModel.sendInviteFriends(id, provider, handler);
    }


    //=========================================================================
    protected static class ViewHolder
    {
        final @Nonnull TextView nameView;
        final @Nonnull TextView surnameView;
        final @Nonnull ImageView imageView;
        final @Nonnull Button inviteBtn;
        final @Nonnull View layout;

        private ViewHolder(@Nonnull TextView nameView, @Nonnull TextView surnameView,
                           @Nonnull ImageView imageView, @Nonnull Button inviteBtn, @Nonnull View layout)
        {
            this.nameView = nameView;
            this.surnameView = surnameView;
            this.imageView = imageView;
            this.inviteBtn = inviteBtn;
            this.layout = layout;
        }
    }

}
