package nl.tudelft.trustchain.offlineeuro.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import nl.tudelft.trustchain.offlineeuro.R
import nl.tudelft.trustchain.offlineeuro.communication.ICommunicationProtocol
import nl.tudelft.trustchain.offlineeuro.communication.IPV8CommunicationProtocol
import nl.tudelft.trustchain.offlineeuro.community.OfflineEuroCommunity
import nl.tudelft.trustchain.offlineeuro.cryptography.BilinearGroup
import nl.tudelft.trustchain.offlineeuro.cryptography.PairingTypes
import nl.tudelft.trustchain.offlineeuro.db.AddressBookManager
import nl.tudelft.trustchain.offlineeuro.entity.User
import nl.tudelft.trustchain.offlineeuro.enums.Role

class BankSelectorFragment : OfflineEuroBaseFragment(R.layout.fragment_bank_selector) {
    private lateinit var community: OfflineEuroCommunity
    private lateinit var communicationProtocol: IPV8CommunicationProtocol
    private lateinit var user: User

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ParticipantHolder.user == null) {
            Toast.makeText(
                requireContext(),
                "Something went wrong",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        } else {
            user = ParticipantHolder.user!!
        }

        community = getIpv8().getOverlay<OfflineEuroCommunity>()!!

        communicationProtocol = user.communicationProtocol as IPV8CommunicationProtocol

        val welcomeTextView = view.findViewById<TextView>(R.id.bank_selector_welcome_text)
        welcomeTextView.text = welcomeTextView.text.toString().replace("_name_", user.name)

        fetchBanks(requireContext(), view, communicationProtocol)
    }

    private fun fetchBanks(
        context: Context,
        view: View,
        communicationProtocol: IPV8CommunicationProtocol
    ) {
        val bankAddresses = communicationProtocol.addressBookManager.getAllAddresses().filter { it.type == Role.Bank }
        Log.i("list_banks", "Found ${bankAddresses.size} banks")
        Log.i("list_banks", bankAddresses.getOrElse(0){"nada"}.toString())
        //Update the possible banks table a user can register at
        val bankSelectorTable = view.findViewById<LinearLayout>(R.id.bank_selector_list) ?: return
        TableHelpers.removeAllButFirstRow(bankSelectorTable)
        TableHelpers.addBanksToTable(bankSelectorTable, bankAddresses, context)
    }

    private fun moveToUserHome(userName: String) {
        val bundle = bundleOf("userName" to userName)
        findNavController().navigate(R.id.nav_home_userhome, bundle)
    }
}
