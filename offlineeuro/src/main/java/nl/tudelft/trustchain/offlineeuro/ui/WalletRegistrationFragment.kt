package nl.tudelft.trustchain.offlineeuro.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.tudelft.trustchain.offlineeuro.R
import nl.tudelft.trustchain.offlineeuro.communication.IPV8CommunicationProtocol
import nl.tudelft.trustchain.offlineeuro.community.OfflineEuroCommunity
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import nl.tudelft.trustchain.offlineeuro.cryptography.PairingTypes
import nl.tudelft.trustchain.offlineeuro.db.AddressBookManager
import nl.tudelft.trustchain.offlineeuro.entity.EUDIAuthManager
import nl.tudelft.trustchain.offlineeuro.entity.User
import nl.tudelft.trustchain.offlineeuro.enums.Role

class WalletRegistrationFragment : OfflineEuroBaseFragment(R.layout.fragment_wallet_registration) {
    private lateinit var community: OfflineEuroCommunity
    private lateinit var communicationProtocol: IPV8CommunicationProtocol
    private lateinit var user: User
    private lateinit var eudiAuthManager: EUDIAuthManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        activity?.title = "User"
//        val userName: String? = arguments?.getString("userName")
//        val welcomeTextView = view.findViewById<TextView>(R.id.user_home_welcome_text)
//        welcomeTextView.text = welcomeTextView.text.toString().replace("_name_", userName!!)
//        community = getIpv8().getOverlay<OfflineEuroCommunity>()!!
//
//        val group = BilinearGroup(PairingTypes.FromFile, context = context)
//        val addressBookManager = AddressBookManager(context, group)
//        communicationProtocol = IPV8CommunicationProtocol(addressBookManager, community)
//        try {
//            user = User(userName, group, context, null, communicationProtocol, onDataChangeCallback = onUserDataChangeCallBack)
//            communicationProtocol.scopePeers()
//        } catch (e: Exception) {
//            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
//        }

        activity?.title = "User"
        var userName: String? = arguments?.getString("userName")
        userName = userName ?: return findNavController().navigate(R.id.nav_null_username)

        community = getIpv8().getOverlay<OfflineEuroCommunity>()!!

        val group = BilinearGroup(PairingTypes.FromFile, context = context)
        val addressBookManager = AddressBookManager(context, group)
        communicationProtocol = IPV8CommunicationProtocol(addressBookManager, community)

        eudiAuthManager = EUDIAuthManager(
            ::openInWallet,
            {
                requireActivity().runOnUiThread {
                    findNavController().navigate(R.id.action_walletRegistrationFragment_to_userHomeFragment)
                }
            },
            {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Could not verify pid", Toast.LENGTH_LONG).show()
                }
            },
            communicationProtocol
        )

        try {
            user = User(
                userName,
                group,
                context,
                null,
                communicationProtocol,
                runSetup = false,
                onDataChangeCallback
            )

            user.authManager = eudiAuthManager

            ParticipantHolder.user = user
            viewLifecycleOwner.lifecycleScope.launch {
                while (true) {
                    refresh()
                    delay(1000)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openInWallet(deepLink: Uri) {
        requireActivity().runOnUiThread {
            Log.e("wallet frontend", "am ajuns aici")
            // When a deep link is ready, the option to open in wallet becomes available
            val openWalletButton =
                requireView().findViewById<Button>(R.id.wallet_registration_button)
            openWalletButton.isEnabled = true

            // Creates an Android intent with the uri
            openWalletButton.setOnClickListener {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, deepLink).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), "No wallet app found", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(eudiAuthManager.isPending() || eudiAuthManager.isNotStarted())
            return
        eudiAuthManager.afterUserReturns()
        //todo: make a buffering state
        Toast.makeText(requireContext(), "Awaiting verification from TTP", Toast.LENGTH_LONG).show()
    }

    private fun refresh() {
        communicationProtocol.scopePeers()
        //onDataChangeCallback(null)
        Log.i("Peers", "Found ${communicationProtocol.addressBookManager.getAllAddresses().size} peers")
    }

    private val onDataChangeCallback: (String?) -> Unit = { message ->
        if (this::user.isInitialized) {
            requireActivity().runOnUiThread {
                val context = requireContext()
                val view = this.view ?: return@runOnUiThread

                if (message != null) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }

                // Update TTP addresses for wallet registration
                val ttpAddresses = communicationProtocol.addressBookManager.getAllAddresses().filter { it.type == Role.TTP }
                val ttpTable = view.findViewById<LinearLayout>(R.id.wallet_registration_ttp_list)
                TableHelpers.removeAllButFirstRow(ttpTable)
                TableHelpers.addTTPsToTable(ttpTable, ttpAddresses, context, user)
            }
        }
    }
}
