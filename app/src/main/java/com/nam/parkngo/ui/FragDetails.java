package com.nam.parkngo.ui;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nam.parkngo.R;
import com.nam.parkngo.adapter.CarPark;
import com.nam.parkngo.adapter.Search;

/**
 * Created by Nam on 11/17/2014.
 */
public class FragDetails extends DialogFragment {

    View rootView;
    ImageView img;
    TextView name;
    TextView dist;
    TextView addr;
    TextView phone;
    TextView time;
    TextView web;
    Button bm;
    Button map;
    Button share;
    Button ok;
    Bundle b;
    CarPark cp;
    Search s;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.fragdetails, container);
        b = this.getArguments();
        s = new Search(rootView.getContext());
        initUI();
        setDisplay();
        setListener();
        return rootView;
    }

    private void initUI()
    {
        img = (ImageView) rootView.findViewById(R.id.imgV);
        name = (TextView) rootView.findViewById(R.id.txtVName);
        dist = (TextView) rootView.findViewById(R.id.txtVDist);
        addr = (TextView) rootView.findViewById(R.id.txtVAddr);
        phone = (TextView) rootView.findViewById(R.id.txtVPhone);
        time = (TextView) rootView.findViewById(R.id.txtVTime);
        web = (TextView) rootView.findViewById(R.id.txtVWeb);
        bm = (Button) rootView.findViewById(R.id.btnBm);
        map = (Button) rootView.findViewById(R.id.btnMap);
        share = (Button) rootView.findViewById(R.id.btnS);
        ok = (Button) rootView.findViewById(R.id.btnOK);

        getDialog().setTitle("Car Park Details");
    }

    private void setListener()
    {
        bm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                s.saveBookmark(cp);
                bm.setEnabled(false);
                Toast.makeText(rootView.getContext(),"New bookmark saved.", Toast.LENGTH_SHORT).show();
            }
        });

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(rootView.getContext(),"Loading map, please wait...", Toast.LENGTH_LONG).show();
                FragMap map = new FragMap();
                map.setArguments(b);
                map.show(getFragmentManager(), "Details");
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                s.share(cp);
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
    }

    private void setDisplay()
    {
        final String[] unit = {" km "," miles "};
        cp = s.cpConv(b.getString("cp"));

        img.setImageResource(R.drawable.ic_car);
        name.setText(cp.getFeatureName());
        addr.setText(cp.getAddress());
        phone.setText(cp.getPhone().replace("|","\n"));
        time.setText(cp.getTime().replace("|","\n"));
        Log.d("FragDetails",cp.getTime());
        web.setText(cp.getWebsite());
        dist.setText(String.format("%.2f",cp.getDist()) + unit[b.getIntArray("settings")[1]] + "from here");
    }
}
