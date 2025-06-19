package nl.tudelft.trustchain.offlineeuro.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
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
import nl.tudelft.trustchain.offlineeuro.entity.User
import nl.tudelft.trustchain.offlineeuro.enums.Role

class UserHomeFragment : OfflineEuroBaseFragment(R.layout.fragment_user_home) {
    private lateinit var user: User
    private lateinit var community: OfflineEuroCommunity
    private lateinit var communicationProtocol: IPV8CommunicationProtocol

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        if (ParticipantHolder.user != null) {
            user = ParticipantHolder.user!!
            user.addCallback(onUserDataChangeCallBack)
            communicationProtocol = user.communicationProtocol as IPV8CommunicationProtocol
            community = getIpv8().getOverlay<OfflineEuroCommunity>()!!
            val userName: String = user.name
            val welcomeTextView = view.findViewById<TextView>(R.id.user_welcome_text)
            welcomeTextView.text = welcomeTextView.text.toString().replace("_name_", userName)
            viewLifecycleOwner.lifecycleScope.launch {
                while (true) {
                    communicationProtocol.scopePeers()
                    delay(1000)
                }
            }
        } else {
            Log.e("user_home", "User should have been initialized already before reaching user home fragment!!")
            return findNavController().navigate(R.id.action_userHomeFragment_to_homeFragment)
        }

        view.findViewById<Button>(R.id.user_home_reset_button).setOnClickListener {
            communicationProtocol.addressBookManager.clear()
            user.reset()
            updateAllAddresses(view)
        }
        view.findViewById<Button>(R.id.user_home_sync_addresses).setOnClickListener {
            communicationProtocol.scopePeers()
        }
        view.findViewById<Button>(R.id.withdraw_button).setOnClickListener {
            val bank = communicationProtocol.addressBookManager.getAllAddresses().filter { it.type == Role.Bank }
            if (bank.isEmpty()) {
                Toast.makeText(context, "Could not find bank. Try again later...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val options = arrayOf("1 €", "5 €", "25 €", "100 €")
            val values = arrayOf(1L, 5L, 25L, 100L)

            AlertDialog.Builder(requireContext())
                .setTitle("Select amount to withdraw")
                .setItems(options) { dialog, which ->
                    val selectedValue = values[which]
                    try {
                        user.withdrawDigitalEuro("Bank", value = selectedValue)
                        Toast.makeText(context, "Requested $selectedValue €", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to withdraw: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        updateAllAddresses(view)
        onUserDataChangeCallBack(null)
    }

    fun updateAllAddresses(view: View) {
        val addresses = communicationProtocol.addressBookManager.getAllAddresses()

        val bankAddresses = addresses.filter { it.type == Role.Bank }
        val userAddresses = addresses.filter { it.type == Role.User }
        val ttpAddresses  = addresses.filter { it.type == Role.TTP }

        // Update bank accounts table
//        val bankAccountsList = view.findViewById<LinearLayout>(R.id.user_home_bankAccountsList)
//        TableHelpers.addAddressesToTable(bankAccountsList, bankAddresses, user, requireContext(), ::showTransactionWindow)

//        // Update peer table
//        val peerList = view.findViewById<LinearLayout>(R.id.user_home_peerList)
//        TableHelpers.addAddressesToTable(peerList, userAddresses, user, requireContext(), ::showTransactionWindow)

        // Update TTP table
        val TTPList = view.findViewById<LinearLayout>(R.id.user_home_TTPList)
        TableHelpers.addAddressesToTable(TTPList, ttpAddresses, user, requireContext(), ::showTransactionWindow)
    }

    // Alert dialog for performing a transaction
    private fun showTransactionWindow(userName: String) {
        val builder = AlertDialog.Builder(requireContext())
        val editText = EditText(requireContext())

        builder.setView(editText)
        builder.setTitle("Amount to send:")

        // Listener for performing a transaction: sending euro to another peer
//        builder.setPositiveButton("Send") { _, _ ->
//                try {
//                    user.sendDigitalEuroTo(userName)
//                } catch (e: Exception) {
//                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
//                }
//        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
        }

        builder.setIcon(R.drawable.ic_baseline_euro_symbol_24)
        builder.show()
    }

    private fun onSendTokenClick(amount: Long, tokenSerialNumber: String) {
        showUserSelectionWithConfirmation("Send", amount) { selectedUserName ->
            try {
                user.sendDigitalEuroTo(selectedUserName, tokenSerialNumber)
                Toast.makeText(requireContext(), "Sent $amount to $selectedUserName", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message ?: "Error sending euro", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onDoubleSpendTokenClick(amount: Long, tokenSerialNumber: String) {
        showUserSelectionWithConfirmation("Double Spend", amount) { selectedUserName ->
            try {
                user.doubleSpendDigitalEuroTo(selectedUserName, tokenSerialNumber)
                Toast.makeText(requireContext(), "Double spent $amount to $selectedUserName", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message ?: "Error double spending euro", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onDepositToken(amount: Long, tokenSerialNumber: String) {
        user.doubleSpendDigitalEuroTo("Bank", tokenSerialNumber)
        Toast.makeText(context, "Deposited token!", Toast.LENGTH_SHORT).show()
    }

    private fun showUserSelectionWithConfirmation(
        actionName: String,
        amount: Long,
        onConfirmed: (String) -> Unit
    ) {
        if (!isAdded) return

        val context = requireContext()
        val users = user.retrieveScopedUsers()
        var selectedUser: String? = null

        val builder = AlertDialog.Builder(context)
        builder.setTitle("$actionName $amount € to:")

        builder.setSingleChoiceItems(users.toTypedArray(), -1) { _, which ->
            selectedUser = users[which]
        }

        builder.setPositiveButton("Next") { dialog, _ ->
            if (selectedUser == null) {
                Toast.makeText(context, "Please select a user", Toast.LENGTH_SHORT).show()
            } else {
                dialog.dismiss()

                if (isAdded) {
                    AlertDialog.Builder(context)
                        .setTitle("Confirm $actionName")
                        .setMessage("$actionName $amount € to $selectedUser?")
                        .setPositiveButton("Confirm") { confirmDialog, _ ->
                            selectedUser?.let { onConfirmed(it) }
                            confirmDialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { confirmDialog, _ ->
                            confirmDialog.dismiss()
                        }
                        .show()
                }
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.setIcon(R.drawable.ic_baseline_euro_symbol_24)
        builder.show()
    }

    private val onUserDataChangeCallBack: (String?) -> Unit = { message ->
        enableWithdraw();
        requireActivity().runOnUiThread {
            val context = requireContext()
            if (this::user.isInitialized) {
                CallbackLibrary.userCallback(
                    context,
                    message,
                    requireView(),
                    communicationProtocol,
                    user
                ) { view ->
                    updateAllAddresses(view)

                    val userBalanceTextView = view.findViewById<TextView>(R.id.user_balance)
                    userBalanceTextView.text = "Balance: ${user.getBalance()} €"

                    val tokenContainer = view.findViewById<LinearLayout>(R.id.token_container)

                    TableHelpers.updateTokens(
                        tokenContainer,
                        user,
                        context,
                        onSendClick = ::onSendTokenClick,
                        onDoubleSpendClick = ::onDoubleSpendTokenClick,
                        onDepositClick = ::onDepositToken
                    )
                }
            }
        }
    }

    private fun enableWithdraw() {
        val bank = communicationProtocol.addressBookManager.getAllAddresses().filter { it.type == Role.Bank }
        val withdrawButton = requireView().findViewById<Button>(R.id.withdraw_button)
        if (bank.isEmpty()) {
            withdrawButton.isEnabled = false
        } else {
            withdrawButton.isEnabled = true
        }
    }

    val updateUI: (View) -> Unit
        get() = { view -> updateAllAddresses(view) }
}
