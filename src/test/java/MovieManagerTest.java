import org.junit.jupiter.api.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MovieManager (Phase 2 requirements).
 */
public class MovieManagerTest {

    private MovieManager manager;

    @BeforeEach
    public void setUp() {
        manager = new MovieManager();
    }

    @AfterEach
    public void tearDown() {
        manager.clearAll();
    }

    @Test
    public void testAddMovie_success() {
        boolean added = manager.addMovie("TestMovie", "2020-01-01", 1, "Director A", 100, 7.5);
        assertTrue(added, "Movie should be added successfully");
        assertEquals(1, manager.getMovies().size());
    }

    @Test
    public void testAddMovie_invalidData() {
        // invalid date format should fail
        assertFalse(manager.addMovie("BadMovie", "01-01-2020", 1, "Director", 100, 7.0));
        assertEquals(0, manager.getMovies().size());
    }

    @Test
    public void testRemoveMovie_success() {
        manager.addMovie("RemMovie", "2020-01-01", 1, "Dir", 100, 7.0);
        assertTrue(manager.removeMovie("RemMovie"));
        assertEquals(0, manager.getMovies().size());
    }

    @Test
    public void testRemoveMovie_notFound() {
        assertFalse(manager.removeMovie("NoSuchMovie"));
    }

    @Test
    public void testUpdateMovie_success() {
        manager.addMovie("UpMovie", "2020-01-01", 1, "OldDir", 100, 7.0);
        boolean updated = manager.updateMovieByTitle("UpMovie", "director", "NewDir");
        assertTrue(updated);
        assertEquals("NewDir", manager.findMovieByTitle("UpMovie").getDirector());
    }

    @Test
    public void testCustomAction_averageRating() {
        manager.addMovie("A", "2020-01-01", 2, "D", 100, 7.0);
        manager.addMovie("B", "2020-01-02", 2, "D", 120, 9.0);
        double avg = manager.calculateAverageRating(2);
        assertEquals(8.0, avg, 0.0001);
    }

    @Test
    public void testLoadBatchData_fileOpenAndLoad() throws IOException {
        // create temporary file with two well-formed lines
        Path temp = Files.createTempFile("mm_sample", ".txt");
        String content = "T1,2000-01-01,1,Dir1,100,7.0\nT2,2001-01-01,1,Dir2,120,8.0\n";
        Files.writeString(temp, content);
        temp.toFile().deleteOnExit();

        String result = manager.loadBatchData(temp.toString());
        assertTrue(result.startsWith("Batch Load Complete"));
        // verify two movies were added
        assertEquals(2, manager.getMovies().size());
    }

    @Test
    public void testLoadBatchData_badFile() {
        String result = manager.loadBatchData("C:\\this\\path\\doesnotexist.txt");
        assertTrue(result.startsWith("ERROR: Unable to read file"));
    }
}
