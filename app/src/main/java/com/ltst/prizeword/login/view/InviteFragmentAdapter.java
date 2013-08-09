package com.ltst.prizeword.login.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ltst.prizeword.R;

public class InviteFragmentAdapter extends ArrayAdapter<String>
{
    private Context context;
    private String[] names;


    public InviteFragmentAdapter(Context context, String[] names)
    {
        super(context, R.layout.invite_simple_item, names);
        this.context = context;
        this.names = names;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.invite_simple_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.invite_item_name_textview);
        textView.setText(names[position]);

        return rowView;
    }

}
