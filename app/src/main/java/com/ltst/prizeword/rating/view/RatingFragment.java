package com.ltst.prizeword.rating.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.ltst.prizeword.R;
import com.ltst.prizeword.app.IBcConnectorOwner;

import org.omich.velo.bcops.client.IBcConnector;

import javax.annotation.Nonnull;

public class RatingFragment extends SherlockFragment implements View.OnClickListener
{
    private @Nonnull String LOG_TAG = "InviteFriends";
    public static final @Nonnull
    String FRAGMENT_ID = "com.ltst.prizeword.rating.view.InviteFriendsFragment";
    public static final @Nonnull String FRAGMENT_CLASSNAME = RatingFragment.class.getName();




    private @Nonnull android.content.Context mContext;
    private @Nonnull IBcConnector mBcConnector;

    private @Nonnull ImageView mImagePlace;
    private @Nonnull ImageView mItemImage;
    private @Nonnull TextView mSurname;
    private @Nonnull TextView mName;
    private @Nonnull TextView mScore;
    private @Nonnull TextView mCrossEnd;
    private @Nonnull ListView mRatingLisView;
    private @Nonnull Button mMenuBtn;
    private @Nonnull ImageView mHeaderImage;
    private @Nonnull ImageView mFooterImage;
    private @Nonnull RatingAdapter mRatingAdapter;



    @Override
    public void onAttach(Activity activity)
    {
        Log.i(LOG_TAG, "RatingFragment.onAttach()"); //$NON-NLS-1$

        mContext = (Context) activity;
        mBcConnector = ((IBcConnectorOwner) getActivity()).getBcConnector();
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(LOG_TAG, "RatingFragment.onCreateView()"); //$NON-NLS-1$
        View v = inflater.inflate(R.layout.rating_fragment_layout, container, false);
        mMenuBtn = (Button) v.findViewById(R.id.header_menu_btn);
        mRatingLisView = (ListView)v.findViewById(R.id.rating_listview);
        mHeaderImage =  new ImageView(mContext);
        mFooterImage = new ImageView(mContext);
        mHeaderImage.setBackgroundResource(R.drawable.rating_header);
        mFooterImage.setBackgroundResource(R.drawable.rating_footer);
        String[] names = new String[] {"Николай","Петр","Август","Миротворец","Кирилл","Августин",
                "Солнечный","Город","Город","Город","Город","Город","Николай","Петр","Август","Миротворец","Кирилл","Августин"};
       /* mImagePlace = (ImageView)v.findViewById(R.id.rating_place_image);
        mItemImage = (ImageView)v.findViewById(R.id.rating_item_image);
        mSurname = (TextView)v.findViewById(R.id.rating_item_surname_textview);*/
        mName = (TextView)v.findViewById(R.id.rating_item_name_textview);
        mRatingAdapter = new RatingAdapter(mContext,R.layout.rating_simple_item,R.id.rating_item_name_textview,names);
        return v;

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.i(LOG_TAG, "InviteFriendsFragment.onActivityCreated()"); //$NON-NLS-1$
        mMenuBtn.setOnClickListener(this);
        mRatingLisView.setDivider(null);
        mRatingLisView.addHeaderView(mHeaderImage);
        mRatingLisView.addFooterView(mFooterImage);
        mRatingLisView.setAdapter(mRatingAdapter);

    }
    @Override public void onClick(View view)
    {

    }
}
