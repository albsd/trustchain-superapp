package nl.tudelft.trustchain.offlineeuro.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
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
import nl.tudelft.trustchain.offlineeuro.entity.User
import nl.tudelft.trustchain.offlineeuro.enums.Role

class WalletRegistrationFragment : OfflineEuroBaseFragment(R.layout.fragment_wallet_registration) {
    private lateinit var community: OfflineEuroCommunity
    private lateinit var communicationProtocol: IPV8CommunicationProtocol
    private lateinit var user: User

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = "User"
        var userName: String? = arguments?.getString("userName")
        userName = userName ?: return findNavController().navigate(R.id.nav_null_username)

        community = getIpv8().getOverlay<OfflineEuroCommunity>()!!

        val group = BilinearGroup(PairingTypes.FromFile, context = context)
        val addressBookManager = AddressBookManager(context, group)
        communicationProtocol = IPV8CommunicationProtocol(addressBookManager, community)
        try {
            user = User(
                userName,
                group,
                context,
                null,
                communicationProtocol,
                runSetup = false
            )

            ParticipantHolder.user = user
            user.addCallback(onDataChangeCallback)
            lifecycleScope.launch {
                while(true) {
                    refresh()
                    delay(1000)
                }
            }
            refresh()
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun refresh() {
        communicationProtocol.scopePeers()
        onDataChangeCallback(null)
        Log.i("Peers", "Found ${communicationProtocol.addressBookManager.getAllAddresses().size} peers")
    }

    private val onDataChangeCallback: (String?) -> Unit = { message ->
        if (this::user.isInitialized) {
            requireActivity().runOnUiThread {
                val context = requireContext()
                val view = requireView()

                if (message != null) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }

                // Update TTP addresses for wallet registration
                val ttpAddresses = communicationProtocol.addressBookManager.getAllAddresses().filter { it.type == Role.TTP }
                val ttpTable = view.findViewById<LinearLayout>(R.id.wallet_registration_ttp_list)
                TableHelpers.removeAllButFirstRow(ttpTable)
                TableHelpers.addTTPsToTable(ttpTable, ttpAddresses, context)
            }
        }
    }
}
