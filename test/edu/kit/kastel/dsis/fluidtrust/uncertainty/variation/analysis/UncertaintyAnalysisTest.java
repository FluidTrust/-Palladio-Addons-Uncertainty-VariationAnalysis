package edu.kit.kastel.dsis.fluidtrust.uncertainty.variation.analysis;

import org.junit.jupiter.api.Test;

import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.dataflow.analysis.DataflowAnalysisWorkflow;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.dataflow.analysis.RunOnlineShopAnalysisJob;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;

public class UncertaintyAnalysisTest extends TestBase {	
	@Test
	public void test() throws JobFailedException, UserCanceledException, IOException {
		System.out.println("RUNNING ONLINE SHOP UNCERTAINTY ANALYSIS");
		System.out.println("\n------------------------------------------------------------------------------------");
		
		File file = new File("scenarios");
		long count = Files.find(Paths.get("scenarios"), 1, (path, attributes) -> attributes.isDirectory()).count() - 1;
		
		for (int i = 0; i < count; i++) {
			System.out.println("\nANALYZING CONFIGURATION " + Integer.toString(i));
			System.out.println("\n------------------------------------------------------------------------------------");
			final var allocationURI = TestInitializer.getModelURI("scenarios/configuration_" + Integer.toString(i) + "/default.allocation");
			final var usageURI = TestInitializer.getModelURI("scenarios/configuration_" + Integer.toString(i) + "/default.usagemodel");
			final var workflow = new DataflowAnalysisWorkflow(allocationURI, usageURI, new RunOnlineShopAnalysisJob());
			NullProgressMonitor monitor = new NullProgressMonitor();
			workflow.execute(monitor);
		}
	}

	@Override
	protected List<String> getModelsPath() {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	}

	@Override
	protected void assignValues(List<Resource> list) {
		// TODO Auto-generated method stub
		
	}
}
