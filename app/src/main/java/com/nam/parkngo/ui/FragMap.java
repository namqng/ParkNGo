package com.nam.parkngo.ui;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nam.parkngo.R;
import com.nam.parkngo.adapter.CarPark;
import com.nam.parkngo.adapter.Search;

/**
 * Created by Nam on 10/27/2014.
 */
public class FragMap extends DialogFragment {
    private View rootView;
    private GoogleMap map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragmap, container, false);
        initUI();
        return rootView;
    }

    @Override
    public void onPause()
    {
        MapFragment frag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        if (map != null)
            getFragmentManager().beginTransaction().remove(frag).commit();
        super.onPause();
    }

    private void initUI()
    {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        Bundle b = this.getArguments();
        CarPark cp = new Search(rootView.getContext()).cpConv(b.getString("cp"));
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.addMarker(new MarkerOptions()
                .position(new LatLng(cp.getLatitude(),cp.getLongitude()))
                .title(cp.getFeatureName()));
        map.animateCamera(CameraUpdateFactory
                .newCameraPosition(new CameraPosition
                        .Builder().target(new LatLng(cp.getLatitude(),cp.getLongitude()))
                        .zoom(b.getIntArray("settings")[2])
                        .build()));
    }

}
