package com.nam.parkngo.ui;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ShareActionProvider;

import com.nam.parkngo.R;
import com.nam.parkngo.adapter.CarPark;
import com.nam.parkngo.adapter.ListRow;
import com.nam.parkngo.adapter.Search;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by Nam on 11/7/2014.
 * This class is commonly used for Recent s, Search Result and Bookmarks
 * All have the same layout and display method, only different list.
 */
public class FragList extends ListFragment
{
    private ArrayList<CarPark> cps;
    private ListRow adapter;
    private Search s;
    private Bundle b;
    private TextView result;
    private View rootView;
    private int fragId;
    private boolean holdOn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.fraglist, container, false);
        b = this.getArguments();
        fragId = b.getInt("frag");
        result = (TextView) rootView.findViewById(R.id.txtVResultInfo);
        s = new Search(rootView.getContext());
        s.setUnit(b.getIntArray("settings")[1]);
        cps = new ArrayList<CarPark>();
        new Load(0).execute();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        b.putBoolean("active", true);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        b.putBoolean("active",false);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if (fragId < 0) saveRecent();
    }

    @Override
    public void onListItemClick(ListView l, View v, int i, long id)
    {
        super.onListItemClick(l, v, i, id);
        final CarPark cp = cps.get(i);
        Load load = new Load(0){
            @Override
            protected ArrayList<CarPark> doInBackground(Void... voids)
            {
                if (!cp.isDetailed()) s.searchDetails(cp);
                return null;
            }

            @Override
            protected void onPostExecute(ArrayList<CarPark> list) {
                super.onPostExecute(list);
                if (b.getBoolean("active")) {
                    b.putString("cp", cp.toLongString());
                    FragDetails details = new FragDetails();
                    details.setArguments(b);
                    details.show(getFragmentManager(), "Details");
                }
            }
        };
        load.execute();
    }

    @Override
    public void  onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo info)
    {
        super.onCreateContextMenu(menu, v, info);
        if (fragId < 1) menu.add(Menu.NONE,0,0,"Add to bookmarks");
        menu.add(Menu.NONE,1,1,"View details");
        menu.add(Menu.NONE,2,2,"View map");
        menu.add(Menu.NONE,3,3,"Share");

    }
    @Override
    public boolean onContextItemSelected(final MenuItem item)
    {   AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final CarPark cp = cps.get(info.position);
        Load load;
        switch (item.getItemId())
        {
            case 0: //add to bookmarks
                load = new Load(0){
                    @Override
                    protected ArrayList<CarPark> doInBackground(Void... voids)
                    {
                        if (!cp.isDetailed()) s.searchDetails(cp);
                        return null;
                    }
                    @Override
                    protected void onPostExecute(ArrayList<CarPark> list) {
                        super.onPostExecute(list);
                        s.saveBookmark(cp);
                        Toast.makeText(rootView.getContext(),"New bookmark saved.",Toast.LENGTH_LONG).show();
                    }
                };
                load.execute();
                return true;
            case 1: //view details
                onListItemClick(getListView(), rootView, info.position, info.id);
                return true;
            case 2: //view map
                Toast.makeText(rootView.getContext(),"Loading map, please wait...", Toast.LENGTH_LONG).show();
                b.putString("cp", cp.toString());
                FragMap map = new FragMap();
                map.setArguments(b);
                map.show(getFragmentManager(), "Map");
                return true;
            case 3: //share
                load = new Load(0){
                    @Override
                    protected ArrayList<CarPark> doInBackground(Void... voids)
                    {
                        if (!cp.isDetailed()) s.searchDetails(cp);
                        return null;
                    }
                    @Override
                    protected void onPostExecute(ArrayList<CarPark> list) {
                        super.onPostExecute(list);
                        s.share(cp);
                    }
                };
                load.execute();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    private void saveRecent()
    {
        Log.d("FragList", "Saving recent list...");
        OutputStream output = null;
        try {
            output = rootView.getContext().openFileOutput("recent.cpl", Context.MODE_PRIVATE);
            for (CarPark cp : cps)
            {
                output.write((cp.toString() + "\n").getBytes());
            }
            output.flush();
            output.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initUI()
    {
        String[] unit = {" km ", " miles "};
        registerForContextMenu(getListView());
        adapter = new ListRow(rootView.getContext(), R.layout.listitem, R.id.txtVParkName, cps);
        adapter.setUnit(unit[b.getIntArray("settings")[1]]);
        setListAdapter(adapter);
        registerForContextMenu(getListView());
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                final int lastItem = firstVisibleItem + visibleItemCount;
                if (s.hasNext() && lastItem == totalItemCount && !holdOn) {
                    holdOn = true;
                    new Load(2000).execute();
                }
            }
        });
    }

    class Load extends AsyncTask<Void, Void, ArrayList<CarPark>>
    {
        private ProgressDialog dialog;
        private long delayMillis;
        private String msg;

        public Load(long delayMillis)
        {
            dialog = new ProgressDialog(getActivity());
            this.delayMillis = delayMillis;
            msg = (String) result.getText();
        }
        @Override
        protected ArrayList<CarPark> doInBackground(Void... voids) {
            if (delayMillis > 0) return s.getResults();
            switch (fragId)
            {
                case 0: //recent
                    msg = "Last search results:";
                    return s.getListFromFile("recent.cpl");
                case 1: //bookmark
                    msg = "Bookmark list:";
                    return s.getListFromFile("bookmark.cpl");
                case -1: //new search
                    msg = "Search results for '" + b.getString("keyword") + "':";
                    return s.search(b.getString("keyword"));
                case -2:
                    msg = "Car parks nearby:";
                    return s.getNearby();
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            dialog.setMessage("Loading...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected void onPostExecute(ArrayList<CarPark> list) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                }
            },delayMillis);
            result.setText(msg);
            holdOn = false;
            if (list != null) {
                cps.addAll(list);
                initUI();
            }
        }
    }
}
