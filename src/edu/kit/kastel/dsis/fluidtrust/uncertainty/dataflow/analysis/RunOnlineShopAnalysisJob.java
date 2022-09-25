package edu.kit.kastel.dsis.fluidtrust.uncertainty.dataflow.analysis;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.dictionary.PCMDataDictionary;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.impl.LiteralImpl;
import org.palladiosimulator.pcm.seff.impl.SetVariableActionImpl;

import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.CharacteristicValue;

public class RunOnlineShopAnalysisJob extends RunCustomJavaBasedAnalysisJob {

	@Override
	protected ActionBasedQueryResult findViolations(List<PCMDataDictionary> dataDictionaries,
			ActionBasedQueryResult allCharacteristics) throws JobFailedException {
		var enumCharacteristicTypes = getAllEnumCharacteristicTypes(dataDictionaries);

		var ctServerLocation = findByName(enumCharacteristicTypes, "ServerLocation");
		var ctDataSensitivity = findByName(enumCharacteristicTypes, "DataSensitivity");
		var ctDataEncryption = findByName(enumCharacteristicTypes, "DataEncryption");

		var violations = new ActionBasedQueryResult();

		// System.out.println("\n\nELEMENTS AND CHARACTERISTICS --------------------");

		for (var resultEntry : allCharacteristics.getResults().entrySet()) {
			var x = 1;
			for (var queryResult : resultEntry.getValue()) {

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
				var element = queryResult.getElement().getElement();

				/*
				 * Define the constraints here. We use a sequence contrary to its purpose
				 * because restructuring all the classes does not work in an easy way We would
				 * need to change ActionBasedQueryResult, to store the literals and locations
				 * together with the sequence That requires also a change of
				 * CharacteristicsQueryEngine but then we get Problems with
				 * ActionSequenceQueryUtils...
				 */
				var violatingSequence = new ViolatedConstraintsActionSequence();
				boolean violationOccured = false;
				if (serverLocations.contains("nonEU") && dataSensitivites.contains("Personal")) {
					violatingSequence.addLiteral((LiteralImpl) serverLocationLiterals.get(0));
					violatingSequence.addLiteral((LiteralImpl) dataSensivityLiterals.get(0));
					violatingSequence.addOccuringElement(queryResult.getElement());
					violationOccured = true;
				} else if (element instanceof SetVariableActionImpl && dataEncryptions.contains("NonEncrypted")
						&& !dataEncryptions.contains("Encrypted")) {
					violatingSequence.addLiteral((LiteralImpl) dataEncryptionLiterals.get(0));
					violatingSequence.addOccuringElement(queryResult.getElement());
					violationOccured = true;
				}
				if (violationOccured) {
					// Put the information about the violation into the resultEntry at the last index
					resultEntry.getKey().add(violatingSequence);
					violations.addResult(resultEntry.getKey(), queryResult);
				}
			}
		}

		return violations;
	}

}
