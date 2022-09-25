package edu.kit.kastel.dsis.fluidtrust.uncertainty.result.interpretation;

import java.util.ArrayList;
import java.util.HashSet;

import org.palladiosimulator.pcm.core.entity.impl.EntityImpl;

import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequence;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequenceElement;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.dataflow.analysis.ViolatedConstraintsActionSequence;

public class Step2ResultInterpretation implements ResultInterpretation {

	@Override
	public Object getInterpretation(ArrayList<ActionBasedQueryResult> violations) {
		HashSet<String> sequences = new HashSet<>();

		for (ActionBasedQueryResult violation : violations) {
			String elementString = "";
			int i = 0;

			for (var result : violation.getResults().entrySet()) {
				ActionSequence actionSequence = result.getKey();

				for (ActionSequenceElement<?> actionElement : actionSequence) {
					if (!(actionElement instanceof ViolatedConstraintsActionSequence)) {
						String elementName = ((EntityImpl) actionElement.getElement()).getEntityName();

						if (i != 0) {
							elementString += " -> " + elementName;
							i++;
						} else {
							elementString += elementName;
							i++;
						}
					}

				}

				sequences.add(elementString);
			}
		}

		System.out.println("The following call sequences produce violations:");
		sequences.forEach(s -> System.out.println(s));
		System.out.println("\n");

		return null;
	}

}
