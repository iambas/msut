package com.darker.motorservice.utility;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.darker.motorservice.R;

/**
 * Created by Darker on 29/11/60.
 */

public class CallPhoneUtil {

    @SuppressLint("SetTextI18n")
    public static void callPhoneDialog(final Context context, String contactName, final String phoneNumber){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();

        @SuppressLint("InflateParams")
        View inflateView = inflater.inflate(R.layout.contact_service, null);
        builder.setView(inflateView);
        final AlertDialog alertDialog = builder.show();
        TextView txtName = (TextView) inflateView.findViewById(R.id.txt_name);
        txtName.setText(context.getString(R.string.push_btn_to_call_phone) + contactName);

        FloatingActionButton fab = (FloatingActionButton) inflateView.findViewById(R.id.fab_call);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callPhoneByNumber(context, phoneNumber);
//                pushStat("dialogCall");
                alertDialog.dismiss();
            }
        });
    }

    public static void callPhoneByNumber(Context context, String phoneNumber){
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));

        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.CALL_PHONE}, 10);
        }
        context.startActivity(intent);
    }
}
