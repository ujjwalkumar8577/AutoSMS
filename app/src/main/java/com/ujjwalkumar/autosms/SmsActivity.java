package com.ujjwalkumar.autosms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SmsActivity extends AppCompatActivity {

    private TextView textViewStatus;
    private EditText editTextMessage;
    private ImageView imageViewInfo;
    private Button buttonSend, buttonSelect;
    private RadioGroup radioGroupSelectSim;
    private SmsManager smsManager;

    private ArrayList<String> phoneNumbers = new ArrayList<>();
    private int simID = 1;                          // default sim 1
    private final int REQUEST_CODE = 897;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        textViewStatus = findViewById(R.id.textViewStatus);
        editTextMessage = findViewById(R.id.editTextMessage);
        imageViewInfo = findViewById(R.id.imageViewInfo);
        buttonSend = findViewById(R.id.buttonSend);
        buttonSelect = findViewById(R.id.buttonSelect);
        radioGroupSelectSim = findViewById(R.id.radioGroupSelectSim);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        }

        textViewStatus.setText("Status : Select contacts file");

        radioGroupSelectSim.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i) {
                    case R.id.radioButtonSim1: {
                        Toast.makeText(SmsActivity.this, "SIM 1 selected", Toast.LENGTH_SHORT).show();
                        simID = 1;
                        break;
                    }
                    case R.id.radioButtonSim2: {
                        Toast.makeText(SmsActivity.this, "SIM 2 selected", Toast.LENGTH_SHORT).show();
                        simID = 2;
                        break;
                    }
                    default: {
                        Toast.makeText(SmsActivity.this, "SIM 1 selected", Toast.LENGTH_SHORT).show();
                        simID = 1;
                    }
                }
            }
        });

        imageViewInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder info = new AlertDialog.Builder(SmsActivity.this);
                info.setTitle("Info");
                info.setMessage("Enter the message, select the contacts file and click Send.");
                info.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                info.create().show();
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editTextMessage.getText().toString().equals("")) {
                    Toast.makeText(SmsActivity.this, "Empty message", Toast.LENGTH_SHORT).show();
                }
                else if(phoneNumbers.size()<=0) {
                    Toast.makeText(SmsActivity.this, "Empty contact list", Toast.LENGTH_SHORT).show();
                }
                else {
                    sendSMStoAll();
                }
            }
        });
        
        buttonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(Intent.ACTION_GET_CONTENT);
                in.setType("text/*");
                in.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(Intent.createChooser(in, "Select text file"), 987);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void readFile(Uri uri) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            phoneNumbers = new ArrayList<>();
            int skipped = 0;
            String line = null;
            while ((line = br.readLine()) != null) {
                if(isValid(line))
                    phoneNumbers.add(line);
                else
                    skipped++;
            }
            br.close();
            if(skipped>0)
                Toast.makeText(SmsActivity.this, "Skipped " + skipped + " lines", Toast.LENGTH_SHORT).show();
            textViewStatus.setText("Status : Retrieved " + phoneNumbers.size() + " contacts");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendSMStoAll() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
            smsManager = SmsManager.getSmsManagerForSubscriptionId(simID);
        else
            smsManager = SmsManager.getDefault();                   // Default is always 1

        String message = editTextMessage.getText().toString();
        int num = phoneNumbers.size();
        int sim = 1;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
            sim = smsManager.getSubscriptionId();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Confirm");
        alertDialog.setMessage("Message : " + message + "\nNo. of contacts : " + num + "\nSim " + sim);
        alertDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int count = 0;
                for(String phoneNumber : phoneNumbers) {
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                    count++;
                    textViewStatus.setText("Status : Sent " + count + " of " + num);
                }
                Toast.makeText(SmsActivity.this, "Message sent to all", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.create().show();
    }

    public boolean isValid(String num) {
        if(num.length()==10 && TextUtils.isDigitsOnly(num))
            return true;
        else
            return false;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder exit = new AlertDialog.Builder(this);
        exit.setTitle("Exit");
        exit.setMessage("Do you want to exit?");
        exit.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        exit.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        exit.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==987 && data!=null) {
            Uri uri = data.getData();
            readFile(uri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE && grantResults[0]!=-1 && grantResults[1]!=-1 && grantResults[2]!=-1 ) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}