# 🎵 How to Use Real-Time Song Recognition

## Quick Start

**1. Run the demo:**
```bash
mvn exec:java -Dexec.mainClass="com.fft.demo.RealTimeSongRecognitionDemo"
```

**2. Whistle or hum a melody** into your microphone

**3. Watch it recognize the song!**

## Example Output

```
=== Real-Time Song Recognition from Microphone ===
Whistle, hum, or sing a melody...

Microphone started. Listening...
(Press Ctrl+C to stop)

Note detected: C4 (261.6 Hz)
Note detected: C4 (261.6 Hz)
Note detected: G4 (392.0 Hz)

--- After 3 notes: Parsons code = *RU ---
No matches found yet. Keep singing...

Note detected: G4 (392.0 Hz)
Note detected: A4 (440.0 Hz)

--- After 5 notes: Parsons code = *RRUUR ---
Top matches:
  1. Twinkle, Twinkle, Little Star - 75% [===============     ]
  2. Happy Birthday - 45% [=========           ]

Note detected: A4 (440.0 Hz)
Note detected: G4 (392.0 Hz)

--- After 7 notes: Parsons code = *RRUURRDR ---
Top matches:
  1. Twinkle, Twinkle, Little Star - 89% [==================  ]
  2. Mary Had a Little Lamb - 52% [==========          ]
```

## Supported Songs

The demo recognizes **47+ famous melodies** from multiple decades and genres!

> 💡 **Want to add more songs?** See `SONG_DATABASE_GUIDE.md` for instructions on expanding the database with your favorite melodies.

### International Classics
- ⭐ Twinkle, Twinkle, Little Star
- 🎂 Happy Birthday
- 🎵 Ode to Joy (Beethoven)
- 🙏 Amazing Grace
- 🎄 Silent Night
- 🔔 Jingle Bells
- 🌉 London Bridge
- 🚣 Row, Row, Row Your Boat
- 🇫🇷 Frère Jacques
- 🐑 Mary Had a Little Lamb

### Italian Favorites 🇮🇹
- 🎗️ Bella Ciao (resistenza italiana)
- ☀️ O Sole Mio (canzone napoletana)
- 🚡 Funiculì Funiculà (canzone napoletana)
- 🎭 Va' Pensiero (Nabucco - Verdi)
- 🎪 La Donna è Mobile (Rigoletto - Verdi)
- ✈️ Volare (Nel blu dipinto di blu - Domenico Modugno)
- ⛵ Santa Lucia (canzone napoletana)

### Modern Hits & Classics (30+ songs from JSON database) 🎸
**70s-80s:**
- Bohemian Rhapsody - Queen
- Stairway to Heaven - Led Zeppelin
- Hotel California - Eagles
- Billie Jean - Michael Jackson
- Sweet Child O' Mine - Guns N' Roses
- Every Breath You Take - The Police
- Purple Rain - Prince
- ...and more!

**90s-2000s:**
- Smells Like Teen Spirit - Nirvana
- Wonderwall - Oasis
- My Heart Will Go On - Celine Dion
- Wannabe - Spice Girls
- ...and more!

**2010s-2020s:**
- Shape of You - Ed Sheeran
- Uptown Funk - Mark Ronson
- Blinding Lights - The Weeknd
- Rolling in the Deep - Adele
- Despacito - Luis Fonsi
- ...and more!

> 📄 Full list available in `SONG_DATABASE_GUIDE.md`

## Tips for Best Results

### 1. **Sing/Whistle Clearly**
- Use a consistent pitch for each note
- Hold each note for at least 0.5 seconds
- Leave small gaps between notes

### 2. **Microphone Setup**
- Position microphone 10-20 cm from your mouth
- Reduce background noise
- Adjust microphone sensitivity if needed

### 3. **What to Sing**
- **Start from the beginning** of the melody for best accuracy
- Sing at least **5-7 notes** before expecting recognition
- The more notes you sing, the higher the confidence

### 4. **Accuracy Expectations**
- **After 3 notes**: Initial detection (often multiple candidates)
- **After 5-7 notes**: Usually identifies the correct song (70-80% confidence)
- **After 10+ notes**: High confidence (85-95%)

## How It Works

```
Microphone → Audio Samples → FFT Analysis → Pitch Detection → Note Detection
                ↓
         Parsons Code (*UDUDRR...) → Database Matching → Song Recognition
```

### Algorithm Details

1. **Audio Capture**: 44.1 kHz, 16-bit mono audio from microphone
2. **Pitch Detection**: Spectral FFT method (0.92% error rate)
3. **Note Tracking**: Detects pitch changes with median filtering
4. **Parsons Code**: Converts pitch sequence to U/D/R (Up/Down/Repeat)
5. **Matching**: Compares against database using fuzzy matching
6. **Confidence**: Based on exact match, partial match, and similarity

### Parsons Code Example

```
Twinkle, Twinkle, Little Star:
C C G G A A G  →  *RRUURRDR
^ Same pitch (R)
  ^ Pitch goes up (U)
```

## Troubleshooting

### "Microphone not supported on this system"
- Check that you have a working microphone connected
- Verify microphone permissions in your OS settings
- Try a different audio input device

### "No matches found yet"
- Sing more notes (need at least 3-5 for initial matching)
- Sing more clearly with distinct pitches
- Try a song from the supported list

### Low Confidence Scores
- Sing more of the melody (10+ notes recommended)
- Maintain consistent tempo and pitch
- Reduce background noise
- Try whistling instead of humming (clearer pitch)

### Wrong Song Detected
- Some songs have similar beginnings - keep singing!
- The more notes you provide, the more accurate the match
- Check if you're singing the correct starting note

## Technical Specifications

- **Sample Rate**: 44.1 kHz
- **FFT Size**: 4096 points
- **Frequency Range**: 80 Hz - 2000 Hz
- **Pitch Accuracy**: 0.92% error (spectral method)
- **Processing Latency**: ~100ms
- **Recognition Speed**: Real-time (updates every 2 notes)
- **Database Size**: 10+ songs with variations
- **Match Accuracy**: 60-80% with partial melodies, 90%+ with complete melodies

## Advanced Features

### Progressive Matching
The system updates its guess as you sing:
- **After 3 notes**: Initial candidates
- **After 5 notes**: Refined list
- **After 7+ notes**: High confidence match

### Fuzzy Matching
- Tolerates slight pitch variations (±3%)
- Handles tempo differences
- Works with partial melodies
- Recognizes melody fragments anywhere in the song

### Confidence Scoring
- **90-100%**: Exact match
- **70-90%**: Very likely match
- **50-70%**: Possible match
- **30-50%**: Low confidence
- **<30%**: Not displayed

## Exit

Press **Ctrl+C** to stop the recognition.

## Next Steps

- Try all the songs in the database
- See if you can stump it with variations
- Check out other demos: `PitchDetectionDemo`, `ChordRecognitionDemo`

---

**Have fun recognizing melodies!** 🎵
