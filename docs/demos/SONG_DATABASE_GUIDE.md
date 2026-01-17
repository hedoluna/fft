# 🎵 Song Database Guide

This guide explains how to add new songs to the recognition database.

## Overview

The song recognition system uses **Parsons Code** to identify melodies. The database can be managed in two ways:

1. **JSON Database** (Recommended) - External file, easy to update
2. **Embedded Database** - Hardcoded in Java, used as fallback

## JSON Database Structure

### Location
```
src/main/resources/songs/songs.json
```

### Schema
```json
{
  "version": "1.0",
  "description": "Song recognition database with Parsons codes",
  "lastUpdated": "2025-12-26",
  "songs": [
    {
      "title": "Song Name",
      "artist": "Artist Name",
      "year": 2020,
      "genre": "Genre",
      "country": "US",
      "parsonsCode": "*UDUDRR",
      "variations": ["*UDUD", "*DURR", "*UDUDR"],
      "tags": ["tag1", "tag2"]
    }
  ]
}
```

## Adding a New Song

### Step 1: Generate Parsons Code

**Parsons Code** represents melodic contour:
- `*` = Start of melody (always first)
- `U` = Up (pitch increases)
- `D` = Down (pitch decreases)
- `R` = Repeat (same pitch)

**Example: "Happy Birthday"**
```
Notes:  C  C  D  C  F  E
Pitch:  |  =  ↑  ↓  ↑  ↓
Code:   *  R  U  D  U  D
Result: *RUDUD
```

### Step 2: Create Variations

Variations help with progressive matching (as user sings):

```json
"parsonsCode": "*RRUURRDRRDDRR",
"variations": [
  "*RRUUR",        // First 5 notes
  "*RUURRDRR",     // Middle section
  "*RRUURRDR",     // Almost complete
  "*RRUURRD",      // Alternative ending
  "*RUURRDRRD"     // Another fragment
]
```

**Tips for variations:**
- Include first 4-5 notes (most distinctive)
- Add middle sections (bars/phrases)
- Include near-complete versions
- Total: 4-6 variations per song

### Step 3: Add Metadata

```json
{
  "title": "Shape of You",
  "artist": "Ed Sheeran",
  "year": 2017,
  "genre": "Pop",           // Rock, Pop, Jazz, Classical, etc.
  "country": "UK",          // ISO country code
  "parsonsCode": "*RRDRDRDURU",
  "variations": ["*RRDR", "*RDRDRD", "*RDRDR", "*RDRDRU", "*DRDURU"],
  "tags": ["pop", "2010s", "dance"]  // Searchable tags
}
```

### Step 4: Add to JSON File

1. Open `src/main/resources/songs/songs.json`
2. Add your song entry to the `"songs"` array
3. Update `"lastUpdated"` field
4. Save and rebuild: `mvn clean compile`

## Testing Your Song

### Option 1: Unit Test
```bash
mvn test -Dtest=JSONDatabaseLoaderTest
```

### Option 2: Live Recognition
```bash
mvn exec:java -Dexec.mainClass="com.fft.demo.RealTimeSongRecognitionDemo"
```

Then whistle/hum your song!

## Current Database Stats

- **Total Songs**: 47+ (17 embedded + 30+ JSON)
- **Decades Covered**: 1970s - 2020s
- **Genres**: Rock, Pop, Disco, Funk, Metal, Grunge, Reggaeton, Synth-pop
- **Countries**: US, UK, Italy, Sweden, Canada, Puerto Rico, Ireland

## JSON Database (30+ songs)

### By Decade

**1970s (6 songs)**
- Bohemian Rhapsody - Queen
- Imagine - John Lennon
- Stairway to Heaven - Led Zeppelin
- Hotel California - Eagles
- Stayin' Alive - Bee Gees
- Dancing Queen - ABBA

**1980s (8 songs)**
- Sweet Child O' Mine - Guns N' Roses
- Billie Jean - Michael Jackson
- Thriller - Michael Jackson
- Like a Prayer - Madonna
- Every Breath You Take - The Police
- Livin' on a Prayer - Bon Jovi
- Don't Stop Believin' - Journey
- Purple Rain - Prince

**1990s (6 songs)**
- Smells Like Teen Spirit - Nirvana
- Wonderwall - Oasis
- Enter Sandman - Metallica
- Wannabe - Spice Girls
- I Will Always Love You - Whitney Houston
- My Heart Will Go On - Celine Dion

**2010s-2020s (6 songs)**
- Shape of You - Ed Sheeran
- Despacito - Luis Fonsi
- Uptown Funk - Mark Ronson ft. Bruno Mars
- Radioactive - Imagine Dragons
- Rolling in the Deep - Adele
- Blinding Lights - The Weeknd

## Embedded Database (17 songs)

### International Classics (10 songs)
- Twinkle, Twinkle, Little Star
- Happy Birthday
- Ode to Joy (Beethoven)
- Amazing Grace
- Silent Night
- Jingle Bells
- London Bridge
- Row, Row, Row Your Boat
- Frère Jacques
- Mary Had a Little Lamb

### Italian Favorites (7 songs) 🇮🇹
- Bella Ciao (resistenza italiana)
- O Sole Mio (canzone napoletana)
- Funiculì Funiculà (canzone napoletana)
- Va' Pensiero (Nabucco - Verdi)
- La Donna è Mobile (Rigoletto - Verdi)
- Volare (Domenico Modugno)
- Santa Lucia (canzone napoletana)

## Advanced Features

### Merge Strategy

The system automatically merges JSON and embedded databases:
1. Loads JSON database first
2. If JSON fails/empty, uses embedded database
3. If JSON succeeds, merges with embedded (JSON takes priority)

### Fallback Behavior

If `songs.json` is not found:
```
WARN: songs.json not found in classpath, using embedded database
INFO: Using embedded song database
```

### Database Priority

For duplicate songs:
- **JSON version** takes priority
- Embedded version used only if not in JSON
- Example: If you add "Happy Birthday" to JSON, it overrides the embedded version

## Tools & Resources

### Online Melody Tools
- **Musipedia**: Search melodies by contour
- **The Melody Catcher**: Convert MIDI to Parsons
- **Online Sequencer**: Create melodies visually

### Parsons Code Generators
```python
def generate_parsons_code(notes):
    """Generate Parsons code from list of MIDI note numbers"""
    code = "*"
    for i in range(1, len(notes)):
        if notes[i] > notes[i-1]:
            code += "U"
        elif notes[i] < notes[i-1]:
            code += "D"
        else:
            code += "R"
    return code

# Example
notes = [60, 60, 62, 60, 65, 64]  # Happy Birthday
print(generate_parsons_code(notes))  # *RRUDUD
```

## Troubleshooting

### Song Not Recognized
1. **Check Parsons Code**: Verify first 5-7 notes
2. **Add More Variations**: Include common starting patterns
3. **Test Incrementally**: Hum just the first phrase
4. **Check Similarity**: Other songs may have same contour

### JSON Loading Fails
```bash
# Verify JSON syntax
cat src/main/resources/songs/songs.json | python -m json.tool

# Rebuild project
mvn clean compile

# Check Maven copied resources
ls target/classes/songs/songs.json
```

### Duplicate Keys
```json
// BAD: Same title causes overwrite
{"title": "Imagine", "artist": "John Lennon"}
{"title": "Imagine", "artist": "Ariana Grande"}

// GOOD: Append artist to title
{"title": "Imagine - John Lennon", ...}
{"title": "Imagine - Ariana Grande", ...}
```

## Contributing

Want to expand the database? Submit songs via:

1. **Pull Request**: Add to `songs.json`
2. **Issue**: Request specific songs
3. **Fork**: Maintain your own custom database

## Best Practices

### ✅ DO
- Use first 8-12 notes (most distinctive part)
- Include iconic/recognizable melodies
- Add 5-6 variations for better matching
- Test with real whistling/humming
- Update `lastUpdated` field

### ❌ DON'T
- Use entire song (too long, impractical)
- Copy Parsons code without verifying
- Add songs with very similar contours
- Forget to rebuild after changes
- Include copyright-protected content beyond metadata

## Examples

### Adding a Classic Rock Song

```json
{
  "title": "Smoke on the Water",
  "artist": "Deep Purple",
  "year": 1972,
  "genre": "Rock",
  "country": "UK",
  "parsonsCode": "*RDRDURDRDU",
  "variations": ["*RDRD", "*DRDUR", "*RDRDUR", "*DURDRD", "*RDURDRD"],
  "tags": ["rock", "70s", "guitar", "riff"]
}
```

### Adding a Pop Hit

```json
{
  "title": "Bad Guy",
  "artist": "Billie Eilish",
  "year": 2019,
  "genre": "Pop",
  "country": "US",
  "parsonsCode": "*DDDUUURDDD",
  "variations": ["*DDDU", "*DUUUR", "*DDDUUU", "*UUURDDD", "*UURDD"],
  "tags": ["pop", "2010s", "alternative"]
}
```

## Future Enhancements

Planned features:
- **Community Database**: Crowdsourced songs
- **Auto-Learning**: System learns new patterns
- **Confidence Tuning**: Adjust matching thresholds
- **Genre Filtering**: Search by genre/decade
- **Export/Import**: Share custom databases

---

**Happy song hunting!** 🎶

For questions or issues, see:
- `HOWTO_SONG_RECOGNITION.md` - Usage guide
- `CLAUDE.md` - Development guide
- GitHub Issues - Report bugs
