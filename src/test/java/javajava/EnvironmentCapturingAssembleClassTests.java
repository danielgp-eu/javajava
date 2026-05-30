package javajava;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Testing for EnvironmentCapturingAssembleClass
 */
class EnvironmentCapturingAssembleClassTests {

    private boolean isValid(final String json) {
        final ObjectMapper mapper = JsonMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .build();
        boolean bolReturn = true;
        try {
            if (json == null) {
                bolReturn = false;
            } else {
                mapper.readTree(json);
            }
        } catch (JacksonException _) {
            bolReturn = false;
        }
        return bolReturn;
    }

    @Test
    @DisplayName("Simple test to check if environment details gathering results returns a valid JSON")
    void testPackageCurrentEnvironmentDetailsIntoJson() {
        final String handled = EnvironmentCapturingAssembleClass.packageCurrentEnvironmentDetailsIntoJson();
        assertTrue(isValid(handled), String.format("JSON produced by environment gathering logic does not seem to be valid... %s", handled));
    }

    @Test
    @DisplayName("Additional test to check if environment details gathering results returns a valid JSON")
    void testPackageCurrentEnvironmentDetailsIntoListOfProperties() {
        final List<Properties> handled = EnvironmentCapturingAssembleClass.packageCurrentEnvironmentDetailsIntoListOfProperties();
        assertNotNull(handled, String.format("Environment gathering logic should not be null... %s", handled));
    }

    /**
     * Constructor
     */
    public EnvironmentCapturingAssembleClassTests() {
        // intentionally blank
    }

}
