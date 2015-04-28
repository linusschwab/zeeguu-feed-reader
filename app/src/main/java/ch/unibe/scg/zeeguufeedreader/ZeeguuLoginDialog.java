package ch.unibe.scg.zeeguufeedreader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class ZeeguuLoginDialog extends DialogFragment {

    private SharedPreferences sharedPref;
    private String title = "";

    private EditText usernameEditText;
    private EditText passwordEditText;

    private ZeeguuConnectionManager connectionManager;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_zeeguu_login, null);

        usernameEditText = (EditText) mainView.findViewById(R.id.username);
        passwordEditText = (EditText) mainView.findViewById(R.id.password);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (savedInstanceState != null) {
            usernameEditText.setText(savedInstanceState.getString("username"));
            passwordEditText.setText(savedInstanceState.getString("password"));
        }

        String button;
        if (title.equals(getActivity().getString(R.string.logout_zeeguu_title))) {
            usernameEditText.setText(R.string.logout_zeeguu_confirmation);
            disableEditText(usernameEditText);
            passwordEditText.setVisibility(View.GONE);
            button = getActivity().getString(R.string.logout);
        }
        else
            button = getActivity().getString(R.string.signin);


        // Temporary workaround
        MainActivity main = (MainActivity) getActivity();
        connectionManager = main.getConnectionManager();

        builder.setMessage(title);
        builder.setView(mainView)
                .setPositiveButton(button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (!title.equals(getActivity().getString(R.string.logout_zeeguu_title))) {
                            // Try to get a session ID to check if the password is correct
                            connectionManager.getSessionId(
                                    usernameEditText.getText().toString(),
                                    passwordEditText.getText().toString());
                        }
                        else
                            connectionManager.getAccount().logout();

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("username", usernameEditText.getText().toString());
        savedInstanceState.putString("password", passwordEditText.getText().toString());
    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
        editText.setBackgroundColor(Color.TRANSPARENT);
        editText.setTextColor(Color.BLACK);
    }

    /**
     *  Allows to set a different title, for example if the user entered a wrong password.
     *  Must be called before the DialogFragment is shown!
     */
    public void setTitle(String title) {
        this.title = title;
    }
}