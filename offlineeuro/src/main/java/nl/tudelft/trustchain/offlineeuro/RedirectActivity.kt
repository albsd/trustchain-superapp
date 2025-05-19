package nl.tudelft.trustchain.offlineeuro
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Receives the custom-scheme redirect (myapp://callback?...).
 * It doesn’t show a UI; it just forwards the user to the main screen.
 */
class RedirectActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ─── OPTIONAL: inspect the deep-link URI — remove if you don’t need it ───
        val uri = intent?.data    // e.g. myapp://callback?vp_token=...&state=...
        // You could log it, validate state/nonce, or stash it in ViewModel, etc.
        println(uri)
        // ─── Jump back to the main screen ───
        val mainIntent = Intent(this, OfflineEuroMainActivity::class.java).apply {
            // If the main activity is already running, just bring that one forward
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or   // clear anything above it
                Intent.FLAG_ACTIVITY_SINGLE_TOP     // reuse existing instance
        }
        startActivity(mainIntent)

        // Close this trampoline activity so it doesn’t sit in the back-stack
        finish()
    }
}
