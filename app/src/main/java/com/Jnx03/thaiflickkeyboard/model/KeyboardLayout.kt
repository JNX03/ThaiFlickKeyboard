package com.Jnx03.thaiflickkeyboard.model

data class KeyboardLayout(
    val name: String,
    val keys: List<FlickKey>,
    val columns: Int = 4,
    val description: String = ""
) {
    val rows: Int get() = (keys.size + columns - 1) / columns

    companion object {

        // ──────────────────────────────────────────────────
        // ORIGINAL PHONETIC LAYOUT (user's initial design)
        // Groups by phonetic category, not optimized for speed
        // ──────────────────────────────────────────────────
        fun default(): KeyboardLayout {
            val keys = listOf(
                FlickKey("k1", "น", "ม", "ง", "ณ", "ญ", "#22c55e", "Nasals"),
                FlickKey("k2", "ร", "ย", "ว", "ล", "ฬ", "#22c55e", "Sonorants"),
                FlickKey("k3", "ก", "ข", "ค", "ฆ", "ฃ", "#6366f1", "/k/ velar"),
                FlickKey("tone", "่", "้", "๊", "๋", "์", "#a855f7", "Tones"),

                FlickKey("k4", "อ", "ห", "ฮ", "ฅ", "ฤ", "#22c55e", "Glottal"),
                FlickKey("k5", "ด", "ต", "ฎ", "ฏ", "ฒ", "#6366f1", "/d,t/ dental"),
                FlickKey("k6", "ท", "ถ", "ธ", "ฐ", "ฑ", "#6366f1", "/tʰ/ aspirated"),
                FlickKey("special", "ฤ", "ฦ", "ๆ", "ฯ", "ฺ", "#a855f7", "Special"),

                FlickKey("k7", "ส", "ศ", "ษ", "ฟ", "ฝ", "#f59e0b", "Fricatives"),
                FlickKey("k8", "บ", "ป", "พ", "ผ", "ภ", "#f59e0b", "Labials"),
                FlickKey("k9", "จ", "ช", "ซ", "ฉ", "ฌ", "#f59e0b", "Palatals"),
                FlickKey("mode", "EN", "123", "TH", "", "", "#64748b", "Mode"),

                FlickKey("v1", "เ", "แ", "ไ", "ใ", "โ", "#ec4899", "สระนำ Leading"),
                FlickKey("v2", "ิ", "ี", "ึ", "ื", "ั", "#ec4899", "สระบน Above"),
                FlickKey("v3", "า", "ะ", "ุ", "ู", "ำ", "#ec4899", "สระหลัง After"),
                FlickKey("backspace", "⌫", "", "", "", "", "#ef4444", "Delete")
            )
            return KeyboardLayout("Phonetic (Original)", keys,
                description = "Groups by phonetic category. Easy to learn but not speed-optimized.")
        }

        // ──────────────────────────────────────────────────────────────────
        // OPTIMIZED CENTER LAYOUT (Both hands / Default optimized)
        //
        // Design principles (from corpus of 354M Thai characters):
        // 1. TAP = most frequent character per key (tap is 1.5-2x faster)
        // 2. Grid positions: center-bottom = easiest → top corners = hardest
        //    Tier 1: (2,1)(2,2)(3,1)(3,2)  Tier 2: (1,1)(1,2)(2,0)(2,3)
        //    Tier 3: (3,0)(3,3)(1,0)(1,3)  Tier 4: (0,0)(0,1)(0,2)(0,3)
        // 3. Flick priority: Tap > Up > Left ≈ Right > Down
        // 4. Common bigrams on adjacent keys (กา, าร, มา, เป, etc.)
        // 5. Phonetic grouping preserved within each key
        //
        // Frequency data (Chulalongkorn University, Aroonmanakun 2006):
        //   า 7.12%  ร 6.49%  น 6.08%  ก 4.42%  อ 3.96%  เ 3.81%
        //   ง 3.80%  ั 3.28%  ม 3.24%  ย 3.22%  ว 2.65%  ี 2.64%
        // ──────────────────────────────────────────────────────────────────
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
            return KeyboardLayout("Optimized (Center)", keys,
                description = "AI-optimized for both hands. Most-used chars in center, bigram-adjacent keys.")
        }

        // ──────────────────────────────────────────────────────────────────
        // OPTIMIZED RIGHT HAND LAYOUT
        //
        // Grid positions shifted: high-freq keys toward right columns
        // Flick priority: Tap > Up > Left(outward) > Down > Right(inward)
        // Right thumb arc: bottom-right center is easiest
        // ──────────────────────────────────────────────────────────────────
        fun optimizedRightHand(): KeyboardLayout {
            val keys = listOf(
                // Row 0 (top - hardest, put least-used)
                FlickKey("special", "ๆ", "ฯ", "ฌ", "๋", "ฺ", "#64748b", "Special"),
                FlickKey("k9", "ท", "ถ", "ธ", "ฝ", "ฐ", "#6366f1", "ท Aspirated"),
                FlickKey("k4", "อ", "ห", "ฮ", "ฅ", "ผ", "#22c55e", "อ Glottal"),
                FlickKey("mode", "EN", "123", "TH", "", "", "#64748b", "Mode"),

                // Row 1 (middle - right cols easier for right thumb)
                FlickKey("k8", "ว", "ส", "ฟ", "ษ", "ศ", "#f59e0b", "ว Fricative"),
                FlickKey("v1", "เ", "แ", "ไ", "โ", "ใ", "#ec4899", "เ Leading"),
                FlickKey("k6", "ม", "ป", "บ", "ภ", "พ", "#f59e0b", "ม Labial"),
                FlickKey("tone", "์", "่", "้", "๊", "็", "#a855f7", "Tones"),

                // Row 2 (center-bottom - easiest for right hand)
                FlickKey("k2", "น", "ณ", "ญ", "ฑ", "ฒ", "#22c55e", "น Nasal"),
                FlickKey("v2", "ั", "ี", "ิ", "ึ", "ื", "#ec4899", "ั Above"),
                FlickKey("v3", "า", "ะ", "ุ", "ำ", "ู", "#ec4899", "า After"),
                FlickKey("k3", "ก", "ค", "ข", "ฃ", "ฆ", "#6366f1", "ก Velar"),

                // Row 3 (bottom)
                FlickKey("k7", "ย", "จ", "ช", "ฉ", "ซ", "#f59e0b", "ย Palatal"),
                FlickKey("k5", "ง", "ด", "ต", "ฏ", "ฎ", "#6366f1", "ง Dental"),
                FlickKey("k1", "ร", "ล", "ฬ", "ฦ", "ฤ", "#22c55e", "ร Liquid"),
                FlickKey("backspace", "⌫", "", "", "", "", "#ef4444", "Delete")
            )
            return KeyboardLayout("Optimized (Right Hand)", keys,
                description = "One-handed right. High-freq keys shifted right, outward flick = left.")
        }

        // ──────────────────────────────────────────────────────────────────
        // OPTIMIZED LEFT HAND LAYOUT
        //
        // Mirror of right-hand: high-freq keys toward left columns
        // Flick priority: Tap > Up > Right(outward) > Down > Left(inward)
        // Left thumb arc: bottom-left center is easiest
        // ──────────────────────────────────────────────────────────────────
        fun optimizedLeftHand(): KeyboardLayout {
            val keys = listOf(
                // Row 0 (top - hardest)
                FlickKey("mode", "EN", "123", "TH", "", "", "#64748b", "Mode"),
                FlickKey("k4", "อ", "ห", "ผ", "ฅ", "ฮ", "#22c55e", "อ Glottal"),
                FlickKey("k9", "ท", "ถ", "ฐ", "ฝ", "ธ", "#6366f1", "ท Aspirated"),
                FlickKey("special", "ๆ", "ฯ", "๋", "ฺ", "ฌ", "#64748b", "Special"),

                // Row 1 (middle - left cols easier for left thumb)
                FlickKey("tone", "์", "่", "็", "๊", "้", "#a855f7", "Tones"),
                FlickKey("k6", "ม", "ป", "พ", "ภ", "บ", "#f59e0b", "ม Labial"),
                FlickKey("v1", "เ", "แ", "ใ", "โ", "ไ", "#ec4899", "เ Leading"),
                FlickKey("k8", "ว", "ส", "ศ", "ษ", "ฟ", "#f59e0b", "ว Fricative"),

                // Row 2 (center-bottom - easiest for left hand)
                FlickKey("k3", "ก", "ค", "ฆ", "ฃ", "ข", "#6366f1", "ก Velar"),
                FlickKey("v3", "า", "ะ", "ู", "ำ", "ุ", "#ec4899", "า After"),
                FlickKey("v2", "ั", "ี", "ื", "ึ", "ิ", "#ec4899", "ั Above"),
                FlickKey("k2", "น", "ณ", "ฒ", "ฑ", "ญ", "#22c55e", "น Nasal"),

                // Row 3 (bottom)
                FlickKey("backspace", "⌫", "", "", "", "", "#ef4444", "Delete"),
                FlickKey("k1", "ร", "ล", "ฤ", "ฦ", "ฬ", "#22c55e", "ร Liquid"),
                FlickKey("k5", "ง", "ด", "ฎ", "ฏ", "ต", "#6366f1", "ง Dental"),
                FlickKey("k7", "ย", "จ", "ซ", "ฉ", "ช", "#f59e0b", "ย Palatal")
            )
            return KeyboardLayout("Optimized (Left Hand)", keys,
                description = "One-handed left. High-freq keys shifted left, outward flick = right.")
        }

        // ──────────────────────────────────────────────────────────────────
        // SPEED LAYOUT - Pure frequency optimization, no phonetic grouping
        //
        // Every character position is strictly assigned by:
        // 1. Grid position ease × flick direction ease = total ease score
        // 2. Character frequency = total need score
        // 3. Highest-need character → highest-ease position
        //
        // This may feel unintuitive but is mathematically fastest.
        // ──────────────────────────────────────────────────────────────────
        fun speedOptimized(): KeyboardLayout {
            val keys = listOf(
                // Row 0 (hardest positions - rarest chars)
                FlickKey("special", "ๆ", "ฯ", "ฌ", "๋", "ฺ", "#64748b", "Special"),
                FlickKey("s1", "ซ", "ฎ", "ฉ", "ฏ", "ฮ", "#64748b", "Rare 1"),
                FlickKey("s2", "ษ", "ฝ", "ฒ", "ฑ", "ฃ", "#64748b", "Rare 2"),
                FlickKey("mode", "EN", "123", "TH", "", "", "#64748b", "Mode"),

                // Row 1 (mid-freq chars at Tier 2 positions)
                FlickKey("tone", "์", "่", "้", "็", "๊", "#a855f7", "Tones"),
                // ม at tap, then next most common consonants as flicks
                FlickKey("s3", "ม", "ด", "จ", "ช", "ถ", "#22c55e", "ม+Common"),
                // เ leads, other leading vowels follow
                FlickKey("v1", "เ", "แ", "ไ", "ใ", "โ", "#ec4899", "เ Leading"),
                // อ + other mid-freq consonants
                FlickKey("s4", "อ", "ห", "ข", "ธ", "ผ", "#22c55e", "อ+Mid"),

                // Row 2 (Tier 1 - easiest positions, highest freq chars)
                // ก + high-freq consonants
                FlickKey("s5", "ก", "ค", "ป", "บ", "ภ", "#6366f1", "ก+Freq"),
                // า - heaviest vowel key
                FlickKey("v3", "า", "ะ", "ุ", "ู", "ำ", "#ec4899", "า After"),
                // ั - above vowels
                FlickKey("v2", "ั", "ี", "ิ", "ื", "ึ", "#ec4899", "ั Above"),
                // น + related
                FlickKey("s6", "น", "ล", "ต", "ณ", "ญ", "#22c55e", "น+Freq"),

                // Row 3 (Tier 1-2)
                // ย + palatals
                FlickKey("s7", "ย", "ว", "พ", "ฟ", "ศ", "#f59e0b", "ย+Freq"),
                // ร - second most common consonant
                FlickKey("s8", "ร", "ส", "ฬ", "ฤ", "ฅ", "#22c55e", "ร+Freq"),
                // ง + common dental
                FlickKey("s9", "ง", "ท", "ฆ", "ฦ", "ฬ", "#6366f1", "ง+Freq"),
                FlickKey("backspace", "⌫", "", "", "", "", "#ef4444", "Delete")
            )
            return KeyboardLayout("Speed (Experimental)", keys,
                description = "Pure frequency optimization. Fastest possible but harder to learn.")
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

        fun allPresets(): List<KeyboardLayout> = listOf(
            optimizedCenter(),
            optimizedRightHand(),
            optimizedLeftHand(),
            default(),
            speedOptimized()
        )

        fun presetNames(): List<String> = allPresets().map { it.name }

        fun fromPresetName(name: String): KeyboardLayout {
            return allPresets().find { it.name == name } ?: optimizedCenter()
        }
    }
}
