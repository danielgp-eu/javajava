package environment;

import log.LogExposureClass;

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

import file.ProjectClass;
import json.JsoningClass;
import localization.JavaJavaLocalizationClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class demonstrates how to resolve all dependencies (including transitive)
 * for a Maven project defined by a POM file using Eclipse Aether
 */
public final class ProjectDependencyResolverClass {

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
        final List<org.apache.maven.model.Dependency> lstDeps = model.getDependencies();
        lstDeps.stream().forEach(modelDep -> {
            final Artifact artifact = new DefaultArtifact(
                    modelDep.getGroupId(), 
                    modelDep.getArtifactId(), 
                    modelDep.getClassifier(), 
                    modelDep.getType(), 
                    modelDep.getVersion()
                );
            collectRequest.addDependency(new Dependency(artifact, modelDep.getScope()));
        });
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
        return JsoningClass.getMapIntoJsonString(arrayAttributes);
    }

    /**
     * Main method to execute the dependency resolution
     * @return dependency details
     */
    public static String getDependency() {
        final RepositorySystem system = new RepositorySystemSupplier().get();
        final DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        final LocalRepository localRepo = new LocalRepository("target/local-repo");
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        final Model model = getProjectObjectModelFileIntoModel();
        final CollectRequest collectRequest = createCollectRequest(model);
        final DependencyNode root = resolveDependencies(system, session, collectRequest);
        final String strFeedback = getDependenciesIntoString(root);
        system.shutdown();
        return strFeedback;
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
     * get POM into Model
     * @return Model
     */
    public static Model getProjectObjectModelFileIntoModel() {
        final String pomFile = ProjectClass.getCurrentProjectObjectModelFile();
        final MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = null;
        try(BufferedReader bReader = Files.newBufferedReader(Path.of(pomFile), StandardCharsets.UTF_8)) {
            model = reader.read(bReader);
        } catch (IOException | XmlPullParserException ex) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nErrorOnGettingDependencies"), Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return model;
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
            final String strFeedback = String.format("Dependency resolution error... %s", Arrays.toString(e.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return root;
    }

    /**
     * Constructor
     */
    private ProjectDependencyResolverClass() {
        // intentionally left blank
    }
}
