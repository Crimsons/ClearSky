package ee.vincent.clearsky.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import ee.vincent.clearsky.R;

/**
 * Created by jakob on 3.01.2015.
 */
public class NewRouteDialog extends DialogFragment {

    public static final String TAG = "NewRouteDialog";

    private EditText routeNameEditText;
    private NewRouteDialogListener listener;


    public interface NewRouteDialogListener {
        public void NewRouteDialogSuccess(String routeName);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        listener = (NewRouteDialogListener)activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_new_route, null);
        routeNameEditText = (EditText)dialogLayout.findViewById(R.id.edit_route_name);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dlg_new_route_title)
                .setView(dialogLayout)
                .setPositiveButton(R.string.dlg_new_route_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String routeName = routeNameEditText.getText().toString().trim();
                        listener.NewRouteDialogSuccess(routeName);
                    }
                })
                .setNegativeButton(R.string.dlg_new_route_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        return  builder.create();
    }


}
