package com.shade.app.crypto

import cash.z.ecc.android.bip39.Mnemonics

class MnemonicManager {
    fun generateMnemonic(): List<String> {
        val mnemonicCode = Mnemonics.MnemonicCode(Mnemonics.WordCount.COUNT_12)
        return mnemonicCode.words.map { String(it) }
    }
}