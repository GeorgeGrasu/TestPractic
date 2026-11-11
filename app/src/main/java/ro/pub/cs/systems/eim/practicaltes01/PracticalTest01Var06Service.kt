package ro.pub.cs.systems.eim.practicaltes01

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PracticalTest01Var06Service : Service() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val scor = intent?.getIntExtra(Constants.EXTRA_SCOR, 0) ?: 0

        if (scor > Constants.VICTORY_THRESHOLD) {
            handler.postDelayed({
                val dateTime = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                sendBroadcast(
                    Intent(Constants.ACTION_VICTORY)
                        .setPackage(packageName)
                        .putExtra(Constants.EXTRA_DATETIME, dateTime)
                        .putExtra(Constants.EXTRA_SCOR, scor)
                )
            }, 2000)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
