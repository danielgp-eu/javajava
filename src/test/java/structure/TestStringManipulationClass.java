package structure;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TestStringManipulationClass {

    @Test
    void cleanStringFromCurlyBraces() {
        final String strExpected = "Original";
        assertEquals(strExpected, StringManipulationClass.CleaningClass.cleanStringFromCurlyBraces("{" + strExpected + "}"));
    }

    @Test
    void convertPromptParametersIntoNamedParameters() {
        final String strOrig = "SELECT {Field A}";
        final String strExpected = "SELECT :Field_A";
        assertEquals(strExpected, StringManipulationClass.TransformingClass.convertPromptParametersIntoNamedParameters(strOrig));
    }

    @Test
    void convertPromptParametersIntoParameters() {
        final String strOrig = "SELECT {Field A}";
        final String strExpected = "SELECT ?";
        assertEquals(strExpected, StringManipulationClass.TransformingClass.convertPromptParametersIntoParameters(strOrig));
    }

    @Test
    void encloseStringIfContainsSpace() {
        final String strOrig = "Original String";
        final String strExpected = "\"Original String\"";
        assertEquals(strExpected, StringManipulationClass.TransformingClass.encloseStringIfContainsSpace(strOrig, '\"'));
    }

    @Test
    void encloseStringIfContainsSpaceFutile() {
        final String strExpected = "\"Original String\"";
        assertEquals(strExpected, StringManipulationClass.TransformingClass.encloseStringIfContainsSpace(strExpected, '\"'));
    }

    @Test
    void encloseStringIfContainsSpacePartialEnd() {
        final String strExpected = "\"Original String";
        assertEquals(strExpected + '"', StringManipulationClass.TransformingClass.encloseStringIfContainsSpace(strExpected, '\"'));
    }

    @Test
    void encloseStringIfContainsSpacePartialStart() {
        final String strExpected = "Original String\"";
        assertEquals('"' + strExpected, StringManipulationClass.TransformingClass.encloseStringIfContainsSpace(strExpected, '\"'));
    }

    @Test
    void handleNameUnformattedMessage() {
        final String strUnformatted = "Multiple strings are %s";
        final String strExpected = "Multiple strings are present";
        assertEquals(strExpected, StringManipulationClass.handleNameUnformattedMessage(1, strUnformatted, "present"));
    }

    @Test
    void handleNameUnformattedMessage2() {
        final String strUnformatted = "Multiple strings are %s and %s";
        final String strExpected = "Multiple strings are present and active";
        assertEquals(strExpected, StringManipulationClass.handleNameUnformattedMessage(2, strUnformatted, "present", "active"));
    }

    @Test
    void handleNameUnformattedMessage3() {
        final String strUnformatted = "Multiple strings are %s, %s and %s";
        final String strExpected = "Multiple strings are present, active and high quality";
        assertEquals(strExpected, StringManipulationClass.handleNameUnformattedMessage(3, strUnformatted, "present", "active", "high quality"));
    }

    @Test
    void handleNameUnformattedMessageElse() {
        final String strUnformatted = "Multiple strings are %s, %s, %s and %s";
        final String strExpected = "Multiple strings are present, active, cool and high quality";
        String exception = StringManipulationClass.handleNameUnformattedMessage(4, strUnformatted, "present", "active", "high", "quality");
        assertNotEquals(strExpected, exception);
    }

    @Test
    void hasMatchingSubstring() {
        final List<String> listStrings = new ArrayList<>();
        listStrings.add("First");
        listStrings.add("Second");
        assertEquals(true, StringManipulationClass.EvaluatingClass.hasMatchingSubstring("First", listStrings));
    }

    @Test
    void stripQuotes() {
        final String strOrig = "Original";
        assertEquals(strOrig, StringManipulationClass.CleaningClass.stripQuotes("\"" + strOrig + "\""));
    }

    @Test
    void stripQuotesShort() {
        final String strOrig = "O";
        assertNotEquals(strOrig, StringManipulationClass.CleaningClass.stripQuotes("\""));
    }

}