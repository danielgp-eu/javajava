package javajava;
/* Time class */
import java.time.LocalDateTime;
/* Util classes */
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
/* Command Line classes */
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
        final Option paramAct = new Option("act", "action", true, DanielLocalization.getMessage("i18nAppParamAct"));
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
        // setting Locale from current user, if missing en-US will be used
        DanielLocalization.setLocale(Locale.forLanguageTag(DanielLocalization.getUserLocale()));
        String strFeedback = DanielLocalization.getMessage("i18nNewExec")
            + new String(new char[80]).replace("\0", "=");
        LOGGER.debug(strFeedback);
        LOGGER.error(strFeedback);
        LOGGER.info(strFeedback);
        final Options options = definedArguments();
        final CommandLineParser parser = new DefaultParser();
        try {
            final CommandLine cmd = parser.parse(options, args);
            performAction(cmd);
        } catch (ParseException e) {
            strFeedback = String.format(DanielLocalization.getMessage("i18nParamParsErr"), Arrays.toString(e.getStackTrace()));
            LOGGER.error(strFeedback);
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(DanielLocalization.getMessage("i18nMndtParamsAre"), options);
            System.exit(1);
        }
        TimingClass.logDuration(startTimeStamp, String.format(DanielLocalization.getMessage("i18nEntOp"), args[0]), "info");
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
                final String strMsg = String.format(DanielLocalization.getMessage("i18nUnknParamFinal"), prmActionValue, StackWalker.getInstance()
                    .walk(frames -> frames.findFirst().map(frame -> frame.getClassName() + "." + frame.getMethodName()).orElse(Common.strUnknown)));
                LOGGER.info(strMsg);
                break;
        }
    }
}
