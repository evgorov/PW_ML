package com.ltst.prizeword.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.widget.LinearLayout;

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

    public void setMessageResource(int idMsg){
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

    public static void showDialog(@Nonnull Context context, int msgResId)
    {
        ErrorAlertDialog builder = new ErrorAlertDialog(context);
        builder.setMessage(msgResId);
        AlertDialog alert = builder.create();
        Resources res = alert.getContext().getResources();
        alert.getWindow().setLayout((int) res.getDimension(R.dimen.error_dialog_width),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        alert.show();
    }

    public static void showDialog(@Nonnull Context context, @Nonnull String msg)
    {
        ErrorAlertDialog builder = new ErrorAlertDialog(context);
        builder.setMessage(msg);
        AlertDialog alert = builder.create();
        Resources res = alert.getContext().getResources();
        alert.getWindow().setLayout((int) res.getDimension(R.dimen.error_dialog_width),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        alert.show();
    }
}