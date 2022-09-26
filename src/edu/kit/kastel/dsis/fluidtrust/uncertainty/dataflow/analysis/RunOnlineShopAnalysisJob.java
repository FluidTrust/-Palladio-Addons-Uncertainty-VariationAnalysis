package edu.kit.kastel.dsis.fluidtrust.uncertainty.dataflow.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.dictionary.PCMDataDictionary;
import org.palladiosimulator.dataflow.confidentiality.transformation.workflow.blackboards.KeyValueMDSDBlackboard;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.impl.LiteralImpl;
import org.palladiosimulator.pcm.seff.impl.SetVariableActionImpl;

import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult.ActionBasedQueryResultDTO;
import utility.AnalysisUtility;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequence;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequenceElement;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.CharacteristicValue;

public class RunOnlineShopAnalysisJob extends RunCustomJavaBasedAnalysisJob {

	@Override
	protected ActionBasedQueryResult findViolations(List<PCMDataDictionary> dataDictionaries,
			ActionBasedQueryResult allCharacteristics, KeyValueMDSDBlackboard blackboard) throws JobFailedException {
		var enumCharacteristicTypes = getAllEnumCharacteristicTypes(dataDictionaries);

		var ctServerLocation = findByName(enumCharacteristicTypes, "ServerLocation");
		var ctDataSensitivity = findByName(enumCharacteristicTypes, "DataSensitivity");
		var ctDataEncryption = findByName(enumCharacteristicTypes, "DataEncryption");

		var violations = new ActionBasedQueryResult();
		var noViolations = new ActionBasedQueryResult();

		// System.out.println("\n\nELEMENTS AND CHARACTERISTICS --------------------");

		for (var resultEntry : allCharacteristics.getResults().entrySet()) {
			// We use these variables to split action sequences. We add elements until a
			// violation occurs. Then we create a new action sequence. If a violation occurs
			// again, we concat the new action sequence with the one before that. Otherwise
			// we can add the remaining part of the action sequence as a no violation
			// sequence. This improves analysis results.
			int actionSequenceIndex = 0;
			ArrayList<ActionSequence> actionSequences = new ArrayList<>();
			ArrayList<ArrayList<ActionBasedQueryResultDTO>> queryResults = new ArrayList<>();
			boolean overallViolation = false;

			for (var queryResult : resultEntry.getValue()) {
				try {
					actionSequences.get(actionSequenceIndex);
				} catch (IndexOutOfBoundsException e) {
					// this case occurs if we start the algorithm or if a new action sequence must
					// be added
					actionSequences.add(actionSequenceIndex, new ActionSequence(new ArrayList<>()));
					queryResults.add(actionSequenceIndex, new ArrayList<>());
				}

				ActionSequence currentActionSequence = actionSequences.get(actionSequenceIndex);
				var currentQueryResultList = queryResults.get(actionSequenceIndex);

				ActionSequenceElement<?> actionSequenceElement = queryResult.getElement();
				currentActionSequence.add(actionSequenceElement);

				// Fetch all Characteristics
				var serverLocations = queryResult.getNodeCharacteristics().stream()
						.filter(cv -> cv.getCharacteristicType() == ctServerLocation)
						.map(CharacteristicValue::getCharacteristicLiteral).map(it -> it.getName())
						.collect(Collectors.toList());

				var serverLocationLiterals = queryResult.getNodeCharacteristics().stream()
						.filter(cv -> cv.getCharacteristicType() == ctServerLocation)
						.map(CharacteristicValue::getCharacteristicLiteral).collect(Collectors.toList());

				var dataSensitivites = queryResult.getDataCharacteristics().values().stream()
						.flatMap(Collection::stream).filter(cv -> cv.getCharacteristicType() == ctDataSensitivity)
						.map(CharacteristicValue::getCharacteristicLiteral).map(it -> it.getName())
						.collect(Collectors.toList());

				var dataSensivityLiterals = queryResult.getDataCharacteristics().values().stream()
						.flatMap(Collection::stream).filter(cv -> cv.getCharacteristicType() == ctDataSensitivity)
						.map(CharacteristicValue::getCharacteristicLiteral).collect(Collectors.toList());

				var dataEncryptions = queryResult.getDataCharacteristics().values().stream().flatMap(Collection::stream)
						.filter(cv -> cv.getCharacteristicType() == ctDataEncryption)
						.map(CharacteristicValue::getCharacteristicLiteral).map(it -> it.getName())
						.collect(Collectors.toList());

				var dataEncryptionLiterals = queryResult.getDataCharacteristics().values().stream()
						.flatMap(Collection::stream).filter(cv -> cv.getCharacteristicType() == ctDataEncryption)
						.map(CharacteristicValue::getCharacteristicLiteral).collect(Collectors.toList());

				// Get the action element from the seff action sequence element
				var element = actionSequenceElement.getElement();

				/*
				 * Define the constraints here. We use a sequence contrary to its purpose
				 * because restructuring all the classes does not work in an easy way We would
				 * need to change ActionBasedQueryResult, to store the literals and locations
				 * together with the sequence That requires also a change of
				 * CharacteristicsQueryEngine but then we get Problems with
				 * ActionSequenceQueryUtils...
				 */
				var violatingSequenceElement = new ViolatedConstraintsActionSequence();
				boolean violationOccured = false;
				if (serverLocations.contains("nonEU") && dataSensitivites.contains("Personal")) {
					violatingSequenceElement.addLiteral((LiteralImpl) serverLocationLiterals.get(0));
					violatingSequenceElement.addLiteral((LiteralImpl) dataSensivityLiterals.get(0));
					violatingSequenceElement.addOccuringElement(actionSequenceElement);
					violationOccured = true;
				} else if (element instanceof SetVariableActionImpl && dataEncryptions.contains("NonEncrypted")
						&& !dataEncryptions.contains("Encrypted")) {
					violatingSequenceElement.addLiteral((LiteralImpl) dataEncryptionLiterals.get(0));
					violatingSequenceElement.addOccuringElement(actionSequenceElement);
					violationOccured = true;
				}
				if (violationOccured) {
					// Put the information about the violation into the resultEntry at the last
					// index
					currentActionSequence.add(violatingSequenceElement);
					currentQueryResultList.add(queryResult);

					overallViolation = true;

					if (actionSequenceIndex != 0) { // concat with previous element
						var previousActionSequence = actionSequences.get(actionSequenceIndex - 1);
						previousActionSequence.addAll(currentActionSequence);

						var previousQueryResult = queryResults.get(actionSequenceIndex - 1);
						previousQueryResult.addAll(currentQueryResultList);

						actionSequences.remove(actionSequenceIndex);
						queryResults.remove(actionSequenceIndex);
					} else { // increase
						actionSequenceIndex++;
					}
				}
			}

			// if there was no violation, we only have one element
			if (overallViolation) {
				// otherwise there are two elements -> TODO: Refactor without the lists
				for (var queryResult : queryResults.get(0)) {
					violations.addResult(actionSequences.get(0), queryResult);
				}

				if (actionSequences.get(1) != null) {
					noViolations.addResult(actionSequences.get(1), new ActionBasedQueryResultDTO(null, null, null));
				}
			} else {
				noViolations.addResult(actionSequences.get(0), new ActionBasedQueryResultDTO(null, null, null));
			}

		}

		// Add violating sequences and non violating sequences to the blackboard
		if (!noViolations.getResults().isEmpty()) {
			ArrayList<ActionBasedQueryResult> occuredViolations = (ArrayList<ActionBasedQueryResult>) getBlackboard()
					.get(AnalysisUtility.NO_VIOLATIONS_KEY).get();
			occuredViolations.add(noViolations);
		}
		if (!violations.getResults().isEmpty()) {
			ArrayList<ActionBasedQueryResult> occuredViolations = (ArrayList<ActionBasedQueryResult>) getBlackboard()
					.get(AnalysisUtility.VIOLATIONS_KEY).get();
			occuredViolations.add(violations);
		}

		return violations;
	}

}
