package nl.tudelft.trustchain.offlineeuro.ui

import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import nl.tudelft.trustchain.offlineeuro.R
import nl.tudelft.trustchain.offlineeuro.entity.Address
import nl.tudelft.trustchain.offlineeuro.entity.Bank
import nl.tudelft.trustchain.offlineeuro.entity.FraudControlResult
import nl.tudelft.trustchain.offlineeuro.entity.RegisteredUser
import nl.tudelft.trustchain.offlineeuro.entity.User
import nl.tudelft.trustchain.offlineeuro.entity.WalletEntry
import nl.tudelft.trustchain.offlineeuro.enums.Role
import nl.tudelft.trustchain.offlineeuro.libraries.DcSdJWTDecoder

object TableHelpers {
    fun removeAllButFirstRow(table: LinearLayout) {
        val childrenCount = table.childCount
        val childrenToBeRemoved = childrenCount - 1

        for (i in childrenToBeRemoved downTo 1) {
            val row = table.getChildAt(i)
            table.removeView(row)
        }
    }

    // Function to add the registered users to the table in the TTP Fragment
    fun addRegisteredUsersToTable(
        table: LinearLayout,
        users: List<RegisteredUser>
    ) {
        val context = table.context
        for (user in users) {
            table.addView(registeredUserToTableRow(user, context))
        }
    }

    private fun registeredUserToTableRow(
        user: RegisteredUser,
        context: Context,
    ): LinearLayout {
        val tableRow = LinearLayout(context)
        tableRow.layoutParams = rowParams(context)
        tableRow.orientation = LinearLayout.HORIZONTAL

        val styledContext = ContextThemeWrapper(context, R.style.TableCell)

        val idField =
            TextView(styledContext).apply {
                text = user.id.toString()
                layoutParams = layoutParams(0.2f)
                gravity = Gravity.CENTER_HORIZONTAL
            }

        val nameField =
            TextView(styledContext).apply {
                text = user.name
                layoutParams = layoutParams(0.2f)
                gravity = Gravity.CENTER_HORIZONTAL
            }

        val publicKeyField =
            TextView(styledContext).apply {
                text = user.publicKey.toString()
                layoutParams = layoutParams(0.7f)
            }

        tableRow.addView(idField)
        tableRow.addView(nameField)
        tableRow.addView(publicKeyField)
        return tableRow
    }

    // Function to add deposited euro information to the table in the Bank Fragment
    fun addDepositedEurosToTable(
        table: LinearLayout,
        bank: Bank
    ) {
        val context = table.context
        for (depositedEuro in bank.depositedEuroLogger) {
            table.addView(depositedEuroToTableRow(depositedEuro, context))
        }
    }

    private fun depositedEuroToTableRow(
        depositedEuro: Pair<String, FraudControlResult>,
        context: Context
    ): LinearLayout {
        val tableRow = LinearLayout(context)
        tableRow.layoutParams = rowParams(context)
        tableRow.orientation = LinearLayout.HORIZONTAL

        val styledContext = ContextThemeWrapper(context, R.style.TableCell)

        val serialNumberField =
            TextView(styledContext).apply {
                text = depositedEuro.first
                layoutParams = layoutParams(1f)
                gravity = Gravity.CENTER
            }

        val doubleSpendingButton = Button(ContextThemeWrapper(context, R.style.Button)).apply {
            text = "Check"
            layoutParams = layoutParams(1f)
            gravity = Gravity.CENTER
        }

        applyButtonStylingToAction(doubleSpendingButton, context)
        setDoubleSpendingCheckButton(doubleSpendingButton, context, depositedEuro)

        tableRow.addView(serialNumberField)
        tableRow.addView(doubleSpendingButton)

        return tableRow
    }

    fun setDoubleSpendingCheckButton(
        doubleSpendingButton: Button,
        context: Context,
        depositedEuro: Pair<String, FraudControlResult>
    ) {
        doubleSpendingButton.setOnClickListener {
            val bottomSheet = IdentityRevealFragment()

            var args = Bundle()
            args.putBoolean("status", depositedEuro.second.isFraud)

            val claims = depositedEuro.second.jwt?.let { it1 -> DcSdJWTDecoder.decodeSdJwt(it1) }

            args.putString("family_name", claims?.get("family_name"))
            args.putString("name", claims?.get("given_name"))
            args.putString("userPK", depositedEuro.second.userPK?.toString())
            args.putString("country", claims?.get("issuing_country"))
            bottomSheet.arguments = args
            bottomSheet.show((context as AppCompatActivity).supportFragmentManager, "DoubleSpendingSheet")
        }
    }

    // Function to add banks to the table in the Bank Selector Fragment
    fun addBanksToTable(
        table: LinearLayout,
        banks: List<Address>,
        context: Context
    ) {
        for (address in banks) {
            table.addView(bankToTableRow(address, context))
        }
    }

    fun bankToTableRow(
        address: Address,
        context: Context
    ): LinearLayout {
        val tableRow = LinearLayout(context)
        tableRow.layoutParams = rowParams(context)
        tableRow.orientation = LinearLayout.HORIZONTAL

        val styledContext = ContextThemeWrapper(context, R.style.TableCell)

        val bankNameField =
            TextView(styledContext).apply {
                layoutParams = layoutParams(1f)
                text = address.name
                gravity = Gravity.CENTER
            }

        val pkField =
            TextView(styledContext).apply {
                layoutParams = layoutParams(1f)
                text = address.publicKey.toString()
                gravity = Gravity.CENTER
            }

        val registerAtBankButton = Button(ContextThemeWrapper(context, R.style.Button)).apply {
            layoutParams = layoutParams(1f)
            text = "Register"
        }

        applyButtonStylingToAction(registerAtBankButton, context)
        setBankRegisterActionButton(registerAtBankButton, context)

        tableRow.addView(bankNameField)
        tableRow.addView(pkField)
        tableRow.addView(registerAtBankButton)

        return tableRow
    }

    // Function to add TTP addresses to the table in the Wallet Registration Fragment
    fun addTTPsToTable(
        table: LinearLayout,
        ttps: List<Address>,
        context: Context,
        user: User
    ) {
        for (ttp in ttps) {
            table.addView(ttpToTableRow(ttp, context, user))
        }
    }

    fun ttpToTableRow(
        ttp: Address,
        context: Context,
        user: User
    ): LinearLayout {
        val tableRow = LinearLayout(context)
        tableRow.layoutParams = rowParams(context)
        tableRow.orientation = LinearLayout.HORIZONTAL

        val styledContext = ContextThemeWrapper(context, R.style.TableCell)

        val ttpNameField =
            TextView(styledContext).apply {
                text = ttp.name
                layoutParams = layoutParams(1f)
                gravity = Gravity.CENTER_HORIZONTAL
            }

        val publicKeyField =
            TextView(styledContext).apply {
                text = ttp.publicKey.toString()
                layoutParams = layoutParams(1f)
            }

        val registerAtTTPButton = Button(ContextThemeWrapper(context, R.style.Button)).apply {
            layoutParams = layoutParams(1f)
            text = "Register"
        }

        applyButtonStylingToAction(registerAtTTPButton, context)
        setTTPRegisterActionButton(registerAtTTPButton, context, user)

        tableRow.addView(ttpNameField)
        tableRow.addView(publicKeyField)
        tableRow.addView(registerAtTTPButton)
        return tableRow
    }

    // Function to add addresses to the tables in the User Fragment
    fun addAddressesToTable(
        table: LinearLayout,
        addresses: List<Address>,
        user: User,
        context: Context,
        onSendClick: (String) -> Unit       // Listener for clicking the send euro button
    ) {
        removeAllButFirstRow(table)

        for (address in addresses) {
            val row = when (address.type) {
                Role.Bank -> addressToBankAccountsTableRow(address, context, user)                     // Add row to Bank Accounts table
                Role.User -> addressToPeerTransactionsTableRow(address, context, user, onSendClick)   // Add row to Peer Transactions table
                Role.TTP -> addressToTTPTableRow(address, context, user)                             // Add row to TTP table
            }
            table.addView(row)
        }
    }

    private fun addressToBankAccountsTableRow(
        address: Address,
        context: Context,
        user: User
    ): LinearLayout {
        val tableRow = LinearLayout(context)
        tableRow.layoutParams = rowParams(context)
        tableRow.orientation = LinearLayout.HORIZONTAL
        val styledContext = ContextThemeWrapper(context, R.style.TableCell)

        val bankNameField =
            TextView(styledContext).apply {
                layoutParams = layoutParams(0.25f)
                text = address.name
                gravity = Gravity.CENTER
            }

        val balanceField =
            TextView(styledContext).apply {
                layoutParams = layoutParams(0.25f)
                text = user.getBalance().toString()      //todo: replace with the correct balance for each specific bank the user is registered at
                gravity = Gravity.CENTER
            }

        val depositContainer = LinearLayout(context).apply {
            layoutParams = layoutParams(0.25f)
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
        }

        val depositButton = ImageButton(context)
        applyButtonStylingToBankAction(depositButton, context)
        depositContainer.addView(depositButton)

        val withdrawContainer = LinearLayout(context).apply {
            layoutParams = layoutParams(0.25f)
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
        }

        val withdrawButton = ImageButton(context)
        applyButtonStylingToBankAction(withdrawButton, context)
        withdrawContainer.addView(withdrawButton)

        setBankActionButtons(depositButton, withdrawButton, address.name, user, context)

        tableRow.addView(bankNameField)
        tableRow.addView(balanceField)
        tableRow.addView(depositContainer)
        tableRow.addView(withdrawContainer)

        return tableRow
    }

    private fun addressToPeerTransactionsTableRow(
        address: Address,
        context: Context,
        user: User,
        onSendClick: (String) -> Unit
    ): LinearLayout {
        val tableRow = LinearLayout(context)
        tableRow.layoutParams = rowParams(context)
        tableRow.orientation = LinearLayout.HORIZONTAL
        val styledContext = ContextThemeWrapper(context, R.style.TableCell)

        val peerNameField =
            TextView(styledContext).apply {
                layoutParams = layoutParams(0.30f)
                text = address.name
                gravity = Gravity.CENTER
            }

        val sendEuroButton = Button(ContextThemeWrapper(context, R.style.Button)).apply {
            layoutParams = layoutParams(0.30f)
            text = "Send"
        }

        val doubleSpendButton = Button(ContextThemeWrapper(context, R.style.Button)).apply {
            layoutParams = layoutParams(0.35f).apply {
                (this as? LinearLayout.LayoutParams)?.marginStart = 2.dp(context)
            }
            text = "Double Spend"
        }

        applyButtonStylingToAction(sendEuroButton, context)
        applyButtonStylingToAction(doubleSpendButton, context)
        setUserActionButtons(sendEuroButton, doubleSpendButton, address.name, user, context, onSendClick)

        tableRow.addView(peerNameField)
        tableRow.addView(sendEuroButton)
        tableRow.addView(doubleSpendButton)

        return tableRow
    }

    private fun addressToTTPTableRow(
        address: Address,
        context: Context,
        user: User
    ): LinearLayout {
        val tableRow = LinearLayout(context)
        tableRow.layoutParams = rowParams(context)
        tableRow.orientation = LinearLayout.HORIZONTAL
        val styledContext = ContextThemeWrapper(context, R.style.TableCell)

        val ttpNameField =
            TextView(styledContext).apply {
                layoutParams = layoutParams(0.3f)
                text = address.name
                gravity = Gravity.CENTER
            }

        val ttpPKField =
            TextView(styledContext).apply {
                layoutParams = layoutParams(0.7f)
                text = address.publicKey.toString()
                gravity = Gravity.CENTER
            }

        tableRow.addView(ttpNameField)
        tableRow.addView(ttpPKField)

        return tableRow
    }

    private fun setBankActionButtons(
        depositButton: ImageButton,
        withdrawButton: ImageButton,
        bankName: String,
        user: User,
        context: Context
    ) {
        depositButton.setBackgroundResource(R.drawable.ic_baseline_outgoing_24)
        depositButton.contentDescription = "deposit"
        depositButton.setOnClickListener {
            try {
                val depositResult = user.sendDigitalEuroTo(bankName, "")

                Toast.makeText(context, depositResult, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        withdrawButton.setBackgroundResource(R.drawable.ic_baseline_incoming_24)
        withdrawButton.contentDescription = "withdraw"
        withdrawButton.setOnClickListener {
            try {
                val digitalEuro = user.withdrawDigitalEuro(bankName)
                Toast.makeText(context, "Successfully withdrawn ${digitalEuro.serialNumber}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUserActionButtons(
        sendEuroButton: Button,
        doubleSpendButton: Button,
        userName: String,
        user: User,
        context: Context,
        onSendClick: (String) -> Unit
    ) {
        sendEuroButton.setOnClickListener {
            onSendClick(userName)
        }

        doubleSpendButton.setOnClickListener {
            try {
                val result = user.doubleSpendDigitalEuroTo(userName, "")
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateTokens(
        tokenContainer: LinearLayout,
        user: User,
        context: Context,
        onSendClick: (Long, String) -> Unit,
        onDoubleSpendClick: (Long, String) -> Unit,
        onDepositClick: (Long, String) -> Unit
    ) {
        tokenContainer.removeAllViews()

        val tokens: List<WalletEntry> = user.getTokens()

        for (tokenList in tokens) {
            val amount : Long = tokenList.digitalEuro.amount
            val textLabel = "$amount €"


            val label = TextView(context).apply {
                text = textLabel
                textSize = 16f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 16.dp(context)
                }
            }

            val buttonRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val sendButton = Button(context).apply {
                text = "SEND"
                setOnClickListener {
                    onSendClick(amount, tokenList.digitalEuro.serialNumber)
                }
            }
            sendButton.isEnabled = true
            sendButton.isClickable = true

            val doubleSpendButton = Button(context).apply {
                text = "DOUBLE SPEND"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 8.dp(context)
                }
                setOnClickListener {
                    onDoubleSpendClick(amount, tokenList.digitalEuro.serialNumber)
                }
            }
            doubleSpendButton.isEnabled = true
            doubleSpendButton.isClickable = true

            val depositButton = Button(context).apply {
                text = "DEPOSIT"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 8.dp(context)
                }
                setOnClickListener {
                    onDepositClick(amount, tokenList.digitalEuro.serialNumber)
                }
            }
            depositButton.isEnabled = true
            depositButton.isClickable = true

            buttonRow.addView(sendButton)
            buttonRow.addView(doubleSpendButton)
            buttonRow.addView(depositButton)

            tokenContainer.addView(label)
            tokenContainer.addView(buttonRow)
        }
    }


    private fun setTTPRegisterActionButton(
        registerAtTTPButton: Button,
        context: Context,
        user: User
    ) {
        registerAtTTPButton.setOnClickListener {
            user.registerAtTTP()
        }
    }

    private fun setBankRegisterActionButton(
        registerAtBankButton: Button,
        context: Context
    ) {
        registerAtBankButton.setOnClickListener {
            //todo
        }
    }

    private fun layoutParams(weight: Float): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            weight
        )
    }

    private fun rowParams(context: Context): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 6.dp(context)
        }
    }

    private fun applyButtonStylingToBankAction(
        button: ImageButton,
        context: Context
    ) {
        button.apply {
            layoutParams = LinearLayout.LayoutParams(52.dp(context), 52.dp(context))
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.colorPrimary)
            isClickable = true
            isFocusable = true
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(8.dp(context), 8.dp(context), 8.dp(context), 8.dp(context))
        }
    }

    private fun applyButtonStylingToAction(
        button: Button,
        context: Context
    ) {
        button.apply {
            textSize = 14f
            setPadding(8.dp(context), 8.dp(context), 8.dp(context), 8.dp(context))
            backgroundTintList = ContextCompat.getColorStateList(context, R.color.colorPrimary)
            isClickable = true
            isFocusable = true
        }
    }

    private fun Int.dp(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}
