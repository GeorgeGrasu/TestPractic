package ro.pub.cs.systems.eim.practicaltes01

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PracticalTest01Var06SecondaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practical_test01_var06_secondary)

        val numbers = intent.getStringArrayExtra(Constants.EXTRA_NUMBERS) ?: emptyArray()
        val checked = intent.getIntExtra(Constants.EXTRA_CHECKED, 0)

        val allEqual = numbers.filter { it != "*" }.toSet().size <= 1

        val gained = if (allEqual) {
            when (checked) { 0 -> 100; 1 -> 50; 2 -> 10; else -> 0 }
        } else 0

        findViewById<TextView>(R.id.tvGained).text = if (allEqual) "Gained: $gained" else ""

        setResult(RESULT_OK, Intent().putExtra(Constants.EXTRA_GAINED, gained))

        findViewById<Button>(R.id.btnOk).setOnClickListener { finish() }
    }
}
