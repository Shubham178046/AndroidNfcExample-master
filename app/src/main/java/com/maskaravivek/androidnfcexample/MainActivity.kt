package com.maskaravivek.androidnfcexample

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.FormatException
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var adapter: NfcAdapter? = null
    var tag: WritableTag? = null
    var tagId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initNfcAdapter()
        initViews()
    }

    private fun initNfcAdapter() {
        val nfcManager = getSystemService(Context.NFC_SERVICE) as NfcManager
        adapter = nfcManager.defaultAdapter
    }

    private fun initViews() {
        write_tag.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this,
                            Manifest.permission.NFC)
                    != PackageManager.PERMISSION_GRANTED)
            {
                Log.d("TAG", "initViews: "+"Call")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.NFC),1)
            }
            else
            {
                Log.d("TAG", "writeNDefMessage: "+"Call")
                writeNDefMessage()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1)
        {
            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                writeNDefMessage()
            }
            else
            {
               // ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.NFC),1)
                Toast.makeText(this , "Permission Not Granted",Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun writeNDefMessage() {
        val message = NfcUtils.prepareMessageToWrite(StringUtils.randomString(44), this)
//        Log.d("TAG", "writeNDefMessage: "+tagId!!)
        Log.d("TAG", "writeMessage: "+message)
        val writeResult = tag!!.writeData(tagId!!, message)
        if (writeResult) {
            showToast("Write successful!")
        } else {
            showToast("Write failed!")
        }
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        enableNfcForegroundDispatch()
    }

    override fun onPause() {
        disableNfcForegroundDispatch()
        super.onPause()
    }

    private fun enableNfcForegroundDispatch() {
        try {
            val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
            adapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
        } catch (ex: IllegalStateException) {
            Log.e(getTag(), "Error enabling NFC foreground dispatch", ex)
        }
    }

    private fun disableNfcForegroundDispatch() {
        try {
            adapter?.disableForegroundDispatch(this)
        } catch (ex: IllegalStateException) {
            Log.e(getTag(), "Error disabling NFC foreground dispatch", ex)
        }
    }

    private fun getTag() = "MainActivity"

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("TAG", "onNewIntent: "+"Call")
        val tagFromIntent = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        try {
            tag = WritableTag(tagFromIntent)
        } catch (e: FormatException) {
            Log.e(getTag(), "Unsupported tag tapped", e)
            return
        }
        tagId = tag!!.tagId
        showToast("Tag tapped: $tagId")

//        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
//            val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
//            if (rawMsgs != null) {
//                onTagTapped(NfcUtils.getUID(intent), NfcUtils.getData(rawMsgs))
//            }
//        }
    }

    private fun onTagTapped(superTagId: String, superTagData: String) {
        tag_uid.text = superTagId
        tag_data.text = superTagData
    }
}
