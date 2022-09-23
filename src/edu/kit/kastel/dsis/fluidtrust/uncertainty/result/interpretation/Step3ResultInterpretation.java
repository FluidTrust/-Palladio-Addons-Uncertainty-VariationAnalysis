package edu.kit.kastel.dsis.fluidtrust.uncertainty.result.interpretation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.emf.common.util.URI;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.impl.LiteralImpl;
import org.palladiosimulator.pcm.allocation.impl.AllocationContextImpl;
import org.palladiosimulator.pcm.core.composition.impl.AssemblyContextImpl;
import org.palladiosimulator.pcm.core.entity.impl.EntityImpl;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.impl.BasicComponentImpl;
import org.palladiosimulator.pcm.seff.impl.BranchActionImpl;
import org.palladiosimulator.pcm.seff.impl.SetVariableActionImpl;
import org.palladiosimulator.pcm.seff.util.SeffSwitch;
import org.palladiosimulator.pcm.uncertainty.variation.UncertaintyVariationModel.gen.pcm.VariationManager;
import org.palladiosimulator.pcm.uncertainty.variation.UncertaintyVariationModel.gen.pcm.adapter.resource.ModelResourceAbstraction;
import org.palladiosimulator.pcm.uncertainty.variation.UncertaintyVariationModel.gen.pcm.adapter.resource.ResourceAbstraction;
import org.palladiosimulator.pcm.usagemodel.impl.BranchImpl;

import UncertaintyVariationModel.VariationPoint;
import UncertaintyVariationModel.impl.UncertaintyVariationsImpl;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.AbstractActionSequenceElement;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequence;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.SEFFActionSequenceElementImpl;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.dataflow.analysis.ViolatingConstraintActionSequence;

public class Step3ResultInterpretation implements ResultInterpretation {

	@Override
	public Object getInterpretation(ArrayList<ActionBasedQueryResult> violations) {
		final URI umURI = URI.createPlatformPluginURI(
				"/edu.kit.kastel.dsis.fluidtrust.uncertainty.variation.analysis/models/My.uncertaintyvariationmodel",
				false);

		ResourceAbstraction rs = new ModelResourceAbstraction();
		VariationManager vm = new VariationManager(umURI, rs);

		UncertaintyVariationsImpl uncertaintyModel = (UncertaintyVariationsImpl) vm.loadUncertaintyVariantModel();
		var variationPoints = uncertaintyModel.getVariationPoints();

		HashSet<String> uncertaintyPointIds = new HashSet<String>();
		HashMap<String, VariationPoint> uncertaintyPointIdsToVariationPoint = new HashMap<>();

		HashSet<VariationPoint> influencingUncertainties = new HashSet<>();

		HashSet<String> occuringStrings = new HashSet<>();

		for (VariationPoint vp : variationPoints) {
			var varyingSubjects = vp.getVaryingSubjects();
			for (var varyingSubject : varyingSubjects) {
				String id = varyingSubject.getId();
				uncertaintyPointIds.add(id);
				uncertaintyPointIdsToVariationPoint.put(id, vp);
			}

		}

		for (var violation : violations) {
			var entrySet = violation.getResults().entrySet();

			for (var result : entrySet) {
				ActionSequence key = result.getKey();

				// TODO: MISSING INFORMATION about which uncertainty modifies which which
				// literal or architectural decision
				ViolatingConstraintActionSequence violatedConstraint = (ViolatingConstraintActionSequence) key
						.get(key.size() - 1);
				ArrayList<LiteralImpl> occuringLiterals = violatedConstraint.getLiterals();
				AbstractActionSequenceElement occuringElement = (SEFFActionSequenceElementImpl) violatedConstraint
						.getOccuringElement();
				key.remove(key.size() - 1);

				EntityImpl element = (EntityImpl) occuringElement.getElement();
				var name = element.getEntityName();
				var name2 = ((BasicComponent) element.eContainer().eContainer()).getEntityName();

				String literalString = "";

				int i = 0;
				for (Literal literal : occuringLiterals) {
					if (i == 0) {
						literalString = literal.getName();
					} else {
						literalString += ", " + literal.getName();
					}
					i++;
				}

				String combined = name + " on " + name2 + " with literals: " + literalString;
				occuringStrings.add(combined);

				var x = 1;

				HashSet<String> violationIds = AnalysisUtility.getIdsFromEntrySet(key);

				for (var violationId : violationIds) {
					for (var uncertaintyPointId : uncertaintyPointIds) {
						if (violationId.equals(uncertaintyPointId)) {
							influencingUncertainties.add(uncertaintyPointIdsToVariationPoint.get(uncertaintyPointId));
						}
					}
				}
			}
		}

		// PRINT OUTPUT AFTER ANALYSIS
		System.out.println("Where and with which literals do violations occur?");
		occuringStrings.forEach(os -> System.out.println(os));

		System.out.println("\n");

		System.out.println("The following uncertainties contribute to violations:");
		influencingUncertainties.forEach(iu -> System.out.println("Entity Name: " + iu.getEntityName()));

		return null;
	}

}
