package crest.siamese.language.javascript;

import org.junit.Before;
import org.junit.Test;


import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.Assert.*;


public class JSTokenizerTest {

    final String DUMMY_FILE_PATH = "crest/siamese/language/javascript/DemoTest.js";
    final String TEST_SOURCE = "function (a,b){ return a+b;} ";
    final ArrayList<String> TOKENIZED_SOURCE = new ArrayList<>(Arrays.asList(
            "function", "(", "a", ",", "b", ")", "{", "return", "a", "+", "b", ";", "}"
    ));

    JSTokenizer jsTokenizer;
    File resourceSourceFile;

    @Before
    public void init() {
        JSNormalizerMode jsNormalizerMode = new JSNormalizerMode();
        JSNormalizer jsNormalizer = new JSNormalizer();
        jsNormalizer.configure(jsNormalizerMode);
        jsTokenizer = new JSTokenizer();
        jsTokenizer.configure(jsNormalizer);
        ClassLoader classLoader = getClass().getClassLoader();
        resourceSourceFile = new File(Objects.requireNonNull(classLoader.getResource(DUMMY_FILE_PATH)).getFile());

    }

    @Test
    public void getTokensFromStringTest() {
        ArrayList<String> actual = jsTokenizer.getTokensFromString(TEST_SOURCE);
        assertEquals(actual, TOKENIZED_SOURCE);

    }

    @Test
    public void testTokenize() throws IOException {
        Reader reader = new FileReader(resourceSourceFile);
        assertEquals(0, jsTokenizer.tokenize(TEST_SOURCE).size());
        assertEquals(0, jsTokenizer.tokenize(reader).size());
        assertEquals(0, jsTokenizer.tokenizeLine(reader).size());
        assertEquals(0, jsTokenizer.tokenize(resourceSourceFile).size());
        assertEquals(0, jsTokenizer.getTokensFromFile(resourceSourceFile.getAbsolutePath()).size());

    }

    /**
     * getTokens() bounds each lex call with TOKENIZE_TIMEOUT_MS (see TimeoutRunnerTest for the
     * timeout/forcible-stop mechanism itself), since the custom lexer's stateful HTML-tag
     * tracking predicate can loop independently of the parser's own prediction engine. Guard the
     * constant's value so it can't silently regress without a test failing.
     */
    @Test
    public void tokenizeTimeoutIsConfiguredToASaneBoundedValue() throws NoSuchFieldException, IllegalAccessException {
        Field field = JSTokenizer.class.getDeclaredField("TOKENIZE_TIMEOUT_MS");
        field.setAccessible(true);
        long timeoutMs = field.getLong(null);
        assertTrue("TOKENIZE_TIMEOUT_MS must be positive", timeoutMs > 0);
        assertTrue("TOKENIZE_TIMEOUT_MS must stay well under a minute, and each method is "
                + "tokenized up to 4 times per file, or a single bad method could still "
                + "dominate an indexing run", timeoutMs <= 60_000);
    }

    /**
     * Regression check: routing getTokens() through TimeoutRunner must not change results for
     * ordinary input that finishes well within the timeout. (Real catastrophic-ambiguity input
     * that actually exercises the timeout-and-abandon path is corpus-specific and takes on the
     * order of TOKENIZE_TIMEOUT_MS to reproduce, so it isn't reliably reproducible in a fast
     * unit test; TimeoutRunnerTest covers the abandon-and-return-early mechanism itself against
     * a genuine infinite loop instead.)
     */
    @Test
    public void getTokensFromStringStillProducesNormalResultsThroughTheTimeoutWrapper() {
        ArrayList<String> actual = jsTokenizer.getTokensFromString(TEST_SOURCE);
        assertFalse(actual.isEmpty());
        assertEquals(TOKENIZED_SOURCE, actual);
    }

}
