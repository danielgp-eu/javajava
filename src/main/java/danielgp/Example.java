package danielgp;
/* Time class */
import java.time.LocalDateTime;
/* Util classes */
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
/* Daniel-Gheorghe Popiniuc classes */
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
/* Logging classes */
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Example class
 */
public final class Example { // NOPMD by Daniel Popiniuc on 24.04.2025, 23:43
    /**
     * pointer for all logs
     */
    private static final Logger LOGGER = LogManager.getLogger(Example.class);

    /**
     * defining Arguments
     * @return
     */
    private static Options definedArguments() {
        final Options options = new Options();
        final Option paramAct = new Option("act", "action", true, "Action to perform");
        paramAct.setRequired(true);
        options.addOption(paramAct);
        return options;
    }

    /**
     * Constructor
     * 
     * @param args
     */
    public static void main(final String[] args) {
        final LocalDateTime startTimeStamp = LocalDateTime.now();
        String strFeedback = "NEW App Execution" + new String(new char[80]).replace("\0", "=");
        LOGGER.debug(strFeedback);
        LOGGER.error(strFeedback);
        LOGGER.info(strFeedback);
        final Options options = definedArguments();
        final CommandLineParser parser = new DefaultParser();
        try {
            final CommandLine cmd = parser.parse(options, args);
            performAction(cmd);
        } catch (ParseException e) {
            strFeedback = String.format("Parameter parsing error: %s", Arrays.toString(e.getStackTrace()));
            LOGGER.error(strFeedback);
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Following mandatory arguments are: ", options);
            System.exit(1);
        }
        TimingClass.logDuration(startTimeStamp, String.format("Entire operation %s completed", args[0]), "info");
    }

    /**
     * Action logic
     * @param cmd
     */
    private static void performAction(final CommandLine cmd) {
        final String prmActionValue = cmd.getOptionValue("action");
        Properties properties = null;
        if (prmActionValue.startsWith("getMySQL_")) {
            properties = DatabaseSpecificMySql.getConnectionPropertiesForMySQL();
        }
        switch(prmActionValue) {
            case "LogEnvironmentDetails":
                final String strFeedback = EnvironmentCapturingClass.getCurrentEnvironmentDetails();
                LOGGER.info(strFeedback);
                break;
            case "getMySQL_Columns":
                DatabaseSpecificMySql.performMySqlPreDefinedAction("Columns", properties);
                break;
            case "getMySQL_Databases":
                DatabaseSpecificMySql.performMySqlPreDefinedAction("Databases", properties);
                break;
            case "getMySQL_TableAndViews":
                DatabaseSpecificMySql.performMySqlPreDefinedAction("TablesAndViews", properties);
                break;
            case "getMySQL_Views":
                DatabaseSpecificMySql.performMySqlPreDefinedAction("Views", properties);
                break;
            case "getMySQL_ViewsLight":
                DatabaseSpecificMySql.performMySqlPreDefinedAction("Views_Light", properties);
                break;
            case "getSubFoldersFromFolder":
                final List<String> listSubFolders = FileHandlingClass.getSubFolderFromFolder("C:\\www\\Config\\");
                listSubFolders.forEach(LOGGER::info);
                break;
            case "TEST":
                break;
            default:
                final String strMsg = String.format("Unknown %s argument received in %s, do not know what to do with it, therefore will quit, bye!", prmActionValue, StackWalker.getInstance()
                        .walk(frames -> frames.findFirst()
                        .map(frame -> frame.getClassName() + "." + frame.getMethodName())
                        .orElse(Common.strUnknown)));
                LOGGER.info(strMsg);
                break;
        }
    }
}
