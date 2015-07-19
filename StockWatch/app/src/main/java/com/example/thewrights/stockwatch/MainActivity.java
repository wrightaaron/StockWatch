package com.example.thewrights.stockwatch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends ActionBarActivity {

   private final String NAMESPACE = "http://www.webserviceX.NET/";

    private final String URL = "http://www.webservicex.net/stockquote.asmx";

    private final String SOAP_ACTION = "http://www.webserviceX.NET/GetQuote";

    private final String METHOD_NAME = "GetQuote";

   /* private final String NAMESPACE = "http://www.w3schools.com/webservices/";

    private final String URL = "http://www.w3schools.com/webservices/tempconvert.asmx";

    private final String SOAP_ACTION = "http://www.w3schools.com/webservices/CelsiusToFahrenheit";

    private final String METHOD_NAME = "CelsiusToFahrenheit";*/

    private String TAG = "CIS295";
    private static String symbol;
    private static String stockInfo;

    TextView title;
    EditText symbolIn;
    Button btnStock;
    ListView result;
    Spinner favorites;
    Button editFaves;
    static SharedPreferences sharedPrefs;
    AlertDialog.Builder alert;
    static ArrayList<String> faveList;
    ArrayList<String> infoList;
    boolean isValidSymbol;
    static Map<String,?> keys;
    static ArrayAdapter adapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editFaves = (Button)findViewById(R.id.btnEdit);
        editFaves.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditFavorites.class);
                startActivity(intent);
                if (MainActivity.this.hasWindowFocus()) {
                    reloadData();
                }
            }
        });
        isValidSymbol = true;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        faveList = new ArrayList<String>();
        //Getting all saved items as a mapped set of key value pairs to iterate over and fill the favorites dropdown list
        getFaves();
        symbolIn = (EditText)findViewById(R.id.txtSymbol);
        result = (ListView)findViewById(R.id.lstStock);
        favorites = (Spinner)findViewById(R.id.dropSymbols);
        //String[] faves = {"Select Favorite", "dis", "msn", "mic"};
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, faveList);
        favorites.setAdapter(adapter);
        favorites.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if(position >0) {
                    hideKeyboard();
                    MainActivity.symbol = String.valueOf(favorites.getSelectedItem());
                    AsyncCallWS task = new AsyncCallWS();
                    task.execute();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnStock = (Button) findViewById(R.id.btnGetStock);
        btnStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (symbolIn.length() != 0 && symbolIn.getText().toString() != "")
                {
                    MainActivity.symbol = symbolIn.getText().toString();
                    AsyncCallWS task = new AsyncCallWS();
                    task.execute();
                }
                else
                {
                    alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setMessage("Please enter a value or select from the list of favorites.").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
                    alert.show();
                }
                hideKeyboard();
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void getInfo(String symbol)
    {
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        PropertyInfo stockPI = new PropertyInfo();
        stockPI.setName("symbol");
        stockPI.setValue(symbol);
        stockPI.setType(String.class);
        request.addProperty(stockPI);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE mailman = new HttpTransportSE(URL);
        try
        {
            mailman.call(SOAP_ACTION, envelope);
            SoapPrimitive response = (SoapPrimitive)envelope.getResponse();
            String stockInfo1 = response.toString();
            String[] tagNames = {"Name", "Last", "High", "Low", "PercentageChange"};
            getElement(tagNames, stockInfo1);
            stockInfo = "Stock Info\n";
            Log.i(TAG, response.toString());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    //Borrowed and modified from http://www.java2s.com/Code/Java/XML/ParseanXMLstringUsingDOMandaStringReader.htm
    // Iterates through an array of tag names to get desired elemnt values
    private void getElement(String[] tagNames, String xml) throws ParserConfigurationException, IOException, SAXException
    {
        String val = "";
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(xml));
        Document doc = db.parse(is);
        NodeList nodes = doc.getElementsByTagName("Stock");
        infoList = new ArrayList<String>();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Element element = (Element) nodes.item(i);
            for(int tn=0; tn<tagNames.length; tn++)
            {
                NodeList nodeItem = element.getElementsByTagName(tagNames[tn]);
                Element itemLine = (Element) nodeItem.item(0);
                val = (tagNames[tn] + ": " + getCharacterDataFromElement(itemLine));
                infoList.add(val);

                if(val.contains("N/A"))
                {
                    isValidSymbol = false;
                }
                else
                {
                    isValidSymbol = true;
                }
            }
        }

    }

    public static String getCharacterDataFromElement(Element e)
    {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }

    private class AsyncCallWS extends AsyncTask<String, Void, Void>
    {

        @Override
        protected Void doInBackground(String... params)
        {
            Log.i(TAG, "doInBackground");
            getInfo(symbol);
            return null;
        }

        @Override
        protected void onPostExecute(Void r)
        {
            Log.i(TAG, "onPostExecute");
            if(isValidSymbol)
            {
                if(!keys.containsKey(symbol))
                {
                    alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setMessage("Would you like to add this stock to your favorites?").setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            SharedPreferences.Editor editor = sharedPrefs.edit();
                            editor.putString(symbol, symbol);
                            editor.commit();
                            reloadData();
                            dialog.dismiss();
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    }).create();
                    alert.show();
                }
                result = (ListView) findViewById(R.id.lstStock);
                ArrayAdapter<String> resultAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, infoList);
                result.setAdapter(resultAdapter);

            }
            else
            {
                alert = new AlertDialog.Builder(MainActivity.this);
                alert.setMessage(symbol + " is not a valid stock symbol").setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                }).setTitle("Invalid Stock Symbol").create();
                alert.show();
            }

        }

        @Override
        protected void onPreExecute()
        {
            Log.i(TAG, "onPreExecute");
        }

        @Override
        protected void onProgressUpdate(Void... values)
        {
            Log.i(TAG, "onProgressUpdate");
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

    public void reloadData(){
        // get new modified random data
        getFaves();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, faveList);
        adapter.notifyDataSetChanged();
    }

    public void getFaves()
    {
        faveList.clear();
        keys = sharedPrefs.getAll();
        if(keys.size() == 0)
        {
            faveList.add("You have no favorites yet.");
        }
        else
        {
            faveList.add("-- Select a favorite --");
        }
        for(Map.Entry<String,?> entry : keys.entrySet())
        {
            faveList.add(entry.getValue().toString());
            /*Log.d("map values",entry.getKey() + ": " +
                    entry.getValue().toString());*/
        }
    }
}
