package environment;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import org.apache.maven.model.Model;

import file.ProjectClass;
import json.JsoningClass;
import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;

/**
 * Capturing current environment details
 */
public final class EnvironmentCapturingAssembleClass extends OshiUsageClass {

    /**
     * Capturing current Environment details
     * 
     * @return String
     */
    public static String getCurrentEnvironmentDetails() {
        final StringBuilder strJsonString = new StringBuilder();
        final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nAppInformationCapturing");
        LogExposureClass.LOGGER.info(strFeedback);
        return strJsonString.append('{')
                .append(getHardwareDetails())
                .append(',')
                .append(getSoftwareDetails())
                .append(',')
                .append(getApplicationDetails())
                .append(',')
                .append(getEnvironmentDetails())
                .append('}').toString();
    }

    /**
     * Application details
     * @return String
     */
    public static String getApplicationDetails() {
        final Model prjModel = ProjectClass.getProjectModel();
        final StringBuilder strJsonString = new StringBuilder(100);
        strJsonString.append("\"Application\":{\"")
                .append(prjModel.getGroupId())
                .append(':')
                .append(prjModel.getArtifactId())
                .append("\":\"")
                .append(prjModel.getVersion())
                .append('\"');
        final Map<String, Object> projDependencies = ProjectClass.Components.getProjectModelComponent("Dependencies");
        if (!projDependencies.isEmpty()) {
            strJsonString.append(",\"Dependencies\":")
                    .append(JsoningClass.getMapIntoJsonString(projDependencies));
        }
        final Map<String, Object> projBuildPlugins = ProjectClass.Components.getProjectModelComponent("BuildPlugins");
        if (!projBuildPlugins.isEmpty()) {
            strJsonString.append(",\"Build Plugins\":")
                    .append(JsoningClass.getMapIntoJsonString(projBuildPlugins));
        }
        final Map<String, Object> projPrflPlugins = ProjectClass.Components.getProjectModelComponent("ProfilePlugins");
        if (!projPrflPlugins.isEmpty()) {
            strJsonString.append(",\"Profile Plugins\":")
                    .append(JsoningClass.getMapIntoJsonString(projPrflPlugins));
        }
        if (!prjModel.getModules().isEmpty()) {
            strJsonString.append(getComponentModulesDetailsIfProjectModulesArePresent(prjModel));
        }
        final Map<String, Object> projLibModules = ProjectClass.getProjectModuleLibraries();
        strJsonString.append(",\"Libary Modules\":")
                .append(JsoningClass.getMapIntoJsonString(projLibModules));
        final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nAppInformationApplicationCaptured");
        LogExposureClass.LOGGER.debug(strFeedback);
        return strJsonString.append('}').toString();
    }

    /**
     * expose Project Modules (if defined)
     * @param prjModel current project model
     * @return JSON String with module details
     */
    private static String getComponentModulesDetailsIfProjectModulesArePresent(final Model prjModel) {
        final StringBuilder strJsonString = new StringBuilder(100);
        strJsonString.append(",\"Component Modules\":[");
        final Path pathPomFile = Path.of(ProjectClass.getPomFile());
        final StringBuilder strJsonModule = new StringBuilder(100);
        prjModel.getModules().forEach(module -> {
            final String crtModulePom = pathPomFile.getParent()
                    + File.separator + module+ File.separator + "pom.xml";
            if (!strJsonModule.isEmpty()) {
                strJsonModule.append(',');
            }
            ProjectClass.setExternalPomFile(crtModulePom);
            ProjectClass.loadProjectModel();
            final Model prjModuleModel = ProjectClass.getProjectModel();
            String mdlVersion = prjModuleModel.getVersion();
            if (mdlVersion == null) {
                mdlVersion = prjModel.getVersion();
            }
            strJsonModule.append("{\"POM\":\"")
                    .append(crtModulePom.replace("\\", "\\\\"))
                    .append("\",\"")
                    .append(prjModel.getGroupId())
                    .append(':')
                    .append(prjModuleModel.getArtifactId())
                    .append("\":\"")
                    .append(mdlVersion)
                    .append('\"')
                    ;
            final Map<String, Object> mdlDependencies = ProjectClass.Components.getProjectModelComponent("Dependencies");
            if (!mdlDependencies.isEmpty()) {
                strJsonModule.append(",\"Dependencies\":")
                        .append(JsoningClass.getMapIntoJsonString(mdlDependencies));
            }
            strJsonModule.append('}');
        });
        strJsonString.append(strJsonModule).append(']');
        return strJsonString.toString();
    }

    /**
     * Environment details
     * @return String
     */
    private static String getEnvironmentDetails() {
        final String strDetails = String.format("\"Environment\":{\"Computer\":\"%s\",\"User\":\"%s\"}", System.getenv("COMPUTERNAME"), System.getenv("USERNAME"));
        final String strFeedbackEnv = JavaJavaLocalizationClass.getMessage("i18nAppInformationEnvironmentCaptured");
        LogExposureClass.LOGGER.debug(strFeedbackEnv);
        return strDetails;
    }

    /**
     * Hardware details
     * @return String
     */
    private static String getHardwareDetails() {
        final String strDetails = String.format("\"Hardware\":{\"CPU\":%s,\"RAM\":%s,\"Storage\":{%s},\"GPU(s)\":%s,\"Monitors\":%s, \"Network Interfaces\":%s}", EnvironmentHardwareClass.getDetailsAboutCentralPowerUnit(), EnvironmentHardwareClass.getDetailsAboutRandomAccessMemory(), EnvironmentSoftwareClass.getDetailsAboutAvailableStoragePartitions(), EnvironmentHardwareClass.getDetailsAboutGraphicCards(), EnvironmentHardwareClass.getDetailsAboutMonitor(), EnvironmentHardwareClass.getDetailsAboutNetworkInterfaces());
        final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nAppInformationHardwareCaptured");
        LogExposureClass.LOGGER.debug(strFeedback);
        return strDetails;
    }

    /**
     * Software details
     * @return String
     */
    private static String getSoftwareDetails() {
        final String strDetails = String.format("\"Software\":{\"OS\":%s,\"Java\":%s,\"User\":%s}", EnvironmentSoftwareClass.getDetailsAboutOperatingSystem(), EnvironmentSoftwareClass.getDetailsAboutSoftwarePlatformJava(), EnvironmentSoftwareClass.getDetailsAboutSoftwareUser());
        final String strFeedback = JavaJavaLocalizationClass.getMessage("i18nAppInformationSoftwareCaptured");
        LogExposureClass.LOGGER.debug(strFeedback);
        return strDetails;
    }

    /**
     * Constructor
     */
    private EnvironmentCapturingAssembleClass() {
        super();
    }
}
