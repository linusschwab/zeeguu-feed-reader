package ch.unibe.scg.zeeguufeedreader;

import android.app.Fragment;
import android.os.Bundle;

public class DataFragment extends Fragment {
    // Stored data
    private ZeeguuConnectionManager connectionManager;

    // This method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment
        setRetainInstance(true);
    }

    // Getters and Setters
    public ZeeguuConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public void setConnectionManager(ZeeguuConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
}
