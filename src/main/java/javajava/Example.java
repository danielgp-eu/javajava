package javajava;
/* Time class */
import java.time.LocalDateTime;
/* Util classes */
import java.util.Arrays;
import java.util.Properties;
/* Command Line classes */
import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
/* Logging classes */
import org.apache.logging.log4j.Level;

/**
 * Example class
 */
public final class Example { // NOPMD by Daniel Popiniuc on 24.04.2025, 23:43

    /**
     * defining Arguments
     * @return Options
     */
    private static Options definedArguments() {
        final Options options = new Options();
        // Mandatory command line arguments
        final Option paramAct = new Option("act", "action", true, JavaJavaLocalization.getMessage("i18nAppParamAct"));
        paramAct.setArgs(1);
        paramAct.setRequired(true);
        options.addOption(paramAct);
        return options;
    }

    /**
     * log Application Start
     */
    private static void logApplicationStart() {
        final String strFeedback = JavaJavaLocalization.getMessage("i18nNewExec")
            + String.valueOf("-").repeat(80);
        LogLevelChecker.logConditional(strFeedback, Level.INFO);
    }

    /**
     * Constructor
     * 
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        final LocalDateTime startTimeStamp = LocalDateTime.now();
        String strFeedback = String.valueOf("=").repeat(80);
        LogLevelChecker.logConditional(strFeedback, Level.DEBUG);
        JavaJavaLocalization.setLocaleByString(JavaJavaLocalization.getUserLocale());
        final Options options = definedArguments();
        final CommandLineParser parser = new DefaultParser();
        try {
            final CommandLine cmd = parser.parse(options, args);
            logApplicationStart();
            performAction(cmd);
        } catch (AlreadySelectedException | MissingArgumentException | MissingOptionException | UnrecognizedOptionException ex) {
            strFeedback = ex.getLocalizedMessage(); 
            LogLevelChecker.logConditional(strFeedback, Level.ERROR);
            System.exit(1);
        } catch (ParseException e) {
            strFeedback = String.format(JavaJavaLocalization.getMessage("i18nAppParamParsOptList"), options);
            LogLevelChecker.logConditional(strFeedback, Level.ERROR);
            strFeedback = String.format(JavaJavaLocalization.getMessage("i18nParamParsErr"), Arrays.toString(e.getStackTrace()));
            LogLevelChecker.logConditional(strFeedback, Level.ERROR);
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(JavaJavaLocalization.getMessage("i18nMndtParamsAre"), options);
            System.exit(1);
        }
        TimingClass.logDuration(startTimeStamp, String.format(JavaJavaLocalization.getMessage("i18nEntOp"), args[0]), "info");
    }

    /**
     * Action logic
     * @param cmd command-line arguments
     */
    private static void performAction(final CommandLine cmd) {
        final String prmActionValue = cmd.getOptionValue("action");
        Properties properties = null;
        if (prmActionValue.startsWith("getMySQL_")) {
            properties = DatabaseSpecificMySql.getConnectionPropertiesForMySQL();
        }
        switch(prmActionValue) {
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
            case "getSnowflake_CurrentUserAssignedRoles":
                DatabaseSpecificSnowflake.performSnowflakePreDefinedAction("AvailableRoles", properties);
                break;
            case "getSnowflake_Warehouses":
                DatabaseSpecificSnowflake.performSnowflakePreDefinedAction("AvailableWarehouses", properties);
                break;
            case "getSubFoldersFromFolder":
                FileHandlingClass.getSubFolderFromFolder("C:\\www\\Config\\");
                break;
            case "LogEnvironmentDetails":
                final String strFeedback = EnvironmentCapturingClass.getCurrentEnvironmentDetails();
                LogLevelChecker.logConditional(strFeedback, Level.INFO);
                break;
            case "TEST":
                break;
            default:
                final String strMsg = String.format(JavaJavaLocalization.getMessage("i18nUnknParamFinal"), prmActionValue, StackWalker.getInstance()
                    .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(Common.strUnknown)));
                LogLevelChecker.logConditional(strMsg, Level.INFO);
                break;
        }
    }

    /**
     * Constructor
     */
    private Example() {
        throw new UnsupportedOperationException(Common.strAppClsWrng);
    }
}
