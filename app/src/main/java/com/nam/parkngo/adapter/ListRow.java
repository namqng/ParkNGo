package com.nam.parkngo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nam.parkngo.R;

import java.util.ArrayList;


/**
 * Created by Nam on 10/16/2014.
 */
public class ListRow extends ArrayAdapter<CarPark>
{
    private String unit;

    private ArrayList<CarPark> list;
    class ViewHolder
    {
        ImageView icon;
        TextView name;
        TextView dist;
    }

    public ListRow(Context context, int rowLayoutId, int txtViewId, ArrayList<CarPark> list)
    {
        super(context, rowLayoutId, txtViewId, list);
        this.list = list;
    }

    public View getView(int pos, View cView, ViewGroup parent)
    {
        ViewHolder holder;
        if (cView == null)
        {   //inflate the GridView Item layout
            LayoutInflater inf = LayoutInflater.from(getContext());
            cView = inf.inflate(R.layout.listitem, parent,false);
            holder = new ViewHolder();

            //init the holder
            holder.icon = (ImageView) cView.findViewById(R.id.parkic);
            holder.name = (TextView) cView.findViewById(R.id.txtVParkName);
            holder.dist = (TextView) cView.findViewById(R.id.txtVDist);
            cView.setTag(holder);
        }
        else
        {   //recycle the inflated view
            holder = (ViewHolder) cView.getTag();
        }
        // get current item info
        CarPark cp = list.get(pos);
        if (cp != null) {
            holder.icon.setImageResource(R.drawable.ic_car);
            holder.name.setText(cp.getFeatureName());
            holder.dist.setText(String.format("%.2f",cp.getDist()) + unit);
        }
        return cView;
    }

     public void setUnit(String unit) {
        this.unit = unit;
    }
}
