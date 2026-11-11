package ro.pub.cs.systems.eim.practicaltes01

// In Kotlin, 'object' creates a Singleton. This means there is only ever one instance
// of this class in memory, making it behave like 'static' fields in Java.
object Constants {

    // Used as a label when printing messages to the Android Logcat console.
    // Example: Log.d(Constants.TAG, "Hello World")
    const val TAG = "[PracticalTest01]"

    // Keys used to safely save and retrieve the number of clicks from the Intent
    // or when saving the screen state (onSaveInstanceState).
    const val LEFT_COUNT = "leftCount"
    const val RIGHT_COUNT = "rightCount"

    // The key used to attach the total number of clicks to the Intent
    // when navigating to the SecondaryActivity.
    const val TOTAL_CLICKS = "totalClicks"

    // A unique ID number used to identify our specific request when we call
    // startActivityForResult(). When the second activity finishes, MainActivity
    // checks this code to know WHICH activity is returning a result.
    const val SECONDARY_ACTIVITY_REQUEST_CODE = 1

    // The specific target number of clicks. Once the left + right clicks get higher
    // than this number (5), the app automatically triggers the background Service.
    const val NUMBER_OF_CLICKS_THRESHOLD = 5

    // Status flags to keep track of whether our background Service is currently
    // running (1) or if it is turned off (0).
    const val SERVICE_STOPPED = 0
    const val SERVICE_STARTED = 1

    // The key used to attach the math calculation string (the date, arithmetic mean,
    // and geometric mean) to the broadcast message sent from the Service.
    const val BROADCAST_EXTRA = "message"

    // An array of custom action strings.
    // 1. The Service picks one of these randomly to label its broadcast message.
    // 2. The MainActivity registers an IntentFilter with these exact strings so it
    //    knows to "wake up" and listen when a message with these labels is broadcasted.
    val ACTION_TYPES = arrayOf(
        "ro.pub.cs.systems.eim.practicaltest01.arithmetic",
        "ro.pub.cs.systems.eim.practicaltest01.geometric",
        "ro.pub.cs.systems.eim.practicaltest01.both"
    )
}