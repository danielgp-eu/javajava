package interactive;

import picocli.CommandLine;

/**
 * Main Command Line
 */
@CommandLine.Command(
    name = "top",
    subcommands = {
            ExperimentalFeature.class
    }
)
public final class NewFeatureClass {

    /**
     * Constructor empty
     */
    private NewFeatureClass() {
        super();
    }

    /**
     * Constructor
     *
     * @param args command-line arguments
     */
    /* default */ static void main(final String... args) {
        CommonInteractiveClass.setAutoLocale(true);
        CommonInteractiveClass.initializeLocalization();
        CommonInteractiveClass.setStartDateTime();
        CommonInteractiveClass.startMeUp();
        // execute appropriate Command with
        final int iExitCode = new CommandLine(new NewFeatureClass()).execute(args);
        CommonInteractiveClass.setExitCode(iExitCode);
        CommonInteractiveClass.shutMeDown(args[0]);
    }

}

/**
 * clean files older than a given number of days
 */
@CommandLine.Command(name = "ExperimentalFeature",
                     description = "Run the experimental new feature")
class ExperimentalFeature implements Runnable {

    @Override
    public void run() {
        // no-op
    }

    /**
     * Constructor
     */
    protected ExperimentalFeature() {
        super();
    }
}