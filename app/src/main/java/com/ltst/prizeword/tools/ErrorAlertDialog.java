package com.ltst.prizeword.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;

import com.ltst.prizeword.R;

import javax.annotation.Nonnull;

/**
 * Created by cosic on 19.07.13.
 */


public class ErrorAlertDialog extends AlertDialog.Builder {

    private @Nonnull Context mContext;
    private @Nonnull String mMessage;

    public ErrorAlertDialog(@Nonnull Context context) {
        super(context);
        mContext = context;
    }

    public void setMessage(@Nonnull String msg){
        mMessage = msg;
    }

    public void setMessage(@Nonnull Integer idMsg){
        Resources res = mContext.getResources();
        mMessage = res.getString(idMsg);
    }

    @Nonnull
    @Override
    public AlertDialog create() {
        Resources res = mContext.getResources();
        this.setTitle(res.getString(R.string.error));
        this.setMessage(mMessage);
        this.setCancelable(false);
        this.setNegativeButton(res.getString(R.string.ok_bnt_title), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        return super.create();
    }
}