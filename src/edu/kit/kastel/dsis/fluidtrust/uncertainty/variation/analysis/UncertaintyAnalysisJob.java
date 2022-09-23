package edu.kit.kastel.dsis.fluidtrust.uncertainty.variation.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.dataflow.confidentiality.transformation.workflow.blackboards.KeyValueMDSDBlackboard;

import de.uka.ipd.sdq.workflow.jobs.AbstractBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.SequentialBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult.ActionBasedQueryResultDTO;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.result.interpretation.ResultInterpretation;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequence;

public class UncertaintyAnalysisJob extends SequentialBlackboardInteractingJob<KeyValueMDSDBlackboard> {
	ResultInterpretation resultInterpretation;
	
	public UncertaintyAnalysisJob(ResultInterpretation resultInterpretation) {
		this.resultInterpretation = resultInterpretation;
	}

	public void execute(IProgressMonitor monitor) {
		KeyValueMDSDBlackboard blackboard = getBlackboard();
		ArrayList<ActionBasedQueryResult> violations = (ArrayList<ActionBasedQueryResult>) blackboard.get("resultViolationsKey").get();
		
		System.out.println("\n\n\n");
		System.out.println("=======================================================================");
		System.out.println("\n");
		System.out.println("UNCERTAINTY ANALYSIS RESULT");
		System.out.println("\n");
		System.out.println("=======================================================================");
		System.out.println("\n");
		
		this.resultInterpretation.getInterpretation(violations);
		
		System.out.println("\n");
		System.out.println("=======================================================================");
		System.out.println("\n\n\n");
	}
	
	@Override
	public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Uncertainty Analysis";
	}
}
