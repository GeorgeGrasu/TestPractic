package ro.pub.cs.systems.eim.practicaltes01

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlin.math.sqrt
import java.util.Date

// This class extends Service, meaning it will run invisibly in the background of the Android system.
class PracticalTest01Service : Service() {

    // A reference to our custom thread so we can stop it later.
    private var thread: ProcessingThread? = null

    // onStartCommand is called every time MainActivity calls 'startService(intent)'.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Extract the left and right numbers sent from the MainActivity.
        // If nothing was sent, default to 0.
        val first = intent?.getIntExtra(Constants.LEFT_COUNT, 0) ?: 0
        val second = intent?.getIntExtra(Constants.RIGHT_COUNT, 0) ?: 0

        // 2. Create a new background thread with these numbers and start it.
        // We use a separate Thread so we don't freeze the app's main screen while it sleeps and calculates.
        thread = ProcessingThread(first, second).also { it.start() }

        // 3. START_REDELIVER_INTENT tells Android: "If the system runs out of memory and kills this service,
        // please restart it later and give it the exact same Intent (numbers) it had before."
        return START_REDELIVER_INTENT
    }

    // onBind is required when extending Service.
    // Returning 'null' means this is a "Started Service" (runs until we stop it),
    // not a "Bound Service" (which would tie its lifespan strictly to the component that called it).
    override fun onBind(intent: Intent?): IBinder? = null

    // onDestroy is called when MainActivity calls 'stopService()' or the system destroys the service.
    override fun onDestroy() {
        // Stop our custom thread so it doesn't keep running and draining battery in the background forever.
        thread?.stopThread()
        super.onDestroy()
    }

    // -------------------------------------------------------------------------
    // Custom Thread Class
    // -------------------------------------------------------------------------
    // This is where the actual background work happens.
    private inner class ProcessingThread(
        private val first: Int,
        private val second: Int
    ) : Thread() {

        // @Volatile means this variable is always read fresh from main memory.
        // It ensures that if the main app changes 'running' to false, this thread sees the change immediately.
        @Volatile
        private var running = true

        // A helper method to safely shut down this infinite loop.
        fun stopThread() {
            running = false
            interrupt() // If the thread is currently sleeping (the 10-second delay), wake it up immediately so it can die.
        }

        // The run() method contains the code that executes when the thread starts.
        override fun run() {
            // Keep looping as long as 'running' is true.
            while (running) {
                // 1. Pick a random action string from our list of actions in Constants.
                val action = Constants.ACTION_TYPES.random()

                // 2. Do some math.
                val arithmetic = (first + second) / 2.0 // Arithmetic mean (average)
                val geometric = sqrt(first.toDouble() * second) // Geometric mean

                // 3. Prepare a message (Broadcast) to send out to the Android system.
                val broadcast = Intent(action)

                // For security and performance in modern Android, we restrict this broadcast
                // so it only goes to our specific app (packageName).
                broadcast.setPackage(packageName)

                // Attach our calculations and the current date/time to the broadcast.
                broadcast.putExtra(
                    Constants.BROADCAST_EXTRA,
                    "${Date()} $arithmetic $geometric"
                )

                // 4. Send the broadcast!
                // MainActivity's 'MessageBroadcastReceiver' is listening for this exact message.
                sendBroadcast(broadcast)

                // 5. Pause the thread for 10 seconds (10,000 milliseconds).
                try {
                    sleep(10000)
                } catch (e: InterruptedException) {
                    // If the thread is interrupted while sleeping (because we called stopThread()),
                    // catch the exception and exit the run() method cleanly.
                    return
                }
            }
        }
    }
}