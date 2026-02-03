package project;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import json.JsoningClass;
import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;

/**
 * Project related goodies
 */
public final class ProjectClass {
    /**
     * External POM file to be considered (Optional)
     */
    private static String externalPomFile;
    /**
     * special value
     */
    private static final String GLOBAL_VERSION = "${project.version}";
    /**
     * special value
     */
    private static final String INTERNAL_POM = "/pom.xml";
    /**
     * holder of Managed Versions
     */
    private static Map<String, Object> managedVersions;
    /**
     * holder of Plugin Management Versions
     */
    private static Map<String, Object> pluginCentralVers;
    /**
     * working POM file as String
     */
    private static String pomFile;
    /**
     * current Project Model Interpolator
     */
    private static StringSearchInterpolator prjInterpolator;
    /**
     * current Project Model
     */
    private static Model prjModel;

    /**
     * Getter for pomFile
     * @return String
     */
    public static String getPomFile() {
        return pomFile;
    }

    /**
     * Getting current project folder
     * @return application folder
     */
    public static String getCurrentFolder() {
        String strAppFolder = "";
        final File directory = new File(""); // parameter is empty
        try {
            strAppFolder = directory.getCanonicalPath();
        } catch (IOException ex) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nFileFolderError"), Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return strAppFolder;
    }

    /**
     * Getter for projectModel
     * @return Model
     */
    public static Model getProjectModel() {
        if (prjModel == null) {
            loadProjectModel();
            Loaders.loadComponents();
        }
        return prjModel;
    }

    /**
     * get POM value through interpolation if needed
     * @param rawValue original value
     * @return String
     */
    private static String getProjectModelValueWithInterpolationIfNeeded(final String rawValue) {
        String finalValue = rawValue;
        if (rawValue == null || rawValue.isBlank()) {
            finalValue = "";
        } else if (GLOBAL_VERSION.equals(rawValue)) {
            finalValue = prjModel.getVersion();
        } else if (rawValue.startsWith("${")
                && rawValue.endsWith("}")) {
            try {
                finalValue = prjInterpolator.interpolate(rawValue);
            } catch (InterpolationException e) {
                final String strFeedback = String.format("InterpolationException %s", Arrays.toString(e.getStackTrace()));
                LogExposureClass.LOGGER.error(strFeedback);
            }
        }
        return finalValue;
    }

    /**
     * Map with current project module libraries
     * @return Map with modules name and its version
     */
    public static Map<String, Object> getProjectModuleLibraries() {
        // Initialize the concurrent map
        final Map<String, Object> moduleMap = new ConcurrentHashMap<>();
        // Get the boot layer (the primary module layer)
        ModuleLayer.boot().modules().forEach(module -> {
            final String strName = module.getName();
            final String strVersion = module.getDescriptor().toNameAndVersion().substring(strName.length() + 1);
            moduleMap.put(strName, strVersion);
        });
        return moduleMap;
    }

    /**
     * detects if current execution is from JAR or not
     * @return boolean
     */
    public static boolean isRunningFromJar() {
        // Get the URL of the current class's byte-code
        final URL classUrl = ProjectClass.class.getResource("ProjectClass.class");
        if (classUrl == null) {
            throw new IllegalStateException("Class resource not found");
        }
        // Check if the protocol is "jar" (JAR execution) or "file" (IDE execution)
        final String protocol = classUrl.getProtocol();
        return "jar".equals(protocol);
    }

    /**
     * Load POM for current project
     */
    public static void loadProjectModel() {
        setPomFile();
        // 1. Read the raw model
        final MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = null;
        if (INTERNAL_POM.equals(pomFile)) {
            try (InputStream inputStream = ProjectClass.class.getResourceAsStream(INTERNAL_POM)) {
                model = reader.read(inputStream);
            } catch (IOException | XmlPullParserException ex) {
                LogExposureClass.exposeProjectModel(Arrays.toString(ex.getStackTrace()));
            }
        } else {
            try(BufferedReader bReader = Files.newBufferedReader(Path.of(pomFile), StandardCharsets.UTF_8)) {
                model = reader.read(bReader);
            } catch (IOException | XmlPullParserException ex) {
                LogExposureClass.exposeProjectModel(Arrays.toString(ex.getStackTrace()));
            }
        }
        prjModel = model;
        Loaders.loadComponents();
    }

    /**
     * Setter for externalPomFile
     * @param inExtPomFile input external POM file
     */
    public static void setExternalPomFile(final String inExtPomFile) {
        externalPomFile = inExtPomFile;
    }

    /**
     * set the POM to work with
     */
    private static void setPomFile() {
        final StringBuilder sbPom = new StringBuilder(100);
        if (externalPomFile == null) {
            if (isRunningFromJar()) {
                sbPom.append(INTERNAL_POM);
            } else {
                final String strPrjFolder = getCurrentFolder();
                sbPom.append(strPrjFolder).append(File.separator).append("pom.xml");
            }
        } else {
            sbPom.append(externalPomFile);
            final String strFeedback = String.format("External POM file %s is being considered!", externalPomFile);
            LogExposureClass.LOGGER.info(strFeedback);
        }
        pomFile = sbPom.toString();
    }

    /**
     * initiating Components class
     */
    public static final class Application {

        /**
         * Application details
         * @return String
         */
        public static String getApplicationDetails() {
            final Model prjModel = getProjectModel();
            final StringBuilder strJsonString = new StringBuilder(100);
            strJsonString.append("\"Application\":{\"")
                    .append(prjModel.getGroupId())
                    .append(':')
                    .append(prjModel.getArtifactId())
                    .append("\":\"")
                    .append(prjModel.getVersion())
                    .append('\"');
            final Map<String, Object> projDependencies = Components.getProjectModelComponent("Dependencies");
            if (!projDependencies.isEmpty()) {
                strJsonString.append(",\"Dependencies\":")
                        .append(JsoningClass.getMapIntoJsonString(projDependencies));
            }
            final Map<String, Object> projBuildPlugins = Components.getProjectModelComponent("BuildPlugins");
            if (!projBuildPlugins.isEmpty()) {
                strJsonString.append(",\"Build Plugins\":")
                        .append(JsoningClass.getMapIntoJsonString(projBuildPlugins));
            }
            final Map<String, Object> projPrflPlugins = Components.getProjectModelComponent("ProfilePlugins");
            if (!projPrflPlugins.isEmpty()) {
                strJsonString.append(",\"Profile Plugins\":")
                        .append(JsoningClass.getMapIntoJsonString(projPrflPlugins));
            }
            if (!prjModel.getModules().isEmpty()) {
                strJsonString.append(getComponentModulesDetailsIfProjectModulesArePresent(prjModel));
            }
            final Map<String, Object> projLibModules = getProjectModuleLibraries();
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
            final Path pathPomFile = Path.of(getPomFile());
            final StringBuilder strJsonModule = new StringBuilder(100);
            prjModel.getModules().forEach(module -> {
                final String crtModulePom = pathPomFile.getParent()
                        + File.separator + module+ File.separator + "pom.xml";
                if (!strJsonModule.isEmpty()) {
                    strJsonModule.append(',');
                }
                setExternalPomFile(crtModulePom);
                loadProjectModel();
                final Model prjModuleModel = getProjectModel();
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
                final Map<String, Object> mdlDependencies = Components.getProjectModelComponent("Dependencies");
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
         * Constructor
         */
        private Application() {
            // intentionally left blank
        }

    }

    /**
     * initiating Components class
     */
    public static final class Loaders {

        /**
         * Load all components: Dependencies and Plug-ins
         */
        public static void loadComponents() {
            if (prjModel.getProperties() != null) {
                loadProjectModelInterpolator();
            }
            if (prjModel.getDependencyManagement() != null) {
                loadProjectModelCentralDependencies();
            }
            if (prjModel.getBuild().getPluginManagement() != null) {
                loadProjectModelPluginManagement();
            }
        }

        /**
         * Loading central dependency management if set
         */
        private static void loadProjectModelCentralDependencies() {
            final Map<String, Object> centralDeps = new ConcurrentHashMap<>();
            for (final Dependency dependency : prjModel.getDependencyManagement().getDependencies()) {
                final String strKkey = dependency.getGroupId() + ":" + dependency.getArtifactId();
                final String strVersion = getProjectModelValueWithInterpolationIfNeeded(dependency.getVersion());
                centralDeps.put(strKkey, strVersion);
            }
            managedVersions = centralDeps;
        }

        /**
         * Loading central plugin management if set
         */
        private static void loadProjectModelPluginManagement() {
            final Map<String, Object> centralPlugM = new ConcurrentHashMap<>();
            for (final Plugin plugin : prjModel.getBuild().getPluginManagement().getPlugins()) {
                final String strKkey = plugin.getGroupId() + ":" + plugin.getArtifactId();
                final String strVersion = getProjectModelValueWithInterpolationIfNeeded(plugin.getVersion());
                centralPlugM.put(strKkey, strVersion);
            }
            pluginCentralVers = centralPlugM;
        }

        /**
         * Loading Properties for current Project Model if set
         */
        private static void loadProjectModelInterpolator() {
            final StringSearchInterpolator interpolator = new StringSearchInterpolator();
            final Properties props = prjModel.getProperties();
            interpolator.addValueSource(new MapBasedValueSource(props));
            prjInterpolator = interpolator;
        }

        /**
         * Constructor
         */
        private Loaders() {
            // intentionally left blank
        }
    }

    /**
     * initiating Components class
     */
    public static final class Components {
        /**
         * special value
         */
        private static final String UNKNOWN = "UNKNOWN";

        /**
         * Project Build Plugins exposed
         * @return Map
         */
        public static Map<String, Object> getProjectModelComponent(final String strComponentName) {
            Map<String, Object> mapToReturn = new ConcurrentHashMap<>();
            switch(strComponentName) {
                case "BuildPlugins":
                    if (prjModel.getBuild() != null) {
                        mapToReturn = getBuildPlugins();
                    }
                    break;
                case "Dependencies":
                    if (prjModel.getDependencies() != null) {
                        mapToReturn = getDependencies();
                    }
                    break;
                case "ProfilePlugins":
                    if (prjModel.getProfiles() != null) {
                        mapToReturn = getProfilePlugins();
                    }
                    break;
                default:
                    break;
            }
            return mapToReturn;
        }

        /**
         * Build Plugins gathering
         * @return Map
         */
        private static Map<String, Object> getBuildPlugins() {
            final Map<String, Object> mapToReturn = new ConcurrentHashMap<>();
            prjModel.getBuild().getPlugins().forEach(plugin -> {
                final String strKey = plugin.getGroupId() + ":" + plugin.getArtifactId();
                String strVersion = getProjectModelValueWithInterpolationIfNeeded(plugin.getVersion());
                if (strVersion.isEmpty() && !managedVersions.isEmpty()) {
                    strVersion = pluginCentralVers.getOrDefault(strKey, UNKNOWN).toString();
                }
                mapToReturn.put(strKey, strVersion);
            });
            return mapToReturn;
        }

        /**
         * Dependencies gathering
         * @return Map
         */
        private static Map<String, Object> getDependencies() {
            final Map<String, Object> mapToReturn = new ConcurrentHashMap<>();
            prjModel.getDependencies().forEach(dependency -> {
                final String strKey = dependency.getGroupId() + ":" + dependency.getArtifactId();
                String strVersion = getProjectModelValueWithInterpolationIfNeeded(dependency.getVersion());
                if (strVersion.isEmpty() && !managedVersions.isEmpty()) {
                    strVersion = managedVersions.getOrDefault(strKey, UNKNOWN).toString();
                }
                mapToReturn.put(strKey, strVersion);
            });
            return mapToReturn;
        }

        /**
         * Profile Plugins gathering
         * @return Map
         */
        private static Map<String, Object> getProfilePlugins() {
            final Map<String, Object> mapToReturn = new ConcurrentHashMap<>();
            prjModel.getProfiles().forEach(profile -> {
                if (profile.getBuild() != null) {
                    profile.getBuild().getPlugins().forEach(plugin -> {
                        final String strKey = plugin.getGroupId() + ":" + plugin.getArtifactId();
                        String strVersion = getProjectModelValueWithInterpolationIfNeeded(plugin.getVersion());
                        if (strVersion.isEmpty() && !managedVersions.isEmpty()) {
                            strVersion = pluginCentralVers.getOrDefault(strKey, UNKNOWN).toString();
                        }
                        mapToReturn.put(strKey, strVersion);
                    });
                }
            });
            return mapToReturn;
        }

        /**
         * Constructor
         */
        private Components() {
            // intentionally left blank
        }

    }

    /**
     * Constructor
     */
    private ProjectClass() {
        // intentionally left blank
    }

}
