package crest.siamese;

import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Confirms the processedFilesLog config property is read into Siamese's field correctly.
 * Kept separate from SiameseTest (which is @Ignore'd because it needs a live Elasticsearch
 * connection via execute()): constructing Siamese and reading its config does not touch
 * Elasticsearch, so this can run as a normal, always-on test. The actual file-writing
 * behavior driven by this field is covered independently in ProcessedFilesTrackerTest,
 * since insert() (which uses it) is private and ES-dependent.
 */
public class SiameseProcessedFilesLogConfigTest {

    @Test
    public void processedFilesLogIsReadFromConfigWhenSet() throws NoSuchFieldException, IllegalAccessException {
        ClassLoader classLoader = getClass().getClassLoader();
        File configFile = new File(Objects.requireNonNull(
                classLoader.getResource("crest/siamese/config_test_processedFilesLog.properties")).getFile());

        Siamese siamese = new Siamese(configFile.getAbsolutePath());

        Field field = Siamese.class.getDeclaredField("processedFilesLog");
        field.setAccessible(true);
        String actual = (String) field.get(siamese);

        assertEquals("/Users/chaiyong/Downloads/matcha/1_matcha/processed_javascript_files.txt", actual);
    }

    @Test
    public void processedFilesLogIsNullWhenNotSetInConfig() throws NoSuchFieldException, IllegalAccessException {
        ClassLoader classLoader = getClass().getClassLoader();
        File configFile = new File(Objects.requireNonNull(
                classLoader.getResource("crest/siamese/config_test_processedFilesLog_disabled.properties")).getFile());

        Siamese siamese = new Siamese(configFile.getAbsolutePath());

        Field field = Siamese.class.getDeclaredField("processedFilesLog");
        field.setAccessible(true);
        String actual = (String) field.get(siamese);

        assertNull(actual);
    }
}
