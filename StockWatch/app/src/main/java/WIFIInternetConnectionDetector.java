import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by Tiffany on 7/8/2015.
 */
public class WIFIInternetConnectionDetector
{
    private Context _context;

    public WIFIInternetConnectionDetector(Context context) {
        this._context = context;
    }

    public boolean checkWifiInternetConn() {
        //Create object for ConnectivityManager class which returns network related info
        ConnectivityManager connManager =
                (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //For WiFi Check
        boolean isWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .isConnectedOrConnecting();

        return isWifi;
    }
}
