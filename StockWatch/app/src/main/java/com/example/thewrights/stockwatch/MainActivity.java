package com.example.thewrights.stockwatch;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class MainActivity extends ActionBarActivity {

   private final String NAMESPACE = "http://www.webserviceX.NET";

    private final String URL = "http://www.webservicex.net/stockquote.asmx";

    private final String SOAP_ACTION = "http://www.webserviceX.NET/GetQuote";

    private final String METHOD_NAME = "GetQuote";

    /*private final String NAMESPACE = "http://www.w3schools.com/webservices/";

    private final String URL = "http://www.w3schools.com/webservices/tempconvert.asmx";

    private final String SOAP_ACTION = "http://www.w3schools.com/webservices/CelsiusToFahrenheit";

    private final String METHOD_NAME = "CelsiusToFahrenheit";*/

    private String TAG = "CIS295";

    private static String symbol;

    private static String stockInfo;

    EditText symbolIn;
    Button btnStock;
    TextView result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        symbolIn = (EditText)findViewById(R.id.txtSymbol);
        result = (TextView)findViewById(R.id.tvStock);

        btnStock = (Button)findViewById(R.id.btnGetStock);
        btnStock.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(symbolIn.length() != 0 && symbolIn.getText().toString() != "")
                {
                    symbol = symbolIn.getText().toString();

                    AsyncCallWS task = new AsyncCallWS();
                    task.execute();
                }
                else
                {
                    result.setText("Please enter a value");
                }
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

    public void getStock(View view)
    {

    }

    public void getInfo(String symbol)
    {
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        PropertyInfo stockPI = new PropertyInfo();
        stockPI.setName("symbol");
        stockPI.setValue(symbol);
        //stockPI.setType(Double.class);
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
            stockInfo = response.toString();
            Log.i(TAG, response.toString());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

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
            result.setText(stockInfo);
        }

        @Override
        protected void onPreExecute()
        {
            Log.i(TAG, "onPreExecute");
            result.setText("Fetching Stock Info");
        }

        @Override
        protected void onProgressUpdate(Void... values)
        {
            Log.i(TAG, "onProgressUpdate");

        }
    }
}
