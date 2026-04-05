package com.Jnx03.thaiflickkeyboard.model

data class KeyboardLayout(
    val name: String,
    val keys: List<FlickKey>,
    val columns: Int = 4
) {
    val rows: Int get() = (keys.size + columns - 1) / columns

    companion object {
        fun default(): KeyboardLayout {
            val keys = listOf(
                // Row 0: Consonant group 1
                FlickKey("k1", "น", "ม", "ง", "ณ", "ญ", "#22c55e", "Nasals"),
                FlickKey("k2", "ร", "ย", "ว", "ล", "ฬ", "#22c55e", "Sonorants"),
                FlickKey("k3", "ก", "ข", "ค", "ฆ", "ฃ", "#6366f1", "/k/ velar"),
                FlickKey("tone", "่", "้", "๊", "๋", "์", "#a855f7", "Tones"),

                // Row 1: Consonant group 2
                FlickKey("k4", "อ", "ห", "ฮ", "ฅ", "ฤ", "#22c55e", "Glottal"),
                FlickKey("k5", "ด", "ต", "ฎ", "ฏ", "ฒ", "#6366f1", "/d,t/ dental"),
                FlickKey("k6", "ท", "ถ", "ธ", "ฐ", "ฑ", "#6366f1", "/tʰ/ aspirated"),
                FlickKey("special", "ฤ", "ฦ", "ๆ", "ฯ", "ฺ", "#a855f7", "Special"),

                // Row 2: Consonant group 3
                FlickKey("k7", "ส", "ศ", "ษ", "ฟ", "ฝ", "#f59e0b", "Fricatives"),
                FlickKey("k8", "บ", "ป", "พ", "ผ", "ภ", "#f59e0b", "Labials"),
                FlickKey("k9", "จ", "ช", "ซ", "ฉ", "ฌ", "#f59e0b", "Palatals"),
                FlickKey("mode", "EN", "123", "TH", "", "", "#64748b", "Mode"),

                // Row 3: Vowels
                FlickKey("v1", "เ", "แ", "ไ", "ใ", "โ", "#ec4899", "สระนำ Leading"),
                FlickKey("v2", "ิ", "ี", "ึ", "ื", "ั", "#ec4899", "สระบน Above"),
                FlickKey("v3", "า", "ะ", "ุ", "ู", "ำ", "#ec4899", "สระหลัง After"),
                FlickKey("backspace", "⌫", "", "", "", "", "#ef4444", "Delete")
            )
            return KeyboardLayout("Default Thai", keys)
        }

        fun english(): KeyboardLayout {
            val keys = listOf(
                FlickKey("e1", "a", "b", "c", "d", "e", "#6366f1", "a-e"),
                FlickKey("e2", "f", "g", "h", "i", "j", "#6366f1", "f-j"),
                FlickKey("e3", "k", "l", "m", "n", "o", "#6366f1", "k-o"),
                FlickKey("tone", ".", ",", "!", "?", "'", "#a855f7", "Punct"),

                FlickKey("e4", "p", "q", "r", "s", "t", "#22c55e", "p-t"),
                FlickKey("e5", "u", "v", "w", "x", "y", "#22c55e", "u-y"),
                FlickKey("e6", "z", "@", "#", "&", "*", "#22c55e", "z+sym"),
                FlickKey("special", "(", ")", "[", "]", "-", "#a855f7", "Brackets"),

                FlickKey("e7", "1", "2", "3", "4", "5", "#f59e0b", "1-5"),
                FlickKey("e8", "6", "7", "8", "9", "0", "#f59e0b", "6-0"),
                FlickKey("e9", "+", "=", "/", "\\", "%", "#f59e0b", "Math"),
                FlickKey("mode", "TH", "123", "EN", "", "", "#64748b", "Mode"),

                FlickKey("e10", "\"", ";", ":", "_", "~", "#ec4899", "Punct2"),
                FlickKey("e11", "<", ">", "{", "}", "|", "#ec4899", "Bracket2"),
                FlickKey("e12", "^", "`", "$", "€", "£", "#ec4899", "Currency"),
                FlickKey("backspace", "⌫", "", "", "", "", "#ef4444", "Delete")
            )
            return KeyboardLayout("English", keys)
        }

        fun numbers(): KeyboardLayout {
            val keys = listOf(
                FlickKey("n1", "1", "", "", "", "", "#6366f1", ""),
                FlickKey("n2", "2", "", "", "", "", "#6366f1", ""),
                FlickKey("n3", "3", "", "", "", "", "#6366f1", ""),
                FlickKey("tone", "+", "-", "×", "÷", "=", "#a855f7", "Math"),

                FlickKey("n4", "4", "", "", "", "", "#22c55e", ""),
                FlickKey("n5", "5", "", "", "", "", "#22c55e", ""),
                FlickKey("n6", "6", "", "", "", "", "#22c55e", ""),
                FlickKey("special", ".", ",", "%", "#", "@", "#a855f7", "Symbols"),

                FlickKey("n7", "7", "", "", "", "", "#f59e0b", ""),
                FlickKey("n8", "8", "", "", "", "", "#f59e0b", ""),
                FlickKey("n9", "9", "", "", "", "", "#f59e0b", ""),
                FlickKey("mode", "TH", "EN", "123", "", "", "#64748b", "Mode"),

                FlickKey("n0", "0", "", "", "", "", "#ec4899", ""),
                FlickKey("nstar", "*", "", "", "", "", "#ec4899", ""),
                FlickKey("nhash", "#", "", "", "", "", "#ec4899", ""),
                FlickKey("backspace", "⌫", "", "", "", "", "#ef4444", "Delete")
            )
            return KeyboardLayout("Numbers", keys)
        }
    }
}
