package com.example.thewrights.stockwatch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;


public class EditFavorites extends ActionBarActivity {
    ListView favesList;
    Button editFaves;
    Button addFaves;
    TextView symbolIn;
    Button finished;
    Map<String,?> keys;
    SharedPreferences sharedPrefs;
    ArrayList<String> faveList;
    ArrayList<String> deleteList;
    String symbol;
    ArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_favorites);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        faveList = new ArrayList<String>();
        deleteList = new ArrayList<String>();
        keys = sharedPrefs.getAll();
        symbolIn = (TextView)findViewById(R.id.txtSymbol);

        for(Map.Entry<String,?> entry : keys.entrySet())
        {
            faveList.add(entry.getValue().toString());
            /*Log.d("map values",entry.getKey() + ": " +
                    entry.getValue().toString());*/
        }
        favesList = (ListView)findViewById(R.id.lstCurrentFaves);
        reloadData();

        editFaves = (Button)findViewById(R.id.btnDelete);
        editFaves.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SharedPreferences.Editor editor = sharedPrefs.edit();

                for(int i=0; i<deleteList.size(); i++)
                {
                    editor.remove(deleteList.get(i));
                    editor.commit();
                    //EditFavorites.this.recreate();
                    reloadData();
                    boolean keyHidden = hideKeyboard();
                }
            }
        });

        addFaves = (Button)findViewById(R.id.btnAddFaves);
        addFaves.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(symbolIn.length() > 0 && symbolIn.getText().toString() != "")
                {
                    symbol = symbolIn.getText().toString();
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString(symbol, symbol);
                    editor.commit();
                    //reloadData();
                    //EditFavorites.this.recreate();
                    reloadData();
                    boolean keyHidden = hideKeyboard();
                }
                else
                {
                    AlertDialog.Builder alert = new AlertDialog.Builder(EditFavorites.this);
                    alert.setMessage("Please enter a value").setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    }).create();
                    alert.show();
                }

            }
        });
        finished = (Button)findViewById(R.id.btnFinish);
        finished.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        boolean keyHidden = hideKeyboard();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_favorites, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void reloadData(){
        // get new modified random data
        getFaves();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, faveList);
        favesList.setAdapter(adapter);
        favesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                deleteList.add(faveList.get(position));
            }
        });
        EditFavorites.this.adapter.notifyDataSetChanged();
    }

    public void getFaves()
    {
        faveList.clear();
        keys = sharedPrefs.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet())
        {
            faveList.add(entry.getValue().toString());
            /*Log.d("map values",entry.getKey() + ": " +
                    entry.getValue().toString());*/
        }
    }

    public boolean hideKeyboard()
    {
        boolean worked = true;
        try
        {
            InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
            worked = true;
        }
        catch(Exception e)
        {
            worked = false;
        }
        finally
        {
            return worked;
        }
    }
}
