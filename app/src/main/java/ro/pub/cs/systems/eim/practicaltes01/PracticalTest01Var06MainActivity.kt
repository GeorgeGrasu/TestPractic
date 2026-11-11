package ro.pub.cs.systems.eim.practicaltes01

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class PracticalTest01Var06MainActivity : AppCompatActivity() {

    private lateinit var numbers: List<EditText>
    private lateinit var checks: List<CheckBox>

    private var scor = 0
    private val intentFilter = IntentFilter(Constants.BROADCAST_ACTION)
    private var serviceStarted = false
    private var lastPlayKey: String? = null

    private val messageBroadcastReceiver = MessageBroadcastReceiver()

    private val secondaryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                scor += result.data?.getIntExtra(Constants.EXTRA_GAINED, 0) ?: 0
            }
            Toast.makeText(this, "Scor: $scor", Toast.LENGTH_SHORT).show()
            notifyService()
        }

    private val victoryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val dateTime = intent.getStringExtra(Constants.EXTRA_DATETIME)
            val s = intent.getIntExtra(Constants.EXTRA_SCOR, 0)
            Toast.makeText(context, "Victory! $dateTime - scor: $s", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practical_test01_var06_main)

        numbers = listOf(R.id.et1, R.id.et2, R.id.et3).map { findViewById<EditText>(it) }
        checks = listOf(R.id.cb1, R.id.cb2, R.id.cb3).map { findViewById<CheckBox>(it) }

        findViewById<Button>(R.id.btnPlay).setOnClickListener { onPlay() }

        ContextCompat.registerReceiver(
            this, victoryReceiver, IntentFilter(Constants.ACTION_VICTORY),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }


    private fun onPlay() {
        val values = (0..2).map { i ->
            val current = numbers[i].text.toString().trim()
            if (checks[i].isChecked && current.isNotEmpty()) current else Constants.SYMBOLS.random()
        }
        values.forEachIndexed { i, v -> numbers[i].setText(v) }
        val checked = checks.count { it.isChecked }

        Toast.makeText(this, values.joinToString(" "), Toast.LENGTH_SHORT).show()

        val key = values.joinToString(",") + "|" + checked
        if (key == lastPlayKey) {
            Toast.makeText(this, "Scor: $scor", Toast.LENGTH_SHORT).show()
            return
        }
        lastPlayKey = key

        val intent = Intent(this, PracticalTest01Var06SecondaryActivity::class.java)
            .putExtra(Constants.EXTRA_NUMBERS, values.toTypedArray())
            .putExtra(Constants.EXTRA_CHECKED, checked)
        secondaryLauncher.launch(intent)
    }

    private fun notifyService() {
        if ((scor > 0 && !serviceStarted) || scor > Constants.VICTORY_THRESHOLD) {
            startService(
                Intent(this, PracticalTest01Var06Service::class.java)
                    .putExtra(Constants.EXTRA_SCOR, scor)
            )
            serviceStarted = true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(Constants.STATE_SCOR, scor)
        outState.putString(Constants.STATE_LAST_PLAY, lastPlayKey)
    }



    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        scor = savedInstanceState.getInt(Constants.STATE_SCOR, 0)
        lastPlayKey = savedInstanceState.getString(Constants.STATE_LAST_PLAY)
        Toast.makeText(this, "Scor restaurat: $scor", Toast.LENGTH_SHORT).show()
        notifyService()
    }

    private inner class MessageBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val message = intent?.getStringExtra(Constants.EXTRA_DATETIME) ?: ""
            Log.d("UN TAG UN TAG UN TAG", message)
        }
    }

    override fun onResume() {
        super.onResume()
        ContextCompat.registerReceiver(
            this, messageBroadcastReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun onPause() {
        unregisterReceiver(messageBroadcastReceiver)
        super.onPause()
    }

    override fun onDestroy() {
        unregisterReceiver(victoryReceiver)
        stopService(Intent(this, PracticalTest01Var06Service::class.java))
        serviceStarted = false
        super.onDestroy()
    }
}
