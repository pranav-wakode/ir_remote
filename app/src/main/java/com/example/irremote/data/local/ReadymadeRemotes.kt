package com.example.irremote.data.local

import com.example.irremote.data.local.entity.RemoteButton

object ReadymadeRemotes {

    data class Template(
        val name: String,
        val type: String,
        val buttons: List<ButtonDef>
    )

    data class ButtonDef(
        val label: String,
        val address: String,
        val command: String
    )

    val templates = listOf(
        Template(
            name = "Samsung TV",
            type = "TV",
            buttons = listOf(
                ButtonDef("POWER", "0707", "02"),
                ButtonDef("VOL+", "0707", "07"),
                ButtonDef("VOL-", "0707", "0B"),
                ButtonDef("CH+", "0707", "12"),
                ButtonDef("CH-", "0707", "10"),
                ButtonDef("MUTE", "0707", "0F"),
                ButtonDef("UP", "0707", "60"),
                ButtonDef("DOWN", "0707", "61"),
                ButtonDef("LEFT", "0707", "65"),
                ButtonDef("RIGHT", "0707", "62"),
                ButtonDef("OK", "0707", "68"),
                ButtonDef("HOME", "0707", "79"),
                ButtonDef("BACK", "0707", "58")
            )
        ),
        Template(
            name = "LG TV",
            type = "TV",
            buttons = listOf(
                ButtonDef("POWER", "20DF", "10"),
                ButtonDef("VOL+", "20DF", "40"),
                ButtonDef("VOL-", "20DF", "C0"),
                ButtonDef("CH+", "20DF", "00"),
                ButtonDef("CH-", "20DF", "80"),
                ButtonDef("MUTE", "20DF", "90"),
                ButtonDef("UP", "20DF", "02"),
                ButtonDef("DOWN", "20DF", "82"),
                ButtonDef("LEFT", "20DF", "DF"),
                ButtonDef("RIGHT", "20DF", "5F"),
                ButtonDef("OK", "20DF", "22"),
                ButtonDef("HOME", "20DF", "C2"),
                ButtonDef("BACK", "20DF", "14")
            )
        )
    )
}