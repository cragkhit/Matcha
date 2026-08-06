package crest.siamese.language.javascript;

import crest.siamese.document.Method;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;

import static org.junit.Assert.*;

public class JSMethodParserTest {

    final String FILE_NAME = "crest/siamese/language/javascript/All-Combination-Of-JS-Functions.js";
    final String JSX_FILE_NAME = "crest/siamese/language/javascript/JSX.jsx";
    final String SYNTAX_ERROR_FILE_NAME = "crest/siamese/language/javascript/Syntax-Error.js";
    final String EMPTY_NOT_EXISTED_FILE_NAME = "Demo.js";
    final String METHOD_MOOD = "METHOD-LEVEL";
    final String FILE_MOOD = "FILE-LEVEL";


    @Test
    public void parseMethodsWithJSXTest() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(JSX_FILE_NAME)).getFile());
        JSMethodParser jsMethodParser = new JSMethodParser();
        jsMethodParser.configure(file.getAbsolutePath(), StringUtils.EMPTY, METHOD_MOOD, false);
        ArrayList<Method> methods = jsMethodParser.parseMethods();
        assertEquals(methods.size(), 1);

    }


    @Test
    public void parseMethodsMethodLevelTest() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(FILE_NAME)).getFile());
        JSMethodParser jsMethodParser = new JSMethodParser();
        jsMethodParser.configure(file.getAbsolutePath(), StringUtils.EMPTY, METHOD_MOOD, false);
        ArrayList<Method> methods = jsMethodParser.parseMethods();
        assertEquals(methods.size(), 30);

    }

    @Test
    public void parseMethodsFileLevelTest() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(FILE_NAME)).getFile());
        JSMethodParser jsMethodParser = new JSMethodParser();
        jsMethodParser.configure(file.getAbsolutePath(), StringUtils.EMPTY, FILE_MOOD, false);
        ArrayList<Method> methods = jsMethodParser.parseMethods();
        assertEquals(methods.size(), 1);

    }

    @Test
    public void getParsedTreeTest() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(FILE_NAME)).getFile());
        JSMethodParser jsMethodParser = new JSMethodParser();
        jsMethodParser.configure(file.getAbsolutePath(), StringUtils.EMPTY, METHOD_MOOD, false);

        ParseTree parseTree = jsMethodParser.getParsedTree(file);
        assertNotNull(parseTree);


    }

    @Test
    public void getParsedTreeForSyntaxErrorFileTest() {
        ClassLoader classLoader = getClass().getClassLoader();
        File syntaxErrorSourceFile = new File(Objects.requireNonNull(classLoader.getResource(SYNTAX_ERROR_FILE_NAME)).getFile());
        JSMethodParser jsMethodParser = new JSMethodParser();
        jsMethodParser.configure(syntaxErrorSourceFile.getAbsolutePath(), StringUtils.EMPTY, METHOD_MOOD, false);
        ParseTree parseTree = jsMethodParser.getParsedTree(syntaxErrorSourceFile);
        assertNotNull(parseTree);
    }

    @Test
    public void getTraversedJSParseTreeListenerTest() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(FILE_NAME)).getFile());
        JSMethodParser jsMethodParser = new JSMethodParser();
        jsMethodParser.configure(file.getAbsolutePath(), StringUtils.EMPTY, METHOD_MOOD, false);
        JSParseTreeListener jsParseTreeListener = jsMethodParser.getTraversedJSParseTreeListener(file);
        assertNotNull(jsParseTreeListener);
    }

    @Test
    public void getJavaScriptParserTest() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(FILE_NAME)).getFile());
        JSMethodParser jsMethodParser = new JSMethodParser();
        jsMethodParser.configure(file.getAbsolutePath(), StringUtils.EMPTY, METHOD_MOOD, false);
        JavaScriptParser jsParser = jsMethodParser.getJavaScriptParser(file);
        assertNotNull(jsParser);
    }

    @Test
    public void getSourceAsCharStreamsInvalidFileTest() {
        JSMethodParser jsMethodParser = new JSMethodParser();
        File dummyFile = new File(EMPTY_NOT_EXISTED_FILE_NAME);
        assertNotNull(jsMethodParser.getSourceAsCharStreams(dummyFile));
    }

    @Test
    public void configureTest() {
        JSMethodParser jsMethodParser = new JSMethodParser();
        jsMethodParser.configure(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, false);
        assertNull(jsMethodParser.getLicense());
    }

    @Test
    public void getLicenseTest() {
        JSMethodParser jsMethodParser = new JSMethodParser();
        assertNull(jsMethodParser.getLicense());
        JSMethodParser jsmp = new JSMethodParser(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, false);
        assertNull(jsmp.getLicense());
    }

    /**
     * getParsedTree() bounds each parse with PARSE_TIMEOUT_MS (see TimeoutRunnerTest for the
     * timeout/forcible-stop mechanism itself) so a single pathological file can't hang an entire
     * indexing run. Guard the constant's value so it can't silently regress to something absurd
     * (e.g. 0, or hours long) without a test failing.
     */
    @Test
    public void parseTimeoutIsConfiguredToASaneBoundedValue() throws NoSuchFieldException, IllegalAccessException {
        Field field = JSMethodParser.class.getDeclaredField("PARSE_TIMEOUT_MS");
        field.setAccessible(true);
        long timeoutMs = field.getLong(null);
        assertTrue("PARSE_TIMEOUT_MS must be positive", timeoutMs > 0);
        assertTrue("PARSE_TIMEOUT_MS must stay well under a minute or a single bad file "
                + "could still dominate an indexing run", timeoutMs <= 60_000);
    }

}
