package com.nam.parkngo.ui;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.nam.parkngo.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Nam on 11/17/2014.
 */
public class FragSettings extends DialogFragment {

    View rootView;
    RadioGroup startup;
    RadioGroup unit;
    SeekBar seekBar;
    Button cache;
    Button bm;
    Button about;
    Button ok;
    Bundle b;
    int[] val;
    Toast t;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.fragsettings, container);
        b = this.getArguments();
        initUI();
        setListener();
        return rootView;
    }

    private void initUI()
    {
        startup = (RadioGroup) rootView.findViewById(R.id.rgStartup);
        unit = (RadioGroup) rootView.findViewById(R.id.rgUnit);
        seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        cache = (Button) rootView.findViewById(R.id.btnClrCache);
        bm = (Button) rootView.findViewById(R.id.btnClrBm);
        about = (Button) rootView.findViewById(R.id.btnAbout);
        ok = (Button) rootView.findViewById(R.id.btnClose);
        val = new int[]{0,0,10};
        t = new Toast(rootView.getContext());
        getDialog().setTitle("Settings");
    }

    private void setListener() {
        startup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int id = startup.getCheckedRadioButtonId();
                if (id == R.id.rbR) val[0] = 0;
                else val[0] = 1;
            }
        });
        unit.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int id = unit.getCheckedRadioButtonId();
                if (id == R.id.rbkm) val[1] = 0;
                else val[1] = 1;
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                val[2] = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        cache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear("recent.cpl");
                cache.setEnabled(false);
                t.makeText(rootView.getContext(), "Cache cleared.", Toast.LENGTH_SHORT);
            }
        });

        bm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear("bookmark.cpl");
                bm.setEnabled(false);
                t.makeText(rootView.getContext(), "Bookmark cleared.", Toast.LENGTH_SHORT);
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                about();
            }
        });


        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
    }
    @Override
    public void onDismiss(DialogInterface dialog)
    {
        if (!val.equals(b.getIntArray("settings"))) save();
        b.putIntArray("settings", val);
    }
    private void clear(String fname)
    {
       File f = rootView.getContext().getFileStreamPath(fname);
       f.delete();
    }

    private void save()
    {

        Log.d("FragSettings", "Saving changes...");
        OutputStream output = null;
        String line = val[0] + ","
                + val[1] + ","
                + val[2];
        try {
            output = rootView.getContext().openFileOutput("settings.cpl", Context.MODE_PRIVATE);
            output.write(line.getBytes());
            output.flush();
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        t.makeText(rootView.getContext(),"Settings saved", Toast.LENGTH_SHORT);
    }

    public void about()
    {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(rootView.getContext());
        dialog.setTitle("Park And Go 1.0");
        dialog.setMessage("Copyright: Quoc Nam NGUYEN\n" +
                "4918304@student.swin.edu.au\n" +
                "------------------------------\n" +
                "Please register online for premium acount at :\n" +
                "www.parkngo.com.au");
        dialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int i) {
                d.cancel();
            }
        });
        dialog.show();
    }
}