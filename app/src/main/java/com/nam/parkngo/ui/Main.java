package com.nam.parkngo.ui;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SearchView;

import com.nam.parkngo.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Main extends FragmentActivity implements ActionBar.OnNavigationListener
{
    private Bundle b;
    private ActionBar ab;
    private FragList fragR;
    private Fragment currFrag;
    private SearchView sv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initUI();
    }

    private void initUI()
    {

        b = new Bundle();
        b.putIntArray("settings", settings());
        ab = getActionBar();

        ab.setHomeButtonEnabled(false);
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ab.setDisplayShowTitleEnabled(false);
        // Set up the dropdown list navigation in the action bar.
        ab.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(
                        getApplicationContext(),
                        android.R.layout.simple_list_item_1, // android built-in layout
                        android.R.id.text1,  // android built-in id used by layout
                        new String[] {"Last search", "Bookmarks"}),
                this);
        //launch recent tab and load list from file
        int startup = b.getIntArray("settings")[0];
        ab.setSelectedNavigationItem(startup);
        onNavigationItemSelected(startup, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        //attach search config with SearchView on action bar
        SearchManager sm = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        sv = (SearchView) menu.findItem(R.id.action_search).getActionView();
                sv.setSearchableInfo(sm.getSearchableInfo(getComponentName()));
                sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        //launch recent tab and load list from search
                        fragR = null;
                        b.putString("keyword", s);
                        b.putInt("frag", -1);
                        ab.setSelectedNavigationItem(0);
                        onNavigationItemSelected(0,0);
                        sv.onActionViewCollapsed();
                        return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }

        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        sv.onActionViewCollapsed();
        switch (item.getItemId())
        {
            case R.id.action_search:
                return true;
            case R.id.action_settings:
                FragSettings frag = new FragSettings();
                frag.setArguments(b);
                frag.show(getFragmentManager(), "settings");
                return true;
            case R.id.action_nearby:
                fragR = null;
                b.putInt("frag", -2);
                ab.setSelectedNavigationItem(0);
                onNavigationItemSelected(0,0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(int i, long l) {
        if (sv != null) sv.onActionViewCollapsed();
        switch (i)
        {
            case 0:
                if (b.getInt("frag") > 0) b.putInt("frag",0);
                if (fragR == null) fragR = new FragList();
                launch(fragR);
                return true;
            case 1:
                b.putInt("frag",1);
                launch(new FragList());
                return true;
            default:
                break;
        }
        return false;
    }

    private void launch(Fragment frag)
    {
        if (frag.equals(currFrag)) return;
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
            if (frag.getArguments() == null) frag.setArguments(b);
            ft.addToBackStack(null);
            if (currFrag != null) ft.hide(currFrag);
            if (frag.isHidden()) ft.show(frag);
            else ft.add(R.id.container,frag);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
        currFrag = frag;
        Log.d("Main",currFrag.toString());
    }

    private int[] settings()
    {
        final String fname = "settings.cpl";
        int[] val = {0, 0, 10};  //default settings
        Context context = getApplicationContext();
        File f = context.getFileStreamPath(fname);
        if (f.exists()) {
            try {
                InputStream is = context.openFileInput(fname);
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is));
                String[] str = br.readLine().split(",");
                if (str.length >= 3) {
                    for (int i = 0; i < val.length; i++)
                        val[i] = Integer.parseInt(str[i]);
                }
                is.close();
            } catch (IOException e) {
            }
        }
        return val;
    }
}
