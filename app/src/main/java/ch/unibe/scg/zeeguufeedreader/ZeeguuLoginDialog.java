package ch.unibe.scg.zeeguufeedreader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class ZeeguuLoginDialog extends DialogFragment {

    private SharedPreferences sharedPref;
    private String title = "";

    private EditText usernameEditText;
    private EditText passwordEditText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_zeeguu_login, null);

        usernameEditText = (EditText) mainView.findViewById(R.id.username);
        passwordEditText = (EditText) mainView.findViewById(R.id.password);

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        if (savedInstanceState != null) {
            usernameEditText.setText(savedInstanceState.getString("username"));
            passwordEditText.setText(savedInstanceState.getString("password"));
        }
        else {
            usernameEditText.setText(sharedPref.getString("pref_zeeguu_username", ""));
            passwordEditText.setText(sharedPref.getString("pref_zeeguu_password", ""));
        }

        builder.setMessage(title);
        builder.setView(mainView)
                .setPositiveButton(R.string.signin, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // save login information
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("pref_zeeguu_username", usernameEditText.getText().toString());
                        editor.putString("pref_zeeguu_password", passwordEditText.getText().toString());
                        editor.commit();
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

    public void setTitle(String title) {
        this.title = title;
    }
}