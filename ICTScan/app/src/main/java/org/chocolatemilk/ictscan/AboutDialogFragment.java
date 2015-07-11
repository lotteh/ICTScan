package org.chocolatemilk.ictscan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by Lotti on 08.07.2015.
 */
public class AboutDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.aboutThisApp)
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // just dismiss
            }
        })
                .setNeutralButton(R.string.openInfoLink, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.georg-boecherer.de/ict-cubes.html"));
                            startActivity(myIntent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(getActivity(), "No application can handle this request."
                                    + " Please install a webbrowser", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                })
                .setTitle("About this Application");
        return builder.create();
    }
}
