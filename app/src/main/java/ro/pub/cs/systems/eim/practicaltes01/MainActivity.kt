package ro.pub.cs.systems.eim.practicaltes01

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// MainActivity is the main screen of your app. It inherits from AppCompatActivity.
class MainActivity : AppCompatActivity() {

    // Declare UI elements. 'lateinit' means we promise to initialize them later (in onCreate).
    private lateinit var leftEditText: EditText
    private lateinit var rightEditText: EditText
    private lateinit var leftButton: Button
    private lateinit var rightButton: Button
    private lateinit var navigateButton: Button

    // Keeps track of whether our background service is running or stopped.
    private var serviceStatus = Constants.SERVICE_STOPPED

    // A receiver that listens for messages broadcasted by the background service.
    private val messageBroadcastReceiver = MessageBroadcastReceiver()
    // A filter to specify exactly which broadcast messages we want to listen to.
    private val intentFilter = IntentFilter()

    // onCreate is called when the activity is first created. This is where you set up the UI.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Makes the app draw behind the system UI (status bar/navigation bar)
        setContentView(R.layout.activity_main) // Links this code to your XML layout file

        // Adjusts padding so UI elements don't overlap with the system status bar or navigation bar.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Link our code variables to the actual UI elements in the XML using their IDs.
        leftEditText = findViewById(R.id.left_edit_text)
        rightEditText = findViewById(R.id.right_edit_text)
        leftButton = findViewById(R.id.left_button)
        rightButton = findViewById(R.id.right_button)
        navigateButton = findViewById(R.id.play_button)

        // Create a custom click listener and attach it to the Left and Right buttons.
        val buttonClickListener = ButtonClickListener()
        leftButton.setOnClickListener(buttonClickListener)
        rightButton.setOnClickListener(buttonClickListener)

        // Set up the listener for the Navigate button.
        navigateButton.setOnClickListener {
            // Calculate the total number of clicks from both text boxes.
            val total = leftEditText.text.toString().toInt() +
                    rightEditText.text.toString().toInt()

            // Create an Intent to jump to the SecondaryActivity.
            val intent = Intent(this, PracticalTest01SecondaryActivity::class.java)
            // Attach the total clicks to the intent so the second activity can read it.
            intent.putExtra(Constants.TOTAL_CLICKS, total)
            // Start the second activity and wait for a result back (OK or CANCEL).
            startActivityForResult(intent, Constants.SECONDARY_ACTIVITY_REQUEST_CODE)
        }

        // Add specific action strings to our intent filter.
        // Our receiver will only wake up for broadcasts matching these actions.
        Constants.ACTION_TYPES.forEach { intentFilter.addAction(it) }
    }

    // Custom class that handles what happens when Left or Right buttons are clicked.
    private inner class ButtonClickListener : View.OnClickListener {
        override fun onClick(view: View) {
            // Read the current numbers from the text boxes.
            var left = leftEditText.text.toString().toInt()
            var right = rightEditText.text.toString().toInt()

            // Check which button was clicked and increment the corresponding number.
            when (view.id) {
                R.id.left_button -> leftEditText.setText((++left).toString())
                R.id.right_button -> rightEditText.setText((++right).toString())
            }

            // If the total clicks exceed a certain threshold AND the service isn't running yet...
            if (left + right > Constants.NUMBER_OF_CLICKS_THRESHOLD &&
                serviceStatus == Constants.SERVICE_STOPPED
            ) {
                // Create an intent to start our custom background service.
                val intent = Intent(applicationContext, PracticalTest01Service::class.java)
                // Pass the current click counts to the service.
                intent.putExtra(Constants.LEFT_COUNT, left)
                intent.putExtra(Constants.RIGHT_COUNT, right)

                // Start the service and update our status tracker.
                startService(intent)
                serviceStatus = Constants.SERVICE_STARTED
            }
        }
    }

    // This receiver listens for global broadcast messages sent by the Service.
    private inner class MessageBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // When a message is received, extract it and log it to the Android Logcat console.
            Log.d(Constants.TAG, intent?.getStringExtra(Constants.BROADCAST_EXTRA) ?: "")
        }
    }

    // Called when the screen rotates or the system needs to kill the app to free memory.
    // We save our important data here so it doesn't get lost.
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(Constants.LEFT_COUNT, leftEditText.text.toString())
        outState.putString(Constants.RIGHT_COUNT, rightEditText.text.toString())
    }

    // Called when the app is recreated (e.g., after screen rotation).
    // We retrieve our saved data and put it back into the UI.
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        leftEditText.setText(savedInstanceState.getString(Constants.LEFT_COUNT) ?: "0")
        rightEditText.setText(savedInstanceState.getString(Constants.RIGHT_COUNT) ?: "0")
    }

    // This is triggered when we come back from the SecondaryActivity.
    @Deprecated("Deprecated in Java") // Modern Android uses ActivityResultLauncher, but this still works for older/school projects
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check if the result is coming from OUR specific secondary activity request.
        if (requestCode == Constants.SECONDARY_ACTIVITY_REQUEST_CODE) {
            // Figure out if the user clicked "OK" or "CANCEL" in the second activity.
            val result = if (resultCode == RESULT_OK) "OK" else "CANCEL"
            // Show a tiny popup message (Toast) at the bottom of the screen with the result.
            Toast.makeText(this, "Result: $result", Toast.LENGTH_LONG).show()
        }
    }

    // onResume is called when the app comes to the foreground and the user can interact with it.
    override fun onResume() {
        super.onResume()
        // We start listening for broadcast messages while the app is actively on screen.
        ContextCompat.registerReceiver(
            this, messageBroadcastReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    // onPause is called when the app goes to the background (e.g., user opens another app).
    override fun onPause() {
        // Stop listening for broadcasts to save battery and prevent memory leaks.
        unregisterReceiver(messageBroadcastReceiver)
        super.onPause()
    }

    // onDestroy is called when the activity is completely closed and destroyed.
    override fun onDestroy() {
        // Stop the background service so it doesn't run forever and drain the battery.
        stopService(Intent(this, PracticalTest01Service::class.java))
        super.onDestroy()
    }
}