package dependency;

import javajava.Common;
import javajava.JavaJavaLocalization;
import javajava.LoggerLevelProvider;
import javajava.StringManipulationClass;

import org.apache.logging.log4j.Level;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.supplier.RepositorySystemSupplier;

import file.FileHandlingClass;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class demonstrates how to resolve all dependencies (including transitive)
 * for a Maven project defined by a POM file using Eclipse Aether
 */
final public class ProjectDependencyResolver {

    /**
     * Minimum prefix length
     */
    private static final int MIN_PREFIX_LENGTH = 4;

    /**
     * Create CollectRequest from Maven Model
     * @param model Model
     * @return CollectRequest
     */
    private static CollectRequest createCollectRequest(final Model model) {
        final CollectRequest collectRequest = new CollectRequest();
        final RemoteRepository central = new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build();
        collectRequest.addRepository(central);
        for (final org.apache.maven.model.Dependency modelDep : model.getDependencies()) {
            final Artifact artifact = new DefaultArtifact(
                modelDep.getGroupId(), 
                modelDep.getArtifactId(), 
                modelDep.getClassifier(), 
                modelDep.getType(), 
                modelDep.getVersion()
            );
            collectRequest.addDependency(new Dependency(artifact, modelDep.getScope()));
        }
        return collectRequest;
    }

    /**
     * Helper to recurse through transitive
     * @param arrayAttributes attributes
     * @param strDirectNode direct node
     * @param node dependency node
     * @param depth integer
     */
    private static void displayTransitive(final Map<String, Object> arrayAttributes
            , final String strDirectNode
            , final DependencyNode node
            , final int depth) {
        String strCurrentNode;
        String strPrefix = strDirectNode + " => ";
        if (strPrefix.length() == MIN_PREFIX_LENGTH) {
            strPrefix = "";
        }
        for (final DependencyNode child : node.getChildren()) {
            strCurrentNode = strPrefix + getGroupIdArtifactId(child);
            arrayAttributes.put(strCurrentNode, getNodeVersion(child));
            displayTransitive(arrayAttributes, strCurrentNode, child, depth + 1);
        }
    }
    /**
     * establish current POM file
     * @return String
     */
    private static String getCurrentProjectObjectModelFile() {
        String strPomFile = FileHandlingClass.getProjectFolder();
        PropertiesReader reader;
        try {
            reader = new PropertiesReader("project.properties");
            if (Common.isRunningFromJar()) {
                strPomFile += String.format("/%s-%s.pom", 
                        reader.getProperty("artifactId"),
                        reader.getProperty("version"));
            } else {
                strPomFile += File.separator + "pom.xml";
            }
        } catch (IOException ex) {
            Common.setInputOutputExecutionLoggedToError(String.format(JavaJavaLocalization.getMessage("i18nFileFindingError"), "pom.xml", Arrays.toString(ex.getStackTrace())));
        }
        if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.INFO)) {
            final String strFeedback = String.format(JavaJavaLocalization.getMessage("i18nFileContentIntoStreamSuccess"), strPomFile);
            LoggerLevelProvider.LOGGER.debug(strFeedback);
        }
        return strPomFile;
    }

    /**
     * capture Dependencies
     * 
     * @param root dependency node
     * @return String
     */
    private static String getDependenciesIntoString(final DependencyNode root) {
        final Map<String, Object> arrayAttributes = new ConcurrentHashMap<>();
        String strDirectNode;
        for (final DependencyNode directNode : root.getChildren()) {
            strDirectNode = getGroupIdArtifactId(directNode);
            arrayAttributes.put(strDirectNode, getNodeVersion(directNode));
            // Transitive dependencies are children of the direct nodes
            displayTransitive(arrayAttributes, strDirectNode, directNode, 1);
        }
        return StringManipulationClass.getMapIntoJsonString(arrayAttributes);
    }

    /**
     * combine GroupId and ArtifactId
     * @param directNode direct node
     * @return String
     */
    private static String getGroupIdArtifactId(final DependencyNode directNode) {
        final Artifact node = getNodeArtifact(directNode);
        return node.getGroupId() + "/" + node.getArtifactId();
    }

    /**
     * combine Artifact
     * @param directNode direct node
     * @return Artifact
     */
    private static Artifact getNodeArtifact(final DependencyNode directNode) {
        return directNode.getArtifact();
    }

    /**
     * combine Version
     * @param directNode direct node
     * @return String
     */
    private static String getNodeVersion(final DependencyNode directNode) {
        final Artifact node = getNodeArtifact(directNode);
        return node.getVersion();
    }

    /**
     * combine Version
     * @param result of dependency
     * @return DependencyNode
     */
    private static DependencyNode getRootNode(final DependencyResult result) {
        return result.getRoot();
    }

    /**
     * Main method to execute the dependency resolution
     * @return dependency details
     */
    public static String getDependency() {
        final String pomFile = getCurrentProjectObjectModelFile();
        final RepositorySystem system = new RepositorySystemSupplier().get();
        final DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        final LocalRepository localRepo = new LocalRepository("target/local-repo");
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        final MavenXpp3Reader reader = new MavenXpp3Reader();
        final Model model;
        String strFeedback = "";
        try(BufferedReader bReader = Files.newBufferedReader(Path.of(pomFile), StandardCharsets.UTF_8)) {
            model = reader.read(bReader);
            final CollectRequest collectRequest = createCollectRequest(model);
            final DependencyNode root = resolveDependencies(system, session, collectRequest);
            strFeedback = getDependenciesIntoString(root);
        } catch (IOException | XmlPullParserException ex) {
            Common.setInputOutputExecutionLoggedToError(
                    String.format(JavaJavaLocalization.getMessage("i18nErrorOnGettingDependencies"),
                            Arrays.toString(ex.getStackTrace())));
        }
        system.shutdown();
        return strFeedback;
    }

    /**
     * Resolve dependencies and return the root node
     * @param system RepositorySystem
     * @param session DefaultRepositorySystemSession
     * @param collectRequest CollectRequest
     * @return DependencyNode
     */
    private static DependencyNode resolveDependencies(final RepositorySystem system
            , final DefaultRepositorySystemSession session
            , final CollectRequest collectRequest) {
        final DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);
        DependencyNode root = null;
        final DependencyResult result;
        try {
            result = system.resolveDependencies(session, dependencyRequest);
            root = getRootNode(result);
        } catch (DependencyResolutionException e) {
            if (LoggerLevelProvider.currentLevel.isLessSpecificThan(Level.FATAL)) {
                final String strFeedback = Arrays.toString(e.getStackTrace());
                LoggerLevelProvider.LOGGER.error(strFeedback);
            }
        }
        return root;
    }

    // Private constructor to prevent instantiation
    private ProjectDependencyResolver() {
        // intentionally left blank
    }
}
