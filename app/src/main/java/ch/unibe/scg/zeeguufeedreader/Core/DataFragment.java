package ch.unibe.scg.zeeguufeedreader.Core;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        connectionManager = new ZeeguuConnectionManager(getActivity());
    }

    public void onRestore(Activity activity) {
        // ConnectionManager
        if (connectionManager != null)
            connectionManager.onRestore(activity);
        else
            connectionManager = new ZeeguuConnectionManager(activity);
    }

    // Getters and Setters
    public ZeeguuConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public void setConnectionManager(ZeeguuConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
}
