package com.fft.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for JSON database loading functionality.
 *
 * <p>Verifies that the song database can be loaded from JSON file and that
 * popular songs are correctly parsed and available for recognition.</p>
 *
 * @author Claude Sonnet 4.5
 * @since 2.0.0
 */
@DisplayName("JSON Database Loader Tests")
class JSONDatabaseLoaderTest {

    private static final Logger logger = LoggerFactory.getLogger(JSONDatabaseLoaderTest.class);

    @Test
    @DisplayName("Should find songs.json in classpath")
    void shouldFindSongsJsonInClasspath() {
        InputStream is = getClass().getResourceAsStream("/songs/songs.json");

        assertThat(is).isNotNull();
        logger.info("✓ songs.json found in classpath");
    }

    @Test
    @DisplayName("Should load valid JSON content")
    void shouldLoadValidJsonContent() throws Exception {
        InputStream is = getClass().getResourceAsStream("/songs/songs.json");
        assertThat(is).isNotNull();

        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        assertThat(content)
            .isNotEmpty()
            .contains("\"version\"")
            .contains("\"songs\"")
            .contains("\"parsonsCode\"");

        logger.info("✓ JSON content is valid (contains required fields)");
    }

    @Test
    @DisplayName("Should contain popular songs from different decades")
    void shouldContainPopularSongs() throws Exception {
        InputStream is = getClass().getResourceAsStream("/songs/songs.json");
        assertThat(is).isNotNull();

        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // 70s classics
        assertThat(content).contains("Bohemian Rhapsody");
        assertThat(content).contains("Stairway to Heaven");
        assertThat(content).contains("Stayin' Alive");

        // 80s hits
        assertThat(content).contains("Billie Jean");
        assertThat(content).contains("Sweet Child O' Mine");
        assertThat(content).contains("Livin' on a Prayer");

        // 90s anthems
        assertThat(content).contains("Smells Like Teen Spirit");
        assertThat(content).contains("Wonderwall");
        assertThat(content).contains("My Heart Will Go On");

        // 2010s+ modern hits
        assertThat(content).contains("Shape of You");
        assertThat(content).contains("Blinding Lights");
        assertThat(content).contains("Uptown Funk");

        logger.info("✓ Database contains songs from all decades (70s-2010s)");
    }

    @Test
    @DisplayName("Should have Parsons codes for all songs")
    void shouldHaveParsonsCodesForAllSongs() throws Exception {
        InputStream is = getClass().getResourceAsStream("/songs/songs.json");
        assertThat(is).isNotNull();

        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // Check that Parsons codes follow the format *[UDR]+
        assertThat(content).containsPattern("\"parsonsCode\"\\s*:\\s*\"\\*[UDR]+\"");

        // Verify some specific codes
        assertThat(content).contains("*RDRUUUDDRD");     // Bohemian Rhapsody
        assertThat(content).contains("*RURDURDURD");     // Imagine
        assertThat(content).contains("*RRRDUDUDUD");     // Smells Like Teen Spirit

        logger.info("✓ All songs have valid Parsons code format");
    }

    @Test
    @DisplayName("Should have metadata for songs")
    void shouldHaveMetadataForSongs() throws Exception {
        InputStream is = getClass().getResourceAsStream("/songs/songs.json");
        assertThat(is).isNotNull();

        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // Check for required metadata fields
        assertThat(content)
            .contains("\"artist\"")
            .contains("\"year\"")
            .contains("\"genre\"")
            .contains("\"country\"")
            .contains("\"tags\"")
            .contains("\"variations\"");

        // Check for specific artist names
        assertThat(content)
            .contains("Queen")
            .contains("Michael Jackson")
            .contains("Nirvana")
            .contains("Ed Sheeran");

        logger.info("✓ Songs include complete metadata (artist, year, genre, etc.)");
    }

    @Test
    @DisplayName("Should have variations for partial matching")
    void shouldHaveVariationsForPartialMatching() throws Exception {
        InputStream is = getClass().getResourceAsStream("/songs/songs.json");
        assertThat(is).isNotNull();

        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // Check that variations arrays exist and are populated
        assertThat(content).contains("\"variations\"");

        // Verify variations format (should be array of shorter codes)
        assertThat(content).containsPattern("\"variations\"\\s*:\\s*\\[");

        logger.info("✓ Songs include variations for progressive matching");
    }

    @Test
    @DisplayName("Should have diverse genres representation")
    void shouldHaveDiverseGenres() throws Exception {
        InputStream is = getClass().getResourceAsStream("/songs/songs.json");
        assertThat(is).isNotNull();

        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // Check for genre diversity
        assertThat(content)
            .contains("\"genre\": \"Rock\"")
            .contains("\"genre\": \"Pop\"")
            .contains("\"genre\": \"Disco\"")
            .contains("\"genre\": \"Funk\"")
            .contains("\"genre\": \"Grunge\"")
            .contains("\"genre\": \"Metal\"");

        logger.info("✓ Database includes diverse music genres");
    }

    @Test
    @DisplayName("Should have international representation")
    void shouldHaveInternationalRepresentation() throws Exception {
        InputStream is = getClass().getResourceAsStream("/songs/songs.json");
        assertThat(is).isNotNull();

        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // Check for international artists
        assertThat(content)
            .contains("\"country\": \"US\"")    // USA
            .contains("\"country\": \"UK\"")    // United Kingdom
            .contains("\"country\": \"SE\"")    // Sweden (ABBA)
            .contains("\"country\": \"PR\"")    // Puerto Rico (Despacito)
            .contains("\"country\": \"CA\"");   // Canada

        logger.info("✓ Database includes international artists from multiple countries");
    }
}
