package com.Jnx03.thaiflickkeyboard.model

data class KeyboardLayout(
    val name: String,
    val keys: List<FlickKey>,
    val columns: Int = 4,
    val description: String = ""
) {
    val rows: Int get() = (keys.size + columns - 1) / columns

    companion object {

        fun optimizedCenter(): KeyboardLayout {
            val keys = listOf(
                // Row 0 (top - hardest to reach)
                // (0,0) Special - rarely used chars
                FlickKey("special", "ๆ", "ฯ", "ฌ", "๋", "ฺ", "#64748b", "Special"),
                // (0,1) อ group - Glottal (5.75% total)
                FlickKey("k4", "อ", "ห", "ฮ", "ผ", "ฅ", "#22c55e", "อ Glottal"),
                // (0,2) ว group - Fricatives (6.02% total)
                FlickKey("k8", "ว", "ส", "ฟ", "ศ", "ษ", "#f59e0b", "ว Fricative"),
                // (0,3) Mode switch
                FlickKey("mode", "EN", "123", "TH", "", "", "#64748b", "Mode"),

                // Row 1 (middle-upper)
                // (1,0) Tone marks (6.17% total - ์ is most common diacritic)
                FlickKey("tone", "์", "่", "้", "็", "๊", "#a855f7", "Tones"),
                // (1,1) ม group - Labials (8.03% total)
                FlickKey("k6", "ม", "ป", "บ", "พ", "ภ", "#f59e0b", "ม Labial"),
                // (1,2) เ group - Leading vowels (7.93% total)
                FlickKey("v1", "เ", "แ", "ไ", "ใ", "โ", "#ec4899", "เ Leading"),
                // (1,3) ท group - Aspirated (3.52% total)
                FlickKey("k9", "ท", "ถ", "ธ", "ฐ", "ฝ", "#6366f1", "ท Aspirated"),

                // Row 2 (center - easiest to reach!)
                // (2,0) ก group - Velars (6.96% total)
                FlickKey("k3", "ก", "ค", "ข", "ฆ", "ฃ", "#6366f1", "ก Velar"),
                // (2,1) า group - After vowels (11.45% total - HEAVIEST key)
                FlickKey("v3", "า", "ะ", "ุ", "ู", "ำ", "#ec4899", "า After"),
                // (2,2) ั group - Above vowels (10.03% total)
                FlickKey("v2", "ั", "ี", "ิ", "ื", "ึ", "#ec4899", "ั Above"),
                // (2,3) น group - Nasals (6.93% total)
                FlickKey("k2", "น", "ณ", "ญ", "ฒ", "ฑ", "#22c55e", "น Nasal"),

                // Row 3 (bottom)
                // (3,0) ย group - Palatals (6.40% total)
                FlickKey("k7", "ย", "จ", "ช", "ซ", "ฉ", "#f59e0b", "ย Palatal"),
                // (3,1) ร group - Liquids (8.85% total)
                FlickKey("k1", "ร", "ล", "ฬ", "ฤ", "ฦ", "#22c55e", "ร Liquid"),
                // (3,2) ง group - Dental stops (8.21% total)
                FlickKey("k5", "ง", "ด", "ต", "ฎ", "ฏ", "#6366f1", "ง Dental"),
                // (3,3) Backspace
                FlickKey("backspace", "⌫", "", "", "", "", "#ef4444", "Delete")
            )
            return KeyboardLayout("Old", keys,
                description = "AI-optimized for both hands. Most-used chars in center, bigram-adjacent keys.")
        }

        fun emoji(): KeyboardLayout {
            val keys = listOf(
                // Row 0: Smileys, Gestures, Hearts
                FlickKey("em1", "😀", "😂", "🥹", "😍", "🤣", "#f59e0b", "Smileys"),
                FlickKey("em2", "👍", "👋", "🙏", "✌️", "👏", "#f59e0b", "Hands"),
                FlickKey("em3", "❤️", "💕", "💔", "🔥", "✨", "#ec4899", "Hearts"),
                FlickKey("tone", "😊", "😎", "🥰", "😇", "🤩", "#a855f7", "Happy"),

                // Row 1: Faces, Animals, Nature
                FlickKey("em4", "😢", "😭", "😤", "😡", "🥺", "#6366f1", "Sad"),
                FlickKey("em5", "🐱", "🐶", "🐻", "🐼", "🦊", "#22c55e", "Animals"),
                FlickKey("em6", "🌸", "🌺", "🌻", "🌹", "🍀", "#22c55e", "Nature"),
                FlickKey("special", "😅", "😜", "🤔", "😏", "🙄", "#64748b", "Misc"),

                // Row 2: Food, Activities, Objects
                FlickKey("em7", "🍕", "🍔", "🍣", "🍜", "☕", "#f59e0b", "Food"),
                FlickKey("em8", "⚽", "🎮", "🎵", "📸", "🎉", "#6366f1", "Fun"),
                FlickKey("em9", "💯", "⭐", "💪", "🎯", "🏆", "#6366f1", "Symbols"),
                FlickKey("mode", "TH", "", "", "", "", "#64748b", "Mode"),

                // Row 3: Weather, Travel, Misc
                FlickKey("em10", "☀️", "🌙", "⛅", "🌈", "❄️", "#22c55e", "Weather"),
                FlickKey("em11", "🏠", "✈️", "🚗", "🌍", "🏖️", "#ec4899", "Travel"),
                FlickKey("em12", "💬", "👀", "💡", "🔔", "📌", "#ec4899", "Misc"),
                FlickKey("backspace", "⌫", "", "", "", "", "#ef4444", "Delete")
            )
            return KeyboardLayout("Emoji", keys,
                description = "Emoji keyboard with flick input for variants.")
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

        fun thaiShift(): KeyboardLayout {
            val keys = listOf(
                // Row 0: Missing Thai chars + Thai numerals
                FlickKey("ts1", "์", "ฌ", "ฺ", "๋", "็", "#a855f7", "Missing"),
                FlickKey("ts2", "๑", "๒", "๓", "๔", "๕", "#6366f1", "Thai 1-5"),
                FlickKey("ts3", "๖", "๗", "๘", "๙", "๐", "#6366f1", "Thai 6-0"),

                // Row 1: Numbers
                FlickKey("ts4", "1", "2", "3", "4", "5", "#22c55e", "Num 1-5"),
                FlickKey("ts5", "6", "7", "8", "9", "0", "#22c55e", "Num 6-0"),
                FlickKey("ts6", "฿", "$", "€", "£", "¥", "#f59e0b", "Currency"),

                // Row 2: Punctuation
                FlickKey("ts7", ".", ",", "!", "?", ":", "#ec4899", "Punct"),
                FlickKey("ts8", "\"", "'", ";", "/", "\\", "#ec4899", "Punct2"),
                FlickKey("ts9", "(", ")", "[", "]", "-", "#ec4899", "Brackets"),

                // Row 3: Symbols
                FlickKey("ts10", "@", "#", "%", "&", "*", "#64748b", "Symbols"),
                FlickKey("ts11", "+", "=", "<", ">", "^", "#64748b", "Math"),
                FlickKey("ts12", "_", "~", "`", "{", "}", "#64748b", "More")
            )
            return KeyboardLayout("Thai Shift", keys)
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

        fun fromPresetName(name: String): KeyboardLayout {
            return optimizedCenter()
        }
    }
}
