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
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.dataflow.analysis.DataflowAnalysisJob;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.dataflow.analysis.RunOnlineShopAnalysisJob;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.result.interpretation.Step4ResultInterpretation;
import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;
import tools.mdsd.library.standalone.initialization.emfprofiles.EMFProfileInitializationTask;
import tools.mdsd.library.standalone.initialization.log4j.Log4jInitilizationTask;
import org.palladiosimulator.dataflow.confidentiality.transformation.workflow.blackboards.KeyValueMDSDBlackboard;

public class UncertaintyAnalysisWorkflow {
	public static void main(String[] args) throws JobFailedException, UserCanceledException, StandaloneInitializationException, IOException {
		init();
		
		// Create a job sequence which contains first the dataflow analysis jobs and then the uncertainty analysis job
		SequentialBlackboardInteractingJob<KeyValueMDSDBlackboard> jobSequence = new SequentialBlackboardInteractingJob<KeyValueMDSDBlackboard>();
		// A blackboard is used to share data between jobs
		KeyValueMDSDBlackboard blackboard = new KeyValueMDSDBlackboard();
		// resultViolationsKey will contain the violations
		blackboard.put("resultViolationsKey", new ArrayList<ActionBasedQueryResult>());
		blackboard.put("resultNoViolationsKey", new ArrayList<ActionBasedQueryResult>());
		jobSequence.setBlackboard(blackboard);
		
		RunOnlineShopAnalysisJob shopJob = new RunOnlineShopAnalysisJob();
		
		// Get the amount of generated scenarios
		long scenarioCount = Files.find(Paths.get("scenarios"), 1, (path, attributes) -> attributes.isDirectory()).count() - 1;
		
		// Create a dataflow analysis job for every scenario
		for (int i = 0; i < scenarioCount; i++) {
			final var allocationURI = TestInitializer.getModelURI("scenarios/configuration_" + Integer.toString(i) + "/default.allocation");
			final var usageURI = TestInitializer.getModelURI("scenarios/configuration_" + Integer.toString(i) + "/default.usagemodel");
			
			DataflowAnalysisJob dataflowJob = new DataflowAnalysisJob(allocationURI, usageURI, shopJob, i);
			jobSequence.add(dataflowJob);
		}
		
		// We insert the wanted interpretation into the constructor
		UncertaintyAnalysisJob job = new UncertaintyAnalysisJob(new Step4ResultInterpretation());
		
		jobSequence.add(job);
		
		Workflow workflow = new Workflow(jobSequence);
		
		NullProgressMonitor monitor = new NullProgressMonitor();
		workflow.execute(monitor);
	}
	
	/*
	 * Some initializing stuff copied from the dataflow analysis
	 */
	private static void init() throws StandaloneInitializationException {
		BasicConfigurator.resetConfiguration();
		StandaloneInitializerBuilder.builder()
        .registerProjectURI(Activator.class,
                "Palladio-Addons-Uncertainty-VariationAnalysis")
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
