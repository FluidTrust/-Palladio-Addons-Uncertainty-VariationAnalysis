package edu.kit.kastel.dsis.fluidtrust.uncertainty.result.interpretation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.palladiosimulator.pcm.seff.impl.ResourceDemandingSEFFImpl;
import org.palladiosimulator.pcm.seff.impl.SetVariableActionImpl;
import org.palladiosimulator.pcm.uncertainty.variation.UncertaintyVariationModel.gen.pcm.VariationManager;
import org.palladiosimulator.pcm.uncertainty.variation.UncertaintyVariationModel.gen.pcm.adapter.resource.ModelResourceAbstraction;
import org.palladiosimulator.pcm.uncertainty.variation.UncertaintyVariationModel.gen.pcm.adapter.resource.ResourceAbstraction;

import UncertaintyVariationModel.Value;
import UncertaintyVariationModel.VariationPoint;
import UncertaintyVariationModel.impl.PrimitiveValueImpl;
import UncertaintyVariationModel.impl.UncertaintyVariationsImpl;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequence;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.SEFFActionSequenceElementImpl;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.dataflow.analysis.ViolatingConstraintActionSequence;

public class Step3_1ResultInterpretation implements ResultInterpretation {

	@Override
	public Object getInterpretation(ArrayList<ActionBasedQueryResult> violations) {
		final URI umURI = URI.createPlatformPluginURI(
				"/edu.kit.kastel.dsis.fluidtrust.uncertainty.variation.analysis/models/My.uncertaintyvariationmodel",
				false);

		ResourceAbstraction rs = new ModelResourceAbstraction();
		VariationManager vm = new VariationManager(umURI, rs);

		UncertaintyVariationsImpl uncertaintyModel = (UncertaintyVariationsImpl) vm.loadUncertaintyVariantModel();
		var uncertaintyVarationPoints = uncertaintyModel.getVariationPoints();

		HashSet<String> uncertaintyPointIds = new HashSet<String>();
		HashMap<String, VariationPoint> uncertaintyPointIdsToVariationPoint = new HashMap<String, VariationPoint>();

		String variationPoints = "";
		for (VariationPoint variationPoint : uncertaintyVarationPoints) {
			var varyingSubjects = variationPoint.getVaryingSubjects();
			if (variationPoint.getVariationDescription() != null) { // Otherwise its a branch
				var variationDescription = variationPoint.getVariationDescription();
				var targetVariations = variationDescription.getTargetVariations();
				for (var targetVariation : targetVariations) {
					var link = ((PrimitiveValueImpl) targetVariation).getLink();
					var linkId = link.getId();
					uncertaintyPointIds.add(linkId);
					uncertaintyPointIdsToVariationPoint.put(linkId, variationPoint);
				}
			} else {
				// TODO: BRANCH CASE
			}
		}

		for (var violation : violations) {
			var entrySet = violation.getResults().entrySet();
			for (var result : entrySet) {
				ActionSequence key = result.getKey();

				HashSet<String> violationIds = new HashSet<String>();

				ViolatingConstraintActionSequence violatingConstraint = (ViolatingConstraintActionSequence) key
						.get(key.size() - 1);
				// TODO: In Zukunft könnte hier auch etwas anderes auftreten, als SEFFActionSequence...
				SEFFActionSequenceElementImpl occuringElement = (SEFFActionSequenceElementImpl) violatingConstraint
						.getOccuringElement();
				key.remove(key.size() - 1);

				SetVariableActionImpl test = (SetVariableActionImpl) occuringElement.getElement();
				ResourceDemandingSEFFImpl a = (ResourceDemandingSEFFImpl) test.getResourceDemandingBehaviour_AbstractAction();
				var b = a.getBasicComponent_ServiceEffectSpecification();
				System.out.println("----------");
				
				var x = 1;
				
				
				
				
				
				
				
				
				
				
				
				HashSet<VariationPoint> influencingUncertainties = new HashSet<VariationPoint>();

				for (var violationId : violationIds) {
					for (var uncertaintyPointId : uncertaintyPointIds) {
						if (violationId.equals(uncertaintyPointId)) {
							influencingUncertainties.add(uncertaintyPointIdsToVariationPoint.get(uncertaintyPointId));
							int i = 1;
						}
					}
				}

				//influencingUncertainties.forEach(u -> System.out.println(u.getEntityName()));
				//System.out.println("\n---------\n");
			}
		}

		return null;
	}

}
