package interactive;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;

import file.FileLocatingClass;
import json.JsonArrayClass;
import log.LogExposureClass;
import picocli.CommandLine;
import structure.NumberClass;

/**
 * Main class
 */
@CommandLine.Command(
    name = "top",
    subcommands = {
        JsonSplit.class
    }
)
public final class JsonOperationClass {

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
        final int iExitCode = new CommandLine(new JsonOperationClass()).execute(args);
        CommonInteractiveClass.setExitCode(iExitCode);
        CommonInteractiveClass.shutMeDown(args[0]);
    }

    /**
     * Constructor empty
     */
    private JsonOperationClass() {
        // intentionally blank
    }
}

/**
 * JSON splitter
 */
@CommandLine.Command(name = "JsonSplit", description = "Splits a given JSON file into multiple smaller files") 
class JsonSplit implements Runnable {
    /**
     * JSON actual file size
     */
    private static long fileSize;
    /**
     * Size limit for split
     */
    private static final long SIZE_THRESHOLD = 5_368_709_120L; // 5GB value see https://convertlive.com/u/convert/gigabytes/to/bytes#5
    /**
     * Size percentage difference between actual & splitSize/SIZE_THRESHOLD
     */
    private static float sizeDifference;
    /**
     * balances threshold size
     */
    private static long sizeThreshold;

    /**
     * String for file name
     */
    @CommandLine.Option(
            names = {"-file", "--fileName"},
            description = "File Name to be split",
            arity = "1",
            required = true)
    private static String strFileName;

    /**
     * String for folder name
     */
    @CommandLine.Option(
            names = {"-fDst", "--folderDestination"},
            description = "Destination Folder where splited files will be created",
            arity = "1",
            required = true)
    private static String strDestFolder;

    /**
     * size of Split threshold (optional)
     */
    @CommandLine.Option(
            names = {"-sz", "--splitSize"},
            description = "Size of JSON file beyou Split is neded")
    private static long splitSize;

    /**
     * size of Split threshold (optional)
     */
    @CommandLine.Option(
            names = {"-bsz", "--bucketSize"},
            description = "Size of final characters to be overwritten as part of the bucketing logic (use -1 for no bucketing)")
    private static int bucketSize;

    @Override
    public void run() {
        setFileSize();
        if (fileSize <= 0) {
            final Properties propertiesReturn = FileLocatingClass.checkFileExistanceAndReadability(fileSize, strFileName);
            final String strFeedback = String.format("There is something not right with given file name... %s", propertiesReturn);
            LogExposureClass.LOGGER.error(strFeedback);
        } else {
            setSplitSizeThreshold();
            setFileSizeDifferenceCompareToThreshold();
            if (fileSize <= sizeThreshold) {
                final String strFeedback = String.format("File %s has a size of %s bytes which compare to split file threshold of %s bytes is %s%% smaller, hence split is NOT neccesary!", strFileName, fileSize, sizeThreshold, sizeDifference);
                LogExposureClass.LOGGER.info(strFeedback);
            } else {
            	performJsonSplit();
            }
        }
    }

    private static void performJsonSplit() {
        final String strFeedback = String.format("File %s has a size of %s bytes which compared to split file threshold of %s bytes is %s%% bigger, hence split IS required and will be performed!", strFileName, fileSize, sizeThreshold, Math.abs(sizeDifference));
        LogExposureClass.LOGGER.info(strFeedback);
        JsonArrayClass.setInputJsonFile(strFileName);
        JsonArrayClass.setDestinationFolder(strDestFolder);
        JsonArrayClass.setRelevantField("ProjectID");
        if (bucketSize != 0) {
            JsonArrayClass.setBucketLength(bucketSize);
        }
        final String destPattern = JsonArrayClass.buildDestinationFileName("x").replaceAll("x.json", ".*.json");
        FileLocatingClass.deleteFilesMathingPatternFromFolder(strDestFolder, destPattern); // clean slate to avoid inheriting old content
        JsonArrayClass.splitJsonIntoSmallerGrouped(); // actual logic
    }

    /**
     * Setter for fileSize
     */
    public static void setFileSize() {
        fileSize = FileLocatingClass.getFileSizeIfFileExistsAndIsReadable(strFileName);
    }

    /**
     * Setter for fileSize
     */
    public static void setFileSizeDifferenceCompareToThreshold() {
        final float sizePercentage = NumberClass.computePercentageSafely(fileSize, sizeThreshold);
        sizeDifference = (float) new BigDecimal(Double.toString(100 - sizePercentage))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Setter for sizeThreshold
     */
    public static void setSplitSizeThreshold() {
        sizeThreshold = SIZE_THRESHOLD;
        if (splitSize != 0) {
            sizeThreshold = splitSize;
            final String strFeedback = String.format("A custom split size threshold value has been provided %s and will be used which will ignore default value of %s bytes...", splitSize, SIZE_THRESHOLD);
            LogExposureClass.LOGGER.info(strFeedback);
        }
    }

    /**
     * Constructor empty
     */
    protected JsonSplit() {
        // intentionally blank
    }
}
