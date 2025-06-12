package nl.tudelft.trustchain.offlineeuro.ui

import android.content.Context
import android.media.Image
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.marginTop
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
        depositedEuro: Pair<String, Boolean>,
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
                gravity = Gravity.CENTER_HORIZONTAL
            }

        val doubleSpendingField =
            TextView(styledContext).apply {
                text = depositedEuro.second.toString()
                layoutParams = layoutParams(1f)
                gravity = Gravity.CENTER_HORIZONTAL
            }

        tableRow.addView(serialNumberField)
        tableRow.addView(doubleSpendingField)

        return tableRow
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

    fun addAddressesToTable(
        table: LinearLayout,
        addresses: List<Address>,
        user: User,
        context: Context
    ) {
        removeAllButFirstRow(table)
        for (address in addresses) {
            table.addView(addressToTableRow(address, context, user))
        }
    }

    private fun addressToTableRow(
        address: Address,
        context: Context,
        user: User
    ): LinearLayout {
        val tableRow = LinearLayout(context)
        tableRow.layoutParams = rowParams(context)
        tableRow.orientation = LinearLayout.HORIZONTAL

        val styledContext = ContextThemeWrapper(context, R.style.TableCell)
        val nameField =
            TextView(styledContext).apply {
                layoutParams = layoutParams(0.5f)
                text = address.name
            }

        val roleField =
            TextView(styledContext).apply {
                layoutParams = layoutParams(0.2f)
                text = address.type.toString()
            }
        tableRow.addView(nameField)
        tableRow.addView(roleField)

        if (address.type == Role.TTP) {
            val actionField =
                TextView(context).apply {
                    layoutParams = layoutParams(0.8f)
                }
            tableRow.addView(actionField)
        } else {
            val buttonWrapper = LinearLayout(context)
            val params = layoutParams(0.8f)
            buttonWrapper.gravity = Gravity.CENTER_HORIZONTAL
            buttonWrapper.orientation = LinearLayout.HORIZONTAL
            buttonWrapper.layoutParams = params

            val mainActionButton = Button(context)
            val secondaryButton = Button(context)

            applyButtonStyling(mainActionButton, context)
            applyButtonStyling(secondaryButton, context)

            buttonWrapper.addView(mainActionButton)
            buttonWrapper.addView(secondaryButton)

            when (address.type) {
                Role.Bank -> {
                    setBankActionButtons(mainActionButton, secondaryButton, address.name, user, context)
                }
                Role.User -> {
                    setUserActionButtons(mainActionButton, secondaryButton, address.name, user, context)
                }

                else -> {}
            }

            tableRow.addView(buttonWrapper)
        }

        return tableRow
    }

//    private fun addressToPeerTransactionsTableRow(
//        address: Address,
//        context: Context,
//        user: User,
//        onSendClick: (String) -> Unit
//    ): LinearLayout {
//        val tableRow = LinearLayout(context)
//        tableRow.layoutParams = rowParams(context)
//        tableRow.orientation = LinearLayout.HORIZONTAL
//        val styledContext = ContextThemeWrapper(context, R.style.TableCell)
//
//        val peerNameField =
//            TextView(styledContext).apply {
//                layoutParams = layoutParams(0.30f)
//                text = address.name
//                gravity = Gravity.CENTER
//            }
//
//        val sendEuroButton = Button(ContextThemeWrapper(context, R.style.Button)).apply {
//            layoutParams = layoutParams(0.30f)
//            text = "Send"
//        }
//
//        val doubleSpendButton = Button(ContextThemeWrapper(context, R.style.Button)).apply {
//            layoutParams = layoutParams(0.35f).apply {
//                (this as? LinearLayout.LayoutParams)?.marginStart = 2.dp(context)
//            }
//            text = "Double Spend"
//        }
//
//        applyButtonStylingToAction(sendEuroButton, context)
//        applyButtonStylingToAction(doubleSpendButton, context)
//        setUserActionButtons(sendEuroButton, doubleSpendButton, address.name, user, context, onSendClick)
//
//        tableRow.addView(peerNameField)
//        tableRow.addView(sendEuroButton)
//        tableRow.addView(doubleSpendButton)
//
//        return tableRow
//    }

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

    fun setBankActionButtons(
        mainButton: Button,
        secondaryButton: Button,
        bankName: String,
        user: User,
        context: Context
    ) {
        mainButton.text = "Withdraw"
        mainButton.setOnClickListener {
            try {
                val digitalEuro = user.withdrawDigitalEuro(bankName)
                Toast.makeText(context, "Successfully withdrawn ${digitalEuro.serialNumber}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        secondaryButton.text = "Deposit"
        secondaryButton.setOnClickListener {
            try {
                val depositResult = user.sendDigitalEuroTo(bankName)

                Toast.makeText(context, depositResult, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setUserActionButtons(
        mainButton: Button,
        secondaryButton: Button,
        userName: String,
        user: User,
        context: Context
    ) {
        mainButton.text = "Send Euro"
        mainButton.setOnClickListener {
            try {
                val result = user.sendDigitalEuroTo(userName)
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        secondaryButton.text = "Double Spend"
        secondaryButton.setOnClickListener {
            try {
                Log.i("double spent", "7")
                val result = user.doubleSpendDigitalEuroTo(userName)
            } catch (e: Exception) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
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
    fun applyButtonStyling(
        button: Button,
        context: Context
    ) {
        button.setTextColor(context.getColor(R.color.white))
        button.background.setTint(context.resources.getColor(R.color.colorPrimary))
        button.isAllCaps = false
        button.textSize = 12f
        button.setPadding(14, 14, 14, 14)
        button.letterSpacing = 0f
    }
}
