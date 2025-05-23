package nl.tudelft.trustchain.offlineeuro.ui

import android.content.Context
import android.media.Image
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import nl.tudelft.trustchain.offlineeuro.R
import nl.tudelft.trustchain.offlineeuro.entity.Address
import nl.tudelft.trustchain.offlineeuro.entity.Bank
import nl.tudelft.trustchain.offlineeuro.entity.RegisteredUser
import nl.tudelft.trustchain.offlineeuro.entity.User
import nl.tudelft.trustchain.offlineeuro.enums.Role

object TableHelpers {
    fun removeAllButFirstRow(table: LinearLayout) {
        val childrenCount = table.childCount
        val childrenToBeRemoved = childrenCount - 1

        for (i in childrenToBeRemoved downTo 1) {
            val row = table.getChildAt(i)
            table.removeView(row)
        }
    }

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
        val layout =
            LinearLayout(context).apply {
                layoutParams = rowParams()
                orientation = LinearLayout.HORIZONTAL
            }

        val idField =
            TextView(context).apply {
                text = user.id.toString()
                layoutParams = layoutParams(0.2f)
                gravity = Gravity.CENTER_HORIZONTAL
            }

        val nameField =
            TextView(context).apply {
                text = user.name
                layoutParams = layoutParams(0.2f)
                gravity = Gravity.CENTER_HORIZONTAL
            }

        val publicKeyField =
            TextView(context).apply {
                text = user.publicKey.toString()
                layoutParams = layoutParams(0.7f)
            }

        layout.addView(idField)
        layout.addView(nameField)
        layout.addView(publicKeyField)
        return layout
    }

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
        depositedEuro: Pair<String, Boolean>,
        context: Context
    ): LinearLayout {
        val layout =
            LinearLayout(context).apply {
                layoutParams = rowParams()
                orientation = LinearLayout.HORIZONTAL
            }

        val numberField =
            TextView(context).apply {
                text = depositedEuro.first
                layoutParams = layoutParams(0.7f)
                gravity = Gravity.CENTER_HORIZONTAL
            }

        val doubleSpendingField =
            TextView(context).apply {
                text = depositedEuro.second.toString()
                layoutParams = layoutParams(0.4f)
                gravity = Gravity.CENTER_HORIZONTAL
            }

        layout.addView(numberField)
        layout.addView(doubleSpendingField)
        return layout
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
        var isFirst = true
        for (address in addresses) {
            val row = when (address.type) {
                Role.Bank -> addressToBankAccountsTableRow(address, context, user)                     // Add row to Bank Accounts table
                Role.User -> addressToPeerTransactionsTableRow(address, context, user, onSendClick)   // Add row to Peer Transactions table
                Role.TTP -> addressToTTPTableRow(address, context, user)                             // Add row to TTP table
            }

            if (isFirst) {
                val params = LinearLayout.LayoutParams(row.layoutParams).apply {
                    topMargin = 6.dp(context)
                }
                row.layoutParams = params
                isFirst = false
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
        tableRow.layoutParams = rowParams()
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
        applyButtonStylingToBankActions(depositButton, context)
        depositContainer.addView(depositButton)

        val withdrawContainer = LinearLayout(context).apply {
            layoutParams = layoutParams(0.25f)
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
        }

        val withdrawButton = ImageButton(context)
        applyButtonStylingToBankActions(withdrawButton, context)
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
        tableRow.layoutParams = rowParams()
        tableRow.orientation = LinearLayout.HORIZONTAL
        val styledContext = ContextThemeWrapper(context, R.style.TableCell)

        val peerNameField =
            TextView(styledContext).apply {
                layoutParams = layoutParams(0.30f)
                text = address.name
                gravity = Gravity.CENTER
            }

        val sendEuroButton = Button(context).apply {
            layoutParams = layoutParams(0.30f)
            text = "Send"
        }

        val doubleSpendButton = Button(context).apply {
            layoutParams = layoutParams(0.35f).apply {
                (this as? LinearLayout.LayoutParams)?.marginStart = 2.dp(context)
            }
            text = "Double Spend"
        }

        applyButtonStylingToPeerActions(sendEuroButton, context)
        applyButtonStylingToPeerActions(doubleSpendButton, context)
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
        tableRow.layoutParams = rowParams()
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
                val depositResult = user.sendDigitalEuroTo(bankName)

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
                val result = user.doubleSpendDigitalEuroTo(userName)
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun layoutParams(weight: Float): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            weight
        )
    }

    private fun rowParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun applyButtonStylingToBankActions(
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

    private fun Int.dp(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    private fun applyButtonStylingToPeerActions(
        button: Button,
        context: Context
    ) {
        button.apply {
            textSize = 14f
            setPadding(8.dp(context), 8.dp(context), 8.dp(context), 8.dp(context))
            setTextColor(context.getColor(R.color.white))
            background.setTint(context.resources.getColor(R.color.colorPrimary))
            button.letterSpacing = 0f
            isClickable = true
            isFocusable = true
        }
    }
}
