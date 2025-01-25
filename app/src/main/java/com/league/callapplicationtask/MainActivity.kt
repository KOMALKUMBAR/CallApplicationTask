package com.league.callapplicationtask


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.Intent
import android.net.Uri

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val phoneNumber = findViewById<EditText>(R.id.phoneNumber)
        val callButton = findViewById<Button>(R.id.callButton)
        val viewContacts = findViewById<Button>(R.id.viewContacts)
        val viewCallLogs = findViewById<Button>(R.id.viewCallLogs)

        // Make a Call
        callButton.setOnClickListener {
            val number = phoneNumber.text.toString()
            if (number.isNotEmpty()) {
                makeCall(number)
            } else {
                Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT).show()
            }
        }

        // View Contacts
        viewContacts.setOnClickListener {
            if (checkPermission(Manifest.permission.READ_CONTACTS)) {
                val contacts = fetchContacts()
                Toast.makeText(this, "Contacts: $contacts", Toast.LENGTH_LONG).show()
            } else {
                requestPermission(Manifest.permission.READ_CONTACTS, 101)
            }
        }

        // View Call Logs
        viewCallLogs.setOnClickListener {
            if (checkPermission(Manifest.permission.READ_CALL_LOG)) {
                val logs = fetchCallLogs()
                Toast.makeText(this, "Call Logs: $logs", Toast.LENGTH_LONG).show()
            } else {
                requestPermission(Manifest.permission.READ_CALL_LOG, 102)
            }
        }
    }

    private fun makeCall(number: String) {
        if (checkPermission(Manifest.permission.CALL_PHONE)) {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$number")
            startActivity(callIntent)
        } else {
            requestPermission(Manifest.permission.CALL_PHONE, 100)
        }
    }

    private fun fetchContacts(): List<String> {
        val contacts = mutableListOf<String>()
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        cursor?.use {
            while (it.moveToNext()) {
                val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                if (nameIndex != -1) { // Check if the column index is valid
                    val name = it.getString(nameIndex)
                    contacts.add(name)
                }
            }
        }
        return contacts
    }


    private fun fetchCallLogs(): List<String> {
        val logs = mutableListOf<String>()
        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            CallLog.Calls.DATE + " DESC"
        )
        cursor?.use {
            while (it.moveToNext()) {
                val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)

                if (numberIndex != -1 && typeIndex != -1) { // Check both column indices
                    val number = it.getString(numberIndex)
                    val type = it.getInt(typeIndex)
                    val typeStr = when (type) {
                        CallLog.Calls.INCOMING_TYPE -> "Incoming"
                        CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                        CallLog.Calls.MISSED_TYPE -> "Missed"
                        else -> "Unknown"
                    }
                    logs.add("Number: $number, Type: $typeStr")
                }
            }
        }
        return logs
    }


    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }
}
