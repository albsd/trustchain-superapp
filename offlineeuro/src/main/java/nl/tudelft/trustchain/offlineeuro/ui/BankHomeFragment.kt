package nl.tudelft.trustchain.offlineeuro.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.tudelft.trustchain.offlineeuro.R
import nl.tudelft.trustchain.offlineeuro.communication.IPV8CommunicationProtocol
import nl.tudelft.trustchain.offlineeuro.community.OfflineEuroCommunity
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import nl.tudelft.trustchain.offlineeuro.cryptography.PairingTypes
import nl.tudelft.trustchain.offlineeuro.db.AddressBookManager
import nl.tudelft.trustchain.offlineeuro.db.DepositedEuroManager
import nl.tudelft.trustchain.offlineeuro.entity.Bank
import nl.tudelft.trustchain.offlineeuro.entity.TTP

class BankHomeFragment : OfflineEuroBaseFragment(R.layout.fragment_bank_home) {
    private lateinit var bank: Bank
    private lateinit var iPV8CommunicationProtocol: IPV8CommunicationProtocol
    private lateinit var community: OfflineEuroCommunity

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        if (ParticipantHolder.bank != null) {
            bank = ParticipantHolder.bank!!
        } else {
            activity?.title = "Bank"
            community = getIpv8().getOverlay<OfflineEuroCommunity>()!!
            val group = BilinearGroup(PairingTypes.FromFile, context = context)
            val addressBookManager = AddressBookManager(context, group)
            val depositedEuroManager = DepositedEuroManager(context, group)
            iPV8CommunicationProtocol = IPV8CommunicationProtocol(addressBookManager, community)
            bank =
                Bank(
                    "Bank",
                    group,
                    iPV8CommunicationProtocol,
                    context,
                    depositedEuroManager
                )
            bank.addCallback(onDataChangeCallBack)
        }
        val communicationProtocol = bank.communicationProtocol as IPV8CommunicationProtocol
        viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                communicationProtocol.scopePeers()
                delay(1000)
            }
        }
        onDataChangeCallBack(null)
    }

    private val onDataChangeCallBack: (String?) -> Unit = { message ->
        if (this::bank.isInitialized) {
            requireActivity().runOnUiThread {
                val context = requireContext()
                CallbackLibrary.bankCallback(context, message, requireView(), bank)
            }
        }
    }
}
