package nl.tudelft.trustchain.offlineeuro.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import nl.tudelft.trustchain.offlineeuro.R
import nl.tudelft.trustchain.offlineeuro.communication.IPV8CommunicationProtocol
import nl.tudelft.trustchain.offlineeuro.community.OfflineEuroCommunity
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import nl.tudelft.trustchain.offlineeuro.cryptography.PairingTypes
import nl.tudelft.trustchain.offlineeuro.db.AddressBookManager
import nl.tudelft.trustchain.offlineeuro.entity.User

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
                onDataChangeCallback = null,
                runSetup = false
            )
            //communicationProtocol.scopePeers()
            ParticipantHolder.user = user
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }
}
