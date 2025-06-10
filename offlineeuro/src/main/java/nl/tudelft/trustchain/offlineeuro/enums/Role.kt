package nl.tudelft.trustchain.offlineeuro.enums

enum class Role {
    TTP,
    Bank,
    User,
    DualRole;

    companion object {
        fun fromLong(value: Long): Role {
            return entries.find { it.ordinal == value.toInt() }!!
        }
    }
}
