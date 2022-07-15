package edu.kit.kastel.dsis.fluidtrust.uncertainty.variation.analysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.Test;

import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import edu.kit.kastel.dsis.fluidtrust.dataflow.analysis.DataflowAnalysisWorkflow;
import edu.kit.kastel.dsis.fluidtrust.dataflow.analysis.RunOnlineShopAnalysisJob;

public class ConfidentialityAnalysisTest extends TestBase {
	@Override
	protected List<String> getModelsPath() {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	}

	@Override
	protected void assignValues(List<Resource> list) {
		// TODO Auto-generated method stub
	}
	
	@Test
	public void testOnlineShop() throws JobFailedException, UserCanceledException {
		System.out.println("\n------------------------------------------------------------------------------------");
		System.out.println("Running Online Shop Analysis:\n");
		
		final var allocationURI = TestInitializer.getModelURI("models/InternationalOnlineShop/default.allocation");
		final var usageURI = TestInitializer.getModelURI("models/InternationalOnlineShop/default.usagemodel");
		final var workflow = new DataflowAnalysisWorkflow(allocationURI, usageURI, new RunOnlineShopAnalysisJob());
		workflow.execute(new NullProgressMonitor());
	}
}
