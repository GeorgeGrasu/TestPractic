package ro.pub.cs.systems.eim.practicaltes01

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

// This is the second screen of your app. It also inherits from AppCompatActivity.
class PracticalTest01SecondaryActivity : AppCompatActivity() {

    // onCreate is triggered as soon as this second screen is opened.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Link this Kotlin code to its specific XML layout file
        // (which defines how the UI looks on this second screen).
        setContentView(R.layout.activity_practical_test01_secondary)

        // 1. RECEIVING DATA FROM MAIN ACTIVITY
        // Find the EditText box where we want to display the total clicks.
        val totalText = findViewById<EditText>(R.id.total_clicks_text)

        // Extract the data that was passed from MainActivity using the 'intent.putExtra' method.
        // The '0' at the end is a default value just in case the data is missing.
        val total = intent.getIntExtra(Constants.TOTAL_CLICKS, 0)

        // Convert the integer to a string and put it inside the text box on the screen.
        totalText.setText(total.toString())


        // 2. SENDING A RESPONSE BACK TO MAIN ACTIVITY

        // Find the "OK" button and tell it what to do when clicked.
        findViewById<Button>(R.id.ok_button).setOnClickListener {
            // Set the result of this activity to RESULT_OK.
            // MainActivity's 'onActivityResult' will see this and know the user clicked OK.
            setResult(RESULT_OK)

            // finish() destroys this secondary activity and returns the user to the previous screen (MainActivity).
            finish()
        }

        // Find the "Cancel" button and tell it what to do when clicked.
        findViewById<Button>(R.id.cancel_button).setOnClickListener {
            // Set the result of this activity to RESULT_CANCELED.
            // MainActivity's 'onActivityResult' will see this and know the user clicked Cancel.
            setResult(RESULT_CANCELED)

            // Close this screen and go back.
            finish()
        }
    }
}