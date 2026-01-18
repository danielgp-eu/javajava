package interactive;

import java.util.List;
import java.util.Map;

import file.FileContentReadClass;
import file.FileContentWriteClass;
import log.LogExposureClass;
import picocli.CommandLine;
import structure.ListAndMapClass;

/**
 * Main class
 */
@CommandLine.Command(
    name = "top",
    subcommands = {
        AnalyzeColumnsFromCsvFile.class
    }
)
public final class AnalysisCsv {

    /**
     * Constructor
     *
     * @param args command-line arguments
     */
    /* default */ static void main(final String... args) {
        CommonInteractiveClass.setAutoLocale(true);
        CommonInteractiveClass.setStartDateTime();
        CommonInteractiveClass.startMeUp();
        // execute appropriate Command with 
        final int iExitCode = new CommandLine(new AnalysisCsv()).execute(args);
        CommonInteractiveClass.setExitCode(iExitCode);
        CommonInteractiveClass.shutMeDown(args[0]);
    }

    /**
     * Constructor empty
     */
    private AnalysisCsv() {
        // intentionally blank
    }

}

/**
 * Captures execution environment details into Log file
 */
@CommandLine.Command(name = "AnalyzeColumnsFromCsvFile", description = "Analyze columns from CSV file")
class AnalyzeColumnsFromCsvFile implements Runnable {

    /**
     *
     * @param strFileName input File
     * @param intColToEval number of column to evaluate (starting from 0)
     * @param intColToGrpBy number of column to group by (starting from 0)
     */
    private static void storeWordFrequencyIntoCsvFile(final String strFileName,
                                                      final Integer intColToEval,
                                                      final Integer intColToGrpBy) {
        // Group values by category
        final Map<String, List<String>> groupedColumns = FileContentReadClass.getListOfValuesFromColumnsGroupedByAnotherColumnValuesFromCsvFile(strFileName, intColToEval, intColToGrpBy);
        // Define merge rules
        final Map<List<String>, String> mergeRules = Map.of(
                List.of("ARRAY", "OBJECT", "VARIANT"), "COMPOSITE__STRUCTURED",
                List.of("FLOAT", "NUMBER"), "COMPOSITE__NUMERIC",
                List.of("TIMESTAMP_LTZ", "TIMESTAMP_NTZ", "TIMESTAMP_TZ"), "COMPOSITE__TIMESTAMP",
                List.of("BINARY", "TEXT", "VARCHAR"), "COMPOSITE__TEXT"
        );
        final Map<String, List<String>> grpCols = ListAndMapClass.mergeKeys(groupedColumns, mergeRules);
        final String strFeedback = "=".repeat(20) + strFileName + "=".repeat(20);
        LogExposureClass.LOGGER.info(strFeedback);
        FileContentWriteClass.setCsvColumnSeparator(',');
        grpCols.forEach((keyDataType, valList) -> {
            final String strColFileName = strFileName.replace(".csv", "__columns.csv");
            final String strFeedbackFile = "Writing file " + strColFileName;
            LogExposureClass.LOGGER.info(strFeedbackFile);
            FileContentWriteClass.setCsvLinePrefix(keyDataType);
            FileContentWriteClass.writeStringListToCsvFile(strColFileName, "DataType,Column", valList);
            final String strFeedbackWrt = String.format("Writing file for %s which has %s values", keyDataType, valList.size());
            LogExposureClass.LOGGER.info(strFeedbackWrt);
            final Map<String, Long> sorted = ListAndMapClass.getWordCounts(valList, "(_| )");
            FileContentWriteClass.writeLinkedHashMapToCsvFile(strFileName.replace(".csv", "__words.csv"), "DataType,Word,Occurrences", sorted);
        });
    }

    @Override
    public void run() {
        storeWordFrequencyIntoCsvFile("C:/www/fields_edw_dev.csv", 3, 4);
        storeWordFrequencyIntoCsvFile("C:/www/fields_edw_qa.csv", 3, 4);
        storeWordFrequencyIntoCsvFile("C:/www/fields_edw_prod.csv", 3, 4);
    }

    /**
     * Constructor
     */
    protected AnalyzeColumnsFromCsvFile() {
        // intentionally blank
    }

}
