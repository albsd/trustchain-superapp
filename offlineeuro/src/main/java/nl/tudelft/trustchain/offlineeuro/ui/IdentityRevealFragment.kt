package nl.tudelft.trustchain.offlineeuro.ui

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import nl.tudelft.trustchain.offlineeuro.R

class IdentityRevealFragment : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        super.onCreateView(inflater, container, savedInstanceState)

        val context = requireContext()
        val isFraud = arguments?.getBoolean("status") ?: false
        val familyName = arguments?.getString("family_name") ?: "N/A"
        //todo: replace username and userPK with information from the jwt
        val givenName = arguments?.getString("name") ?: ""
        val userPK = arguments?.getString("userPK") ?: "N/A"
        val country = arguments?.getString("country") ?: "N/A"

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(48, 48, 48, 48)
            background = ContextCompat.getDrawable(context, R.drawable.rounded_frame)
        }

        val titleView = TextView(context).apply {
            text = "Double Spending Info"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
        }

        val statusView = TextView(context).apply {
            text = "Status: ${if (isFraud) "Double Spent" else "Not double spent"}"
            textSize = 16f
            setPadding(0, 16, 0, 16)
        }

        layout.addView(titleView)
        layout.addView(statusView)

        if (isFraud) {
            layout.addView(TextView(context).apply {
                text = "Name: $familyName $givenName"
                textSize = 14f
                setPadding(0, 8, 0, 8)
            })

            layout.addView(TextView(context).apply {
                text = "Public Key: $userPK"
                textSize = 14f
                setPadding(0, 8, 0, 8)
            })

            layout.addView(TextView(context).apply {
                text = "Issuing Country: $country"
                textSize = 14f
                setPadding(0, 8, 0, 8)
            })
        }

        val closeButton = ImageButton(context).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            background = null
            val size = (40 * context.resources.displayMetrics.density).toInt()
            layoutParams = FrameLayout.LayoutParams(size, size).apply {
                gravity = Gravity.END or Gravity.TOP
            }
            setOnClickListener { dismiss() }
        }

        val containerLayout = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            clipToOutline = true
            addView(layout)
            addView(closeButton)
        }

        return containerLayout
    }
}

