# Frequency-Optimized Thai Flick Keyboard Layout

## Research Source

Character frequency data from **Aroonmanakun (2006)**, Department of Linguistics, Chulalongkorn University. Corpus of ~354 million Thai characters from mixed text sources.

Reference: Wirote Aroonmanakun, "List of Thai characters sorted by frequency", Faculty of Arts, Chulalongkorn University.

## Methodology

### Position Scoring

Each of the 12 character keys (3 columns x 4 rows) has a **grid score** based on ergonomic reach:

```
mic   | 40 (low)    | 60 (mid)    | 40 (low)    | delete
  <   | 80 (mid)    | 100 (HIGH)  | 80 (mid)    |   >
lang  | 40 (low)    | 60 (mid)    | 40 (low)    | space
shift | 20 (s.low)  | 20 (s.low)  | 20 (s.low)  | enter
```

Each key has **flick direction multipliers**:
- Center (tap): 1.0
- Up: 0.8
- Left / Right: 0.6
- Down: 0.4 (rows 0-2 only; row 3 has NO down flick)

**Final score = grid_score x flick_multiplier**

### Assignment Algorithm

Greedy: sort all 57 positions by descending score, sort all 57 characters by descending frequency, zip them 1:1.

### Tone Mark Exclusion

Tone marks ่ (mai ek), ้ (mai tho), ๊ (mai tri) are accessible via **space key flicks** and are excluded from the main character grid. This frees 3 slots for characters that would otherwise be on the shift page.

## Character Frequency Table

| Rank | Char | Name | Freq % | Position | Score |
|------|------|------|--------|----------|-------|
| 1 | า | Sara aa | 7.12 | (1,2) center | 100 |
| 2 | ร | Ro rua | 6.49 | (1,2) up | 80 |
| 3 | น | No nu | 6.08 | (1,1) center | 80 |
| 4 | ก | Ko kai | 4.42 | (1,3) center | 80 |
| 5 | อ | O ang | 3.96 | (1,1) up | 64 |
| 6 | เ | Sara e | 3.81 | (1,3) up | 64 |
| 7 | ง | Ngo ngu | 3.80 | (1,2) left | 60 |
| 8 | ั | Sara an | 3.28 | (1,2) right | 60 |
| 9 | ม | Mo ma | 3.24 | (0,2) center | 60 |
| 10 | ย | Yo yak | 3.22 | (2,2) center | 60 |
| 11 | ว | Wo waen | 2.65 | (1,1) left | 48 |
| 12 | ี | Sara ii | 2.64 | (1,1) right | 48 |
| 13 | ท | Tho thahan | 2.50 | (1,3) left | 48 |
| 14 | ด | Do chada | 2.32 | (1,3) right | 48 |
| 15 | ล | Lo ling | 2.29 | (0,2) up | 48 |
| 16 | ิ | Sara i | 2.06 | (2,2) up | 48 |
| 17 | ต | To tao | 1.99 | (1,2) down | 40 |
| 18 | ะ | Sara a | 1.81 | (0,1) center | 40 |
| 19 | ป | Po pla | 1.69 | (0,3) center | 40 |
| 20 | จ | Jo jan | 1.64 | (2,1) center | 40 |
| 21 | ค | Kho khwai | 1.62 | (2,3) center | 40 |
| 22 | ห | Ho hip | 1.55 | (0,2) left | 36 |
| 23 | ส | So suea | 1.51 | (0,2) right | 36 |
| 24 | แ | Sara ae | 1.29 | (2,2) left | 36 |
| 25 | ์ | Thanthakhat | 1.20 | (2,2) right | 36 |
| 26 | ไ | Sara ai malai | 1.20 | (1,1) down | 32 |
| 27 | พ | Pho phan | 1.14 | (1,3) down | 32 |
| 28 | ข | Kho khai | 1.14 | (0,1) up | 32 |
| 29 | ใ | Sara ai muan | 1.08 | (0,3) up | 32 |
| 30 | ช | Cho chang | 1.07 | (2,1) up | 32 |
| 31 | ื | Sara uue | 1.03 | (2,3) up | 32 |
| 32 | ุ | Sara u | 1.03 | (0,2) down | 24 |
| 33 | บ | Bo baimai | 0.97 | (2,2) down | 24 |
| 34 | ธ | Tho thong | 0.84 | (0,1) left | 24 |
| 35 | ผ | Pho phung | 0.80 | (0,1) right | 24 |
| 36 | ู | Sara uu | 0.77 | (0,3) left | 24 |
| 37 | ็ | Mai taikhu | 0.76 | (0,3) right | 24 |
| 38 | ำ | Sara am | 0.72 | (2,1) left | 24 |
| 39 | ณ | No nen | 0.70 | (2,1) right | 24 |
| 40 | ศ | So sala | 0.70 | (2,3) left | 24 |
| 41 | ถ | Tho thung | 0.65 | (2,3) right | 24 |
| 42 | โ | Sara o | 0.55 | (3,1) center | 20 |
| 43 | ซ | So so | 0.54 | (3,2) center | 20 |
| 44 | ษ | So ruesi | 0.53 | (3,3) center | 20 |
| 45 | ึ | Sara ue | 0.50 | (0,1) down | 16 |
| 46 | ฟ | Fo fan | 0.38 | (0,3) down | 16 |
| 47 | ญ | Yo ying | 0.35 | (2,1) down | 16 |
| 48 | ภ | Pho samphao | 0.33 | (2,3) down | 16 |
| 49 | ๆ | Mai yamok | 0.28 | (3,1) up | 16 |
| 50 | ฉ | Cho ching | 0.22 | (3,2) up | 16 |
| 51 | ฒ | Tho phuthao | 0.11 | (3,3) up | 16 |
| 52 | ฯ | Pai yan noi | 0.09 | (3,1) left | 12 |
| 53 | ฎ | Do chada | 0.09 | (3,1) right | 12 |
| 54 | ฏ | To patak | 0.07 | (3,2) left | 12 |
| 55 | ฐ | Tho than | 0.06 | (3,2) right | 12 |
| 56 | ฤ | Rue | 0.04 | (3,3) left | 12 |
| 57 | ฑ | Tho nangmon | 0.03 | (3,3) right | 12 |

## Final Layout (Main Page)

```
         Col 1 (low)      Col 2 (mid)      Col 3 (low)
Row 0    ┌─────────┐      ┌─────────┐      ┌─────────┐
         │   ข     │      │   ล     │      │   ใ     │
         │ ธ ะ ผ   │      │ ห ม ส   │      │ ู ป ็   │
         │   ึ     │      │   ุ     │      │   ฟ     │
         └─────────┘      └─────────┘      └─────────┘

         Col 1 (mid)      Col 2 (HIGH)     Col 3 (mid)
Row 1    ┌─────────┐      ┌─────────┐      ┌─────────┐
         │   อ     │      │   ร     │      │   เ     │
         │ ว น ี   │      │ ง า ั   │      │ ท ก ด   │
         │   ไ     │      │   ต     │      │   พ     │
         └─────────┘      └─────────┘      └─────────┘

         Col 1 (low)      Col 2 (mid)      Col 3 (low)
Row 2    ┌─────────┐      ┌─────────┐      ┌─────────┐
         │   ช     │      │   ิ     │      │   ื     │
         │ ำ จ ณ   │      │ แ ย ์   │      │ ศ ค ถ   │
         │   ญ     │      │   บ     │      │   ภ     │
         └─────────┘      └─────────┘      └─────────┘

         Col 1 (s.low)    Col 2 (s.low)    Col 3 (s.low)
Row 3    ┌─────────┐      ┌─────────┐      ┌─────────┐
         │   ๆ     │      │   ฉ     │      │   ฒ     │
         │ ฯ โ ฎ   │      │ ฏ ซ ฐ   │      │ ฤ ษ ฑ   │
         │ (none)  │      │ (none)  │      │ (none)  │
         └─────────┘      └─────────┘      └─────────┘
```

Key: center = tap, top = up, left = left-flick, right = right-flick, bottom = down-flick

## Tone Marks (Space Key Flicks)

| Direction | Character | Name | Freq % |
|-----------|-----------|------|--------|
| Left | ่ | Mai ek | 2.50 |
| Up | ้ | Mai tho | 1.80 |
| Right | ๊ | Mai tri | 0.15 |

## Shift Page (9 Super-Low Frequency Characters)

Characters accessible via shift key: ฆ (0.03%), ๋ (0.02%), ฺ (0.01%), ฌ (0.01%), ฬ (0.01%), ฮ (<0.01%), ฃ (~0%), ฅ (~0%), ฦ (~0%)

Plus numbers, Thai numerals, punctuation, currency, and symbols.

## Coverage

- **Main page**: 57 characters covering ~98.3% of all Thai text
- **Space key tones**: 3 tone marks covering ~4.45% of text
- **Shift page**: 9 rare characters covering ~0.1% of text
- **Total**: All 69 Thai characters + tone marks + diacritics
