package nl.tudelft.trustchain.offlineeuro.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import nl.tudelft.trustchain.offlineeuro.R
import nl.tudelft.trustchain.offlineeuro.communication.IPV8CommunicationProtocol
import nl.tudelft.trustchain.offlineeuro.community.OfflineEuroCommunity
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import nl.tudelft.trustchain.offlineeuro.cryptography.PairingTypes
import nl.tudelft.trustchain.offlineeuro.db.AddressBookManager
import nl.tudelft.trustchain.offlineeuro.entity.Address
import nl.tudelft.trustchain.offlineeuro.entity.DualRole
import nl.tudelft.trustchain.offlineeuro.enums.Role

class HomeFragment : OfflineEuroBaseFragment(R.layout.fragment_home) {

    private var mockTTP = false

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
            if (ParticipantHolder.user == null)
                showAlertDialog()
            else
                if (mockTTP) {
                    runMockTTP()
                }
                findNavController().navigate(R.id.action_homeFragment_to_userHomeFragment)
        }
        view.findViewById<Button>(R.id.JoinAsAllRolesButton).setOnClickListener {
            findNavController().navigate(R.id.nav_home_all_roles_home)
        }

        val mockTtpSwitch = view.findViewById<Switch>(R.id.mockTTPSwitch)
        mockTtpSwitch.setOnCheckedChangeListener { _, isChecked ->
            mockTTP = isChecked
            Toast.makeText(
                requireContext(),
                if (isChecked) "Local TTP enabled" else "Local TTP disabled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun runMockTTP() {
        if (ParticipantHolder.ttp == null && ParticipantHolder.dualRole == null) {
            val group = BilinearGroup(PairingTypes.FromFile, context = requireContext())
            val community = getIpv8().getOverlay<OfflineEuroCommunity>()!!
            val addressBookManager = AddressBookManager(requireContext(), group)
            val iPV8CommunicationProtocol = IPV8CommunicationProtocol(addressBookManager, community)

            val dualRole = DualRole(requireContext(), group, iPV8CommunicationProtocol, "LocalDualRole")

            dualRole.addCallback { message ->
                requireActivity().runOnUiThread {
                    CallbackLibrary.ttpCallback(requireContext(), message, requireView(), dualRole.getTTP())
                }
            }

            ParticipantHolder.dualRole = dualRole
            iPV8CommunicationProtocol.participant = dualRole

            // Insert address with DualRole role
            iPV8CommunicationProtocol.addressBookManager.insertAddress(
                Address(dualRole.name, Role.DualRole, dualRole.publicKey, null)
            )

            Toast.makeText(requireContext(), "Local DualRole started", Toast.LENGTH_SHORT).show()
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
            val username = editText.text?.toString() ?: ""
            if (username.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Illegal username",
                    Toast.LENGTH_LONG
                )
                    .show()
                dialog.cancel()
            } else {
                moveToWalletRegistration(username)
            }
        }

        // Set negative button
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
        }

        // Create and show the AlertDialog
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun moveToWalletRegistration(userName: String) {
        val bundle = bundleOf("userName" to userName)
        findNavController().navigate(R.id.nav_wallet_registration, bundle)
    }
}
