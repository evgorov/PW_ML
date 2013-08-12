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

    static class viewHolder{
        public TextView text;
    }

    public InviteFragmentAdapter(Context context, String[] names)
    {
        super(context, R.layout.invite_simple_item, names);
        this.context = context;
        this.names = names;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent)
    {
        View rowView = convertView;
        if(rowView==null){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.invite_simple_item, null);
            viewHolder viewHolder = new viewHolder();
            viewHolder.text=(TextView) rowView.findViewById(R.id.invite_item_name_textview);
            rowView.setTag(viewHolder);
        }

        viewHolder holder = (viewHolder) rowView.getTag();
        String s  = names[position];
        holder.text.setText(s);
        return rowView;
    }

}
