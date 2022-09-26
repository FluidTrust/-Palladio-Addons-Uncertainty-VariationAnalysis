package edu.kit.kastel.dsis.fluidtrust.uncertainty.result.interpretation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
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
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequenceElement;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.SEFFActionSequenceElementImpl;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.dataflow.analysis.ViolatedConstraintsActionSequence;
import utility.AnalysisUtility;

public class Step3ResultInterpretation implements ResultInterpretation {

	@Override
	public Object getInterpretation(ArrayList<ActionBasedQueryResult> violations) {
		Step2ResultInterpretation previousInterpretation = new Step2ResultInterpretation();
		previousInterpretation.getInterpretation(violations);

		// Get variation Points from uncertainty model
		var variationPoints = this.getVariationPoints();

		HashSet<String> uncertaintyPointIds = new HashSet<String>();
		HashMap<String, VariationPoint> uncertaintyPointIdsToVariationPoint = new HashMap<>();
		HashSet<String> uncertaintyNames = new HashSet<String>(); // use this to identify non influencing uncertainties

		for (VariationPoint vp : variationPoints) {
			var varyingSubjects = vp.getVaryingSubjects();
			uncertaintyNames.add(vp.getEntityName());
			for (var varyingSubject : varyingSubjects) {
				String id = varyingSubject.getId();
				uncertaintyPointIds.add(id);
				uncertaintyPointIdsToVariationPoint.put(id, vp);
			}

		}

		HashSet<VariationPoint> influencingUncertainties = new HashSet<>();
		HashSet<String> occuringStrings = new HashSet<>();

		// Collect the reason why a violation occured and get all the uncertainty points
		// lying on the sequence
		for (var violation : violations) {
			for (var result : violation.getResults().entrySet()) {
				ActionSequence actionSequence = result.getKey();

				ArrayList<ViolatedConstraintsActionSequence> violatedConstraintsList = this
						.getViolatedConstraintSequenceFromActionSequence(actionSequence);
				ArrayList<LiteralImpl> occuringLiterals = new ArrayList<>();
				ArrayList<ActionSequenceElement<?>> occuringSequenceElements = new ArrayList<>();
				violatedConstraintsList.forEach(vC -> {
					occuringLiterals.addAll(vC.getLiterals());
					occuringSequenceElements.addAll(vC.getOccuringElements());
				});

				occuringStrings.add(this.createCombinedString(occuringSequenceElements, occuringLiterals));

				// Get all ids of the elements in the action sequence
				HashSet<String> violationIds = AnalysisUtility.getIdsFromActionSequence(actionSequence);

				for (var violationId : violationIds) {
					for (var uncertaintyPointId : uncertaintyPointIds) {
						// search for ids in the variation points
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

		System.out.println("The following uncertainties contribute to these violations:");
		influencingUncertainties.forEach(iu -> System.out.println(" - " + iu.getEntityName()));
		
		System.out.println("\n");

		System.out.println("The following uncertainties do not contribute to these violations:");
		uncertaintyNames.stream()
				.filter(name -> influencingUncertainties.stream().filter(u -> u.getEntityName().equals(name))
						.collect(Collectors.toList()).size() == 0)
				.collect(Collectors.toList()).forEach(name -> System.out.println(" - " + name));

		return null;
	}

	/*
	 * Creates the output string that contains the element where the violation
	 * occurred and which literals were set
	 */
	private String createCombinedString(ArrayList<ActionSequenceElement<?>> occuringSequenceElements,
			ArrayList<LiteralImpl> occuringLiterals) {
		String occuringElementString = this.createOccuringElementString(occuringSequenceElements);
		String literalString = this.createLiteralString(occuringLiterals);

		return occuringElementString + " with literals: " + literalString;
	}

	/*
	 * Loads the uncertainty model and returns the cointaining variation points
	 */
	private EList<VariationPoint> getVariationPoints() {
		final URI umURI = URI.createPlatformPluginURI(
				"/Palladio-Addons-Uncertainty-VariationAnalysis/models/My.uncertaintyvariationmodel", false);

		ResourceAbstraction rs = new ModelResourceAbstraction();
		VariationManager vm = new VariationManager(umURI, rs);

		UncertaintyVariationsImpl uncertaintyModel = (UncertaintyVariationsImpl) vm.loadUncertaintyVariantModel();
		return uncertaintyModel.getVariationPoints();
	}

	/*
	 * Gets the last element of an action sequence as we know that it must be a
	 * violated constraint action sequence
	 */
	private ArrayList<ViolatedConstraintsActionSequence> getViolatedConstraintSequenceFromActionSequence(
			ActionSequence actionSequence) {
		ArrayList<ViolatedConstraintsActionSequence> filteredSequences = new ArrayList<>();
		actionSequence.forEach(e -> {
			if (e instanceof ViolatedConstraintsActionSequence) {
				filteredSequences.add((ViolatedConstraintsActionSequence) e);
			}
		});
		ViolatedConstraintsActionSequence violatedConstraints = (ViolatedConstraintsActionSequence) actionSequence
				.get(actionSequence.size() - 1);
		return filteredSequences;
	}

	private String createOccuringElementString(ArrayList<ActionSequenceElement<?>> occuringSequenceElements) {
		String occuringElementString = "";

		int n = 0;
		for (ActionSequenceElement<?> occuringElementSequence : occuringSequenceElements) {
			EntityImpl occuringElement = (EntityImpl) occuringElementSequence.getElement();
			var occuringName = occuringElement.getEntityName();
			var locationName = ((BasicComponent) occuringElement.eContainer().eContainer()).getEntityName();
			if (n != 0) {
				occuringElementString += ", ";
			}
			occuringElementString += occuringName + " on " + locationName;
			n++;
		}

		return occuringElementString;
	}

	private String createLiteralString(ArrayList<LiteralImpl> occuringLiterals) {
		String literalString = "";
		int i = 0;
		for (Literal literal : occuringLiterals) {
			if (i != 0) {
				literalString += ", ";
			}
			literalString += literal.getName();
			i++;
		}
		return literalString;
	}
}
