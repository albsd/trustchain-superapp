package nl.tudelft.trustchain.offlineeuro.ui

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import nl.tudelft.trustchain.offlineeuro.R
import nl.tudelft.trustchain.offlineeuro.community.OfflineEuroCommunity

class HomeFragment : OfflineEuroBaseFragment(R.layout.fragment_home) {
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Home"

        view.findViewById<Button>(R.id.JoinAsTTP).setOnClickListener {
            findNavController().navigate(R.id.nav_home_ttphome)
        }

        view.findViewById<Button>(R.id.JoinAsBankButton).setOnClickListener {
            findNavController().navigate(R.id.nav_home_bankhome)
        }

        view.findViewById<Button>(R.id.JoinAsUserButton).setOnClickListener {
            showAlertDialog()
        }
        view.findViewById<Button>(R.id.JoinAsAllRolesButton).setOnClickListener {
            findNavController().navigate(R.id.nav_home_all_roles_home)
        }
        view.findViewById<Button>(R.id.OpenInWallet).setOnClickListener {

            //eudi-openid4vp://?
            // client_id=x509_san_dns%3Averifier-backend.eudiw.dev&
            // request_uri=https%3A%2F%2Fverifier-backend.eudiw.dev%2Fwallet%2Frequest.jwt%2Fhvs0AgIgEG8NlZQDBDh6OVG2kqgrtKt9Ni0h2rEgvpmqeEBuWwDuvaE_U6MpucbimDTn7wXiOnL89y5plCvErw&
            // request_uri_method=get

            val deepLink = """
            eudi-openid4vp://?
            client_id=x509_san_dns:verifier-backend.eudiw.dev&
            request=eyJ4NWMiOlsiTUlJREREQ0NBcktnQXdJQkFnSVVHOFNndVVyYmdwSlV2ZDZ2KzA3U3A4dXRMZlF3Q2dZSUtvWkl6ajBFQXdJd1hERWVNQndHQTFVRUF3d1ZVRWxFSUVsemMzVmxjaUJEUVNBdElGVlVJREF5TVMwd0t3WURWUVFLRENSRlZVUkpJRmRoYkd4bGRDQlNaV1psY21WdVkyVWdTVzF3YkdWdFpXNTBZWFJwYjI0eEN6QUpCZ05WQkFZVEFsVlVNQjRYRFRJMU1EUXhNREEyTkRVMU9Gb1hEVEkzTURReE1EQTJORFUxTjFvd1Z6RWRNQnNHQTFVRUF3d1VSVlZFU1NCU1pXMXZkR1VnVm1WeWFXWnBaWEl4Q2pBSUJnTlZCQVVUQVRFeEhUQWJCZ05WQkFvTUZFVlZSRWtnVW1WdGIzUmxJRlpsY21sbWFXVnlNUXN3Q1FZRFZRUUdFd0pWVkRCWk1CTUdCeXFHU000OUFnRUdDQ3FHU000OUF3RUhBMElBQk9jaVY0Mm1JVDhuUU1BTjhrVzlDSE5VVFl3a2llZW01aGwxUXNMZjYya0ViYlpoNnd1bDVpTDI4Zy9BM1pxY1RYOVhvTG53L252SjgvSFJwMys5NWVLamdnRlZNSUlCVVRBTUJnTlZIUk1CQWY4RUFqQUFNQjhHQTFVZEl3UVlNQmFBRkdMSGxFY292UStpRmlDbm1zSkpsRVR4QWRQSE1Ea0dBMVVkRVFReU1EQ0JFbTV2TFhKbGNHeDVRR1YxWkdsM0xtUmxkb0lhZG1WeWFXWnBaWEl0WW1GamEyVnVaQzVsZFdScGR5NWtaWFl3RWdZRFZSMGxCQXN3Q1FZSEtJR01YUVVCQmpCREJnTlZIUjhFUERBNk1EaWdOcUEwaGpKb2RIUndjem92TDNCeVpYQnliMlF1Y0d0cExtVjFaR2wzTG1SbGRpOWpjbXd2Y0dsa1gwTkJYMVZVWHpBeUxtTnliREFkQmdOVkhRNEVGZ1FVZ0FoOUtzb1lYWUs4am5kVWJGUUV0ZkRzSGpZd0RnWURWUjBQQVFIL0JBUURBZ2VBTUYwR0ExVWRFZ1JXTUZTR1VtaDBkSEJ6T2k4dloybDBhSFZpTG1OdmJTOWxkUzFrYVdkcGRHRnNMV2xrWlc1MGFYUjVMWGRoYkd4bGRDOWhjbU5vYVhSbFkzUjFjbVV0WVc1a0xYSmxabVZ5Wlc1alpTMW1jbUZ0WlhkdmNtc3dDZ1lJS29aSXpqMEVBd0lEU0FBd1JRSWdERkNneUVqR25KUzI1bi9GZmRQN0hYMGVsejdDMnE0dVVRLzdaY3JsMFFZQ0lRQy9yckpwUTVzRjFPNGFpSGVqSVBQTHVPM0pqZHJMSlBaU0ErRlFIK2VJckE9PSJdLCJ0eXAiOiJvYXV0aC1hdXRoei1yZXErand0IiwiYWxnIjoiRVMyNTYifQ.eyJyZXNwb25zZV91cmkiOiJodHRwczovL3ZlcmlmaWVyLWJhY2tlbmQuZXVkaXcuZGV2L3dhbGxldC9kaXJlY3RfcG9zdC9YemtLQThsV3VIVk1xdWQ3UW5aWWdkS3I1X1k4bGg5ZTh1dWlKcW5YSFhhbVlTU1RtcUsxdkpkRXZFOFkwOTJwcGJYeDVDMzY3RTZWWFFLTDZHbGlNdyIsImF1ZCI6Imh0dHBzOi8vc2VsZi1pc3N1ZWQubWUvdjIiLCJzY29wZSI6IiIsInJlc3BvbnNlX3R5cGUiOiJ2cF90b2tlbiIsInByZXNlbnRhdGlvbl9kZWZpbml0aW9uIjp7ImlkIjoiYzQ4MjJiNTgtN2ZiNC00NTRlLWI4MjctZjg3NThmZTI3ZjlhIiwiaW5wdXRfZGVzY3JpcHRvcnMiOlt7ImlkIjoiZXUuZXVyb3BhLmVjLmV1ZGkucGlkLjEiLCJuYW1lIjoiRVVESSBQSUQiLCJwdXJwb3NlIjoiV2UgbmVlZCB0byB2ZXJpZnkgeW91ciBpZGVudGl0eSIsImZvcm1hdCI6eyJtc29fbWRvYyI6eyJhbGciOlsiRVMyNTYiXX19LCJjb25zdHJhaW50cyI6eyJmaWVsZHMiOlt7InBhdGgiOlsiJFsnZXUuZXVyb3BhLmVjLmV1ZGkucGlkLjEnXVsnZmFtaWx5X25hbWUnXSJdLCJpbnRlbnRfdG9fcmV0YWluIjpmYWxzZX1dfX1dfSwic3RhdGUiOiJYemtLQThsV3VIVk1xdWQ3UW5aWWdkS3I1X1k4bGg5ZTh1dWlKcW5YSFhhbVlTU1RtcUsxdkpkRXZFOFkwOTJwcGJYeDVDMzY3RTZWWFFLTDZHbGlNdyIsImlhdCI6MTc0NzY3MjI4OCwibm9uY2UiOiI4OGM2MTVhMi1iN2NhLTQzMjktYjY3My0zZjRjNDE0NmFkMzMiLCJjbGllbnRfaWQiOiJ4NTA5X3Nhbl9kbnM6dmVyaWZpZXItYmFja2VuZC5ldWRpdy5kZXYiLCJjbGllbnRfbWV0YWRhdGEiOnsiYXV0aG9yaXphdGlvbl9lbmNyeXB0ZWRfcmVzcG9uc2VfYWxnIjoiRUNESC1FUyIsImF1dGhvcml6YXRpb25fZW5jcnlwdGVkX3Jlc3BvbnNlX2VuYyI6IkExMjhDQkMtSFMyNTYiLCJqd2tzIjp7ImtleXMiOlt7Imt0eSI6IkVDIiwidXNlIjoiZW5jIiwiY3J2IjoiUC0yNTYiLCJraWQiOiJjY2QxZGJmZi0zNjE2LTQ4YjAtOWM2NC1lNzA1MmVmNmExMjgiLCJ4IjoidHZlMEdJWGFBZHVVd1ZlWEo5aWhFMnpaUTZVaUhRVVctNDVZUXgxYmZSdyIsInkiOiJHTDU0TzkyUWRwbXFDYnVYR1pCSEpqQTJvb2c2ckt2aG5pMVZmOGFfYnZFIiwiYWxnIjoiRUNESC1FUyJ9XX0sImlkX3Rva2VuX2VuY3J5cHRlZF9yZXNwb25zZV9hbGciOiJSU0EtT0FFUC0yNTYiLCJpZF90b2tlbl9lbmNyeXB0ZWRfcmVzcG9uc2VfZW5jIjoiQTEyOENCQy1IUzI1NiIsInZwX2Zvcm1hdHMiOnsidmMrc2Qtand0Ijp7InNkLWp3dF9hbGdfdmFsdWVzIjpbIkVTMjU2IiwiRVMzODQiLCJFUzUxMiJdLCJrYi1qd3RfYWxnX3ZhbHVlcyI6WyJSUzI1NiIsIlJTMzg0IiwiUlM1MTIiLCJFUzI1NiIsIkVTMzg0IiwiRVM1MTIiXX0sImRjK3NkLWp3dCI6eyJzZC1qd3RfYWxnX3ZhbHVlcyI6WyJFUzI1NiIsIkVTMzg0IiwiRVM1MTIiXSwia2Itand0X2FsZ192YWx1ZXMiOlsiUlMyNTYiLCJSUzM4NCIsIlJTNTEyIiwiRVMyNTYiLCJFUzM4NCIsIkVTNTEyIl19LCJtc29fbWRvYyI6eyJhbGciOlsiRVMyNTYiLCJFUzM4NCIsIkVTNTEyIiwiRWREU0EiXX19LCJzdWJqZWN0X3N5bnRheF90eXBlc19zdXBwb3J0ZWQiOlsidXJuOmlldGY6cGFyYW1zOm9hdXRoOmp3ay10aHVtYnByaW50Il0sImlkX3Rva2VuX3NpZ25lZF9yZXNwb25zZV9hbGciOiJSUzI1NiJ9LCJyZXNwb25zZV9tb2RlIjoiZGlyZWN0X3Bvc3Quand0In0.qGv9pnjWqDQWnYssJcZWJuz8t3BfloS8Uq1tAwlx9He6D38FKx2sSJR3SsHNpxVu5Y_U9KZpuIuvrQ4BjvfeEQ
        """.trimIndent().replace("\n", "")   // ← remove embedded line-breaks

                // 2.  Launch the wallet via an ACTION_VIEW intent.
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                try {
                    startActivity(intent)                           // your Fragment’s helper
                } catch (e: ActivityNotFoundException) {
                    // 3.  Optional fallback (no wallet registered for "openid4vp://")
                    Toast.makeText(
                        requireContext(),
                        "No compatible wallet installed to open the link.",
                        Toast.LENGTH_LONG
                    ).show()
                    // or navigate to an instruction screen instead:
                    // findNavController().navigate(R.id.nav_no_wallet_found)
                }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        try {
            val euroTokenCommunity = getIpv8().getOverlay<OfflineEuroCommunity>()
            if (euroTokenCommunity == null) {
                Toast.makeText(requireContext(), "Could not find community", Toast.LENGTH_LONG)
                    .show()
            }
            if (euroTokenCommunity != null) {
                Toast.makeText(requireContext(), "Found community", Toast.LENGTH_LONG)
                    .show()
            }
        } catch (e: Exception) {
            logger.error { e }
            Toast.makeText(
                requireContext(),
                "Failed to send transactions",
                Toast.LENGTH_LONG
            )
                .show()
        }
        return
    }

    private fun showAlertDialog() {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())

        val editText = EditText(requireContext())
        alertDialogBuilder.setView(editText)
        alertDialogBuilder.setTitle("Pick an username")
        alertDialogBuilder.setMessage("")
        // Set positive button
        alertDialogBuilder.setPositiveButton("Join!") { dialog, which ->
            val username = editText.text.toString()
            moveToUserHome(username)
        }

        // Set negative button
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }

        // Create and show the AlertDialog
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun moveToUserHome(userName: String) {
        val bundle = bundleOf("userName" to userName)
        findNavController().navigate(R.id.nav_home_userhome, bundle)
    }
}
