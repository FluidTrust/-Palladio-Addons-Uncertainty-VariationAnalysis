package edu.kit.kastel.dsis.fluidtrust.uncertainty.variation.analysis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.xtext.linking.impl.AbstractCleaningLinker;
import org.eclipse.xtext.linking.impl.DefaultLinkingService;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.resource.containers.ResourceSetBasedAllContainersStateProvider;
import org.palladiosimulator.dataflow.confidentiality.pcm.dddsl.DDDslStandaloneSetup;

import de.uka.ipd.sdq.workflow.Workflow;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.SequentialBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.SequentialJob;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.dataflow.analysis.DataflowAnalysisJob;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.dataflow.analysis.RunOnlineShopAnalysisJob;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.result.interpretation.NaiveResultInterpretation;
import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;
import tools.mdsd.library.standalone.initialization.emfprofiles.EMFProfileInitializationTask;
import tools.mdsd.library.standalone.initialization.log4j.Log4jInitilizationTask;
import org.palladiosimulator.dataflow.confidentiality.transformation.workflow.blackboards.KeyValueMDSDBlackboard;

public class UncertaintyAnalysisWorkflow {
	public static void main(String[] args) throws JobFailedException, UserCanceledException, StandaloneInitializationException, IOException {
		init();
		
		SequentialBlackboardInteractingJob<KeyValueMDSDBlackboard> jobSequence = new SequentialBlackboardInteractingJob<KeyValueMDSDBlackboard>();
		KeyValueMDSDBlackboard blackboard = new KeyValueMDSDBlackboard();
		blackboard.put("resultViolationsKey", new ArrayList<ActionBasedQueryResult>());
		jobSequence.setBlackboard(blackboard);
		
		RunOnlineShopAnalysisJob shopJob = new RunOnlineShopAnalysisJob();
		
		long scenarioCount = Files.find(Paths.get("scenarios"), 1, (path, attributes) -> attributes.isDirectory()).count() - 1;
		
		for (int i = (int) (scenarioCount - 1); i >= 0; i--) {
			final var allocationURI = TestInitializer.getModelURI("scenarios/configuration_" + Integer.toString(i) + "/default.allocation");
			final var usageURI = TestInitializer.getModelURI("scenarios/configuration_" + Integer.toString(i) + "/default.usagemodel");
			
			DataflowAnalysisJob dataflowJob = new DataflowAnalysisJob(allocationURI, usageURI, shopJob);
			jobSequence.add(dataflowJob);
		}
		
		UncertaintyAnalysisJob job = new UncertaintyAnalysisJob(new NaiveResultInterpretation());
		
		jobSequence.add(job);
		
		Workflow workflow = new Workflow(jobSequence);
		
		NullProgressMonitor monitor = new NullProgressMonitor();
		workflow.execute(monitor);
	}
	
	private static void init() throws StandaloneInitializationException {
		BasicConfigurator.resetConfiguration();
		StandaloneInitializerBuilder.builder()
        .registerProjectURI(Activator.class,
                "edu.kit.kastel.dsis.fluidtrust.uncertainty.variation.analysis")
        .addCustomTask(new Log4jInitilizationTask()).build().init();
		
		try {
			new EMFProfileInitializationTask("org.palladiosimulator.dataflow.confidentiality.pcm.model.profile",
					"profile.emfprofile_diagram").initilizationWithoutPlatform();
		} catch (final StandaloneInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DDDslStandaloneSetup.doSetup();
		try {
			new Log4jInitilizationTask().initilizationWithoutPlatform();
		} catch (StandaloneInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d{HH:mm:ss,SSS} %m%n")));
		Logger.getLogger(AbstractInternalAntlrParser.class).setLevel(Level.WARN);
		Logger.getLogger(DefaultLinkingService.class).setLevel(Level.WARN);
		Logger.getLogger(ResourceSetBasedAllContainersStateProvider.class).setLevel(Level.WARN);
		Logger.getLogger(AbstractCleaningLinker.class).setLevel(Level.WARN);
	}
}
