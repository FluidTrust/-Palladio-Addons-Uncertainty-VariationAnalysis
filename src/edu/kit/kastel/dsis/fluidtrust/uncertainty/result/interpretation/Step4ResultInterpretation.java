package edu.kit.kastel.dsis.fluidtrust.uncertainty.result.interpretation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.dataflow.analysis.DataflowAnalysisJob;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.variation.analysis.TestInitializer;

public class Step4ResultInterpretation implements ResultInterpretation {

	@Override
	public Object getInterpretation(ArrayList<ActionBasedQueryResult> violations) {
		Step3ResultInterpretation previousInterpretation = new Step3ResultInterpretation();
		previousInterpretation.getInterpretation(violations);	

		// Get the amount of generated scenarios
		try {
			long scenarioCount = Files.find(Paths.get("scenarios"), 1, (path, attributes) -> attributes.isDirectory())
					.count() - 1;
			
			// Create a dataflow analysis job for every scenario
			for (int i = 0; i < scenarioCount; i++) {
				final var allocationURI = TestInitializer.getModelURI("scenarios/configuration_" + Integer.toString(i) + "/default.allocation");
				final var usageURI = TestInitializer.getModelURI("scenarios/configuration_" + Integer.toString(i) + "/default.usagemodel");
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

}
