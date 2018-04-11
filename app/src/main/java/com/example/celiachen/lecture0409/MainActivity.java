package com.example.celiachen.lecture0409;

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button sendButton;
    private Button pickContact;
    private EditText phoneEditText;
    private EditText messageEditText;

    private static final int REQUEST_SMS = 0;

    // in order to send SMS
    // broadcast to receive
    // BroadcaseReceiver -> send and deliver


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = findViewById(R.id.send_button);
        pickContact = findViewById(R.id.add_contact_button);
        phoneEditText = findViewById(R.id.phone_number_edit_text);
        messageEditText = findViewById(R.id.message_edit_text);

        Typeface font = Typeface.createFromAsset(getAssets(),"fonts/Condition-Regular.ttf");
        phoneEditText.setTypeface(font);
        messageEditText.setTypeface(font);
        sendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick( View view){
                // permission
                // if you are building on 23 or above,
                // you need to ask for permission at runtime
                // here, we need to ask for permission to allow SMS to be sent

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // ask permisson at runtime
                    if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

                        if (!shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
                            showRationale("You need to give permission to send SMS.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            requestPermissions(new String[]{Manifest.permission.SEND_SMS},
                                                    REQUEST_SMS);
                                        }
                                    });
                            return;
                        }
                        requestPermissions(new String[] {Manifest.permission.SEND_SMS},
                                REQUEST_SMS);
                        return;

                    }
                    // SEND THE MESSAGE

                    sendMessage();

                }
            }
        });

        // pick contact button
        pickContact.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // to access the contact
                // 1. you can ask permission to access Contacts
                /**
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    // ask for permission
                    if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
                        if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)){
                            showRationale("You need to give access to read your contacts.", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 0);
                                    return;
                                }
                            });
                        }
                    }
                    // readContacts()
                }
                 */
                // 2. you can use external sd card storage by using an intent
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(intent, 0);

            }
        });
    }

    public void sendMessage(){
        // phonenumber
        String phoneNumber = phoneEditText.getText().toString();
        // message
        String message = messageEditText.getText().toString();

        // if the phone number is empty, alert window to warn users
        if (phoneNumber.isEmpty()){
            Toast.makeText(getApplicationContext(),
                    "No phone number found.",
                    Toast.LENGTH_SHORT).show();
        }
        else{
            SmsManager sms = SmsManager.getDefault();
            // long message
            // create a list of messages
            List<String> messages = sms.divideMessage(message);

            // for each message, you create the intent and send
            for (int i = 0; i < messages.size(); i++){
                PendingIntent sentIntent = PendingIntent.getBroadcast(this, REQUEST_SMS,
                        new Intent("SMS SENT"), 0);
                PendingIntent deliverIntent = PendingIntent.getBroadcast(this, REQUEST_SMS,
                        new Intent("SMS DELIVERED"), 0);

                sms.sendTextMessage(phoneNumber, null,
                        messages.get(i), sentIntent, deliverIntent);

            }
        }
        // else
        // get the phone number and message
        // send the message to the number using intent to broadcast
        // if the message is too long
        // then message will be divided






    }


    private void showRationale(String message, DialogInterface.OnClickListener okay){
        // showing an alert box of the message
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("Okay", okay) // when its okay, show the permission window
                .setNegativeButton("Cancel", null) // when it cancels, do nothing
                .create()
                .show();
    }

    // onRequestPermissionResult takes the permission result to see whether these permissions are granted.
    // Same thing as what we did last week but now you check for SMS.

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SMS:
                if (grantResults.length > 0 &&  grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access sms", Toast.LENGTH_SHORT).show();
                    sendMessage();

                }else {
                    Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and sms", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(android.Manifest.permission.SEND_SMS)) {
                            showRationale("You need to allow access to both the permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(new String[]{android.Manifest.permission.SEND_SMS},
                                                        REQUEST_SMS);
                                            }
                                        }
                                    });
                            return;
                        }
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Uri contactData = data.getData();
                Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
                cursor.moveToFirst();

                String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                phoneEditText.setText(number);
            }
        }
    }
}
