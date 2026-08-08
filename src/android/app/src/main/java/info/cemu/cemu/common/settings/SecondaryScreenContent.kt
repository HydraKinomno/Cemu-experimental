package info.cemu.cemu.common.settings

enum class SecondaryScreenContent {
    GAMEPAD,
    TV;

    fun isTV() = this == TV
}
