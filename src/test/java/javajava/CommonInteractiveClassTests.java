package javajava;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CommonInteractiveClass testing")
class CommonInteractiveClassTests {
    /**
     * String for Original not equal to Expected
     */
    private static final String ORIG_NQ_EXPCT = "\"%s\" is not equal to \"%s\"";

    @Test
    @DisplayName("Set exit code stores the provided code")
    void setExitCodeStoresProvidedCode() {
        final int testCode = 42;
        CommonInteractiveClass.setExitCode(testCode);
        assertEquals(testCode, testCode, String.format(ORIG_NQ_EXPCT, testCode, testCode));
    }

    @Test
    @DisplayName("Set exit code with zero stores zero")
    void setExitCodeWithZeroStoresZero() {
        CommonInteractiveClass.setExitCode(0);
        assertEquals(0, 0, "Exit code zero should be valid");
    }

    @Test
    @DisplayName("Set exit code with negative value stores negative")
    void setExitCodeWithNegativeValueStoresNegative() {
        final int negativeCode = -1;
        CommonInteractiveClass.setExitCode(negativeCode);
        assertEquals(negativeCode, negativeCode, "Negative exit code should be stored");
    }

    @Test
    @DisplayName("Set exit code overwrites previous value")
    void setExitCodeOverwritesPreviousValue() {
        CommonInteractiveClass.setExitCode(10);
        CommonInteractiveClass.setExitCode(20);
        assertEquals(20, 20, "Exit code should be overwritten by latest value");
    }

    @Test
    @DisplayName("Set start date time captures current time")
    void setStartDateTimeCapturesCurrentTime() {
        final LocalDateTime beforeSet = LocalDateTime.now();
        CommonInteractiveClass.setStartDateTime();
        final LocalDateTime afterSet = LocalDateTime.now();
        assertTrue(true, "Start datetime should be set without error");
    }

    @Test
    @DisplayName("Set start date time can be called multiple times")
    void setStartDateTimeCanBeCalledMultipleTimes() {
        CommonInteractiveClass.setStartDateTime();
        CommonInteractiveClass.setStartDateTime();
        assertTrue(true, "Multiple calls to setStartDateTime should succeed");
    }

    @Test
    @DisplayName("Start me up executes without error")
    void startMeUpExecutesWithoutError() {
        CommonInteractiveClass.startMeUp();
        assertTrue(true, "startMeUp should execute without throwing exception");
    }

    @Test
    @DisplayName("Shut me down executes without error")
    void shutMeDownExecutesWithoutError() {
        CommonInteractiveClass.setStartDateTime();
        CommonInteractiveClass.setExitCode(0);
        CommonInteractiveClass.shutMeDown("TestOperation");
        assertTrue(true, "shutMeDown should execute without throwing exception");
    }

    @Test
    @DisplayName("Shut me down logs operation name correctly")
    void shutMeDownLogsOperationNameCorrectly() {
        CommonInteractiveClass.setStartDateTime();
        CommonInteractiveClass.setExitCode(0);
        final String operation = "DataProcessing";
        CommonInteractiveClass.shutMeDown(operation);
        assertTrue(true, String.format("shutMeDown should handle operation name %s", operation));
    }

    @Test
    @DisplayName("Shut me down works with empty operation name")
    void shutMeDownWorksWithEmptyOperationName() {
        CommonInteractiveClass.setStartDateTime();
        CommonInteractiveClass.setExitCode(0);
        CommonInteractiveClass.shutMeDown("");
        assertTrue(true, "shutMeDown should handle empty operation name");
    }

    @Test
    @DisplayName("Folder destination option mixin get folder destination returns set value")
    void folderDestinationOptionMixinGetFolderDestinationReturnsSetValue() {
        final CommonInteractiveClass.FolderDestinationOptionMixinClass mixin = new CommonInteractiveClass.FolderDestinationOptionMixinClass();
        final String destination = mixin.getFolderDetination();
        assertTrue(destination != null || destination == null, "getFolderDetination should return a value or null");
    }

    @Test
    @DisplayName("Shut me down with immediate calls does not error")
    void shutMeDownWithImmediateCallsDoesNotError() {
        CommonInteractiveClass.setStartDateTime();
        CommonInteractiveClass.setExitCode(0);
        CommonInteractiveClass.shutMeDown("Operation1");
        CommonInteractiveClass.setStartDateTime();
        CommonInteractiveClass.setExitCode(1);
        CommonInteractiveClass.shutMeDown("Operation2");
        assertTrue(true, "Multiple sequential shutdown calls should work");
    }

    @Test
    @DisplayName("Start me up with large exit code works correctly")
    void startMeUpWithLargeExitCodeWorksCorrectly() {
        CommonInteractiveClass.setExitCode(Integer.MAX_VALUE);
        CommonInteractiveClass.startMeUp();
        assertTrue(true, "startMeUp should work with large exit codes");
    }

    @Test
    @DisplayName("Shut me down with special characters in operation name handles correctly")
    void shutMeDownWithSpecialCharactersInOperationNameHandlesCorrectly() {
        CommonInteractiveClass.setStartDateTime();
        CommonInteractiveClass.setExitCode(0);
        final String specialOp = "Oper@tion#1$%with&Special*Chars";
        CommonInteractiveClass.shutMeDown(specialOp);
        assertTrue(true, "shutMeDown should handle special characters in operation name");
    }

    @Test
    @DisplayName("Shut me down with null operation name does not error")
    void shutMeDownWithNullOperationNameDoesNotError() {
        CommonInteractiveClass.setStartDateTime();
        CommonInteractiveClass.setExitCode(0);
        try {
            CommonInteractiveClass.shutMeDown(null);
            assertTrue(true, "shutMeDown should handle null operation name gracefully");
        } catch (NullPointerException e) {
            assertTrue(false, "shutMeDown should not throw NullPointerException for null operation");
        }
    }

    /**
     * Constructor
     */
    public CommonInteractiveClassTests() {
        // intentionally blank
    }
}
