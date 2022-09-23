package edu.kit.kastel.dsis.fluidtrust.uncertainty.result.interpretation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult.ActionBasedQueryResultDTO;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequence;

public class NaiveResultInterpretation implements ResultInterpretation {

	@Override
	public Object getInterpretation(ArrayList<ActionBasedQueryResult> violations) {
		boolean isEmpty = true;
		for (ActionBasedQueryResult entry : violations) {
			if (!entry.getResults().isEmpty()) {
				isEmpty = false;
				break;
			}
		}
		if (isEmpty) {
			System.out.println("The architectural model contains no violations. Ready to use!");
		} else {
			System.out.println("The architectural model contains violations. Do not use!");
		}
		return null;
	}

}
