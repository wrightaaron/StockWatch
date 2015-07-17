import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by Tiffany on 7/8/2015.
 */
public class MobileInternetConnectionDetector
{
    private Context _context;

    public MobileInternetConnectionDetector(Context context) {
        this._context = context;
    }

    public boolean checkMobileInternetConn() {
        //Create object for ConnectivityManager class which returns network related info
        ConnectivityManager connManager =
                (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //Get network info - Mobile internet access
        boolean isMobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .isConnectedOrConnecting();
        return isMobile;
    }
}
