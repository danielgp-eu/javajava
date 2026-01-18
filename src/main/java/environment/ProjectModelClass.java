package environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import file.ProjectClass;
import localization.JavaJavaLocalizationClass;
import log.LogExposureClass;

/**
 * Project Model
 */
public final class ProjectModelClass {
    /**
     * current Project properties
     */
    private static final Properties projProperties = new Properties();

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
     * collect request for this project
     * @return CollectRequest
     */
    public static CollectRequest getCollectRequestForCurrentProject() {
        final Model model = getProjectObjectModelFileIntoModel();
        final CollectRequest collectRequest = createCollectRequest(model);
        final String strFeedback = "I have created Request for Collection of current project!";
        LogExposureClass.LOGGER.debug(strFeedback);
        return collectRequest;
    }

    /**
     * Getter for Project Properties
     */
    public static Properties getProjectProperties() {
        return projProperties;
    }

    /**
     * Setter for Project Properties
     * @param model model
     */
    public static void setProjectProperties(final Model model) {
        projProperties.put("groupId", model.getGroupId());
        projProperties.put("artifactId", model.getArtifactId());
        projProperties.put("version", model.getVersion());
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
            setProjectProperties(model);
        } catch (IOException | XmlPullParserException ex) {
            final String strFeedback = String.format(JavaJavaLocalizationClass.getMessage("i18nErrorOnGettingDependencies"), Arrays.toString(ex.getStackTrace()));
            LogExposureClass.LOGGER.error(strFeedback);
        }
        return model;
    }

    /**
     * Constructor
     */
    private ProjectModelClass() {
        // intentionally left blank
    }

}
