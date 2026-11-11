package ro.pub.cs.systems.eim.practicaltes01

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
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

    // C.1 - scorul NU este atasat niciunui control grafic
    private var scor = 0
    private var serviceStarted = false
    // C.2 b) - configuratia ultimului "Play"
    private var lastPlayKey: String? = null

    // C.1 - primim castigul de la a doua activitate
    private val secondaryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                scor += result.data?.getIntExtra(Constants.EXTRA_GAINED, 0) ?: 0
            }
            Toast.makeText(this, "Scor: $scor", Toast.LENGTH_SHORT).show()
            notifyService()
        }

    // D.2 - receiver pentru mesajele de difuzare
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

    // B.1
    private fun onPlay() {
        val values = (0..2).map { i ->
            val current = numbers[i].text.toString().trim()
            // checkbox bifat -> pastram valoarea din camp; nebifat -> o suprascriem cu random
            if (checks[i].isChecked && current.isNotEmpty()) current else Constants.SYMBOLS.random()
        }
        values.forEachIndexed { i, v -> numbers[i].setText(v) }
        val checked = checks.count { it.isChecked }

        Toast.makeText(this, values.joinToString(" "), Toast.LENGTH_SHORT).show()

        // C.2 b) - aceeasi configuratie ca la ultimul Play => afisam direct scorul pastrat
        val key = values.joinToString(",") + "|" + checked
        if (key == lastPlayKey) {
            Toast.makeText(this, "Scor: $scor", Toast.LENGTH_SHORT).show()
            return
        }
        lastPlayKey = key

        // B.2 - intent explicit catre a doua activitate
        val intent = Intent(this, PracticalTest01Var06SecondaryActivity::class.java)
            .putExtra(Constants.EXTRA_NUMBERS, values.toTypedArray())
            .putExtra(Constants.EXTRA_CHECKED, checked)
        secondaryLauncher.launch(intent)
    }

    // D.1 b) pornire la primul scor > 0; D.1 a) notificare cand scor > 300
    private fun notifyService() {
        if ((scor > 0 && !serviceStarted) || scor > Constants.VICTORY_THRESHOLD) {
            startService(
                Intent(this, PracticalTest01Var06Service::class.java)
                    .putExtra(Constants.EXTRA_SCOR, scor)
            )
            serviceStarted = true
        }
    }

    // C.2 a) - salvare/restaurare scor la distrugerea activitatii de catre sistem
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
        notifyService() // serviciul a fost oprit in onDestroy, il repornim daca scor > 0
    }

    // D.1 b) - serviciul se opreste cand activitatea principala e distrusa
    override fun onDestroy() {
        unregisterReceiver(victoryReceiver)
        stopService(Intent(this, PracticalTest01Var06Service::class.java))
        serviceStarted = false
        super.onDestroy()
    }
}
