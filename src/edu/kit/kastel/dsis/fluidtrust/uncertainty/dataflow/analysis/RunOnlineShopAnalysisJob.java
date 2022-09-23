package edu.kit.kastel.dsis.fluidtrust.uncertainty.dataflow.analysis;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.dictionary.PCMDataDictionary;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.impl.LiteralImpl;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.seff.impl.SetVariableActionImpl;
import org.palladiosimulator.pcm.seff.util.SeffSwitch;

import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequence;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.CharacteristicValue;

public class RunOnlineShopAnalysisJob extends RunCustomJavaBasedAnalysisJob {

	@Override
	protected edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult findViolations(
			List<PCMDataDictionary> dataDictionaries, ActionBasedQueryResult allCharacteristics)
			throws JobFailedException {
		var enumCharacteristicTypes = getAllEnumCharacteristicTypes(dataDictionaries);
		//System.out.println("\n\nCHARACTERISTIC TYPES --------------------");
		//enumCharacteristicTypes.forEach(entry -> System.out.println(entry.getName()));

		var ctServerLocation = findByName(enumCharacteristicTypes, "ServerLocation");
		var ctDataSensitivity = findByName(enumCharacteristicTypes, "DataSensitivity");
		var ctDataEncryption = findByName(enumCharacteristicTypes, "DataEncryption");

		var violations = new ActionBasedQueryResult();

		//System.out.println("\n\nELEMENTS AND CHARACTERISTICS --------------------");

		for (var resultEntry : allCharacteristics.getResults().entrySet()) {
			var x = 1;
			for (var queryResult : resultEntry.getValue()) {

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

				var element = queryResult.getElement().getElement();
				var elementString = getElementRepresentation(queryResult.getElement());

				//System.out.println(
				//		elementString + ", " + serverLocations.toString() + ", " + dataSensitivites.toString());

				// Actual constraint
				var violatingSequence = new ViolatingConstraintActionSequence();
				boolean violationOccured = false;
				if (serverLocations.contains("nonEU") && dataSensitivites.contains("Personal")) {
					violatingSequence.addLiteral((LiteralImpl) serverLocationLiterals.get(0));
					violatingSequence.addLiteral((LiteralImpl) dataSensivityLiterals.get(0));
					violatingSequence.setOccuringElement(queryResult.getElement());
					violationOccured = true;
				} else if (element instanceof SetVariableActionImpl && dataEncryptions.contains("NonEncrypted")
						&& !dataEncryptions.contains("Encrypted")) {
					violatingSequence.addLiteral((LiteralImpl) dataEncryptionLiterals.get(0));
					violatingSequence.setOccuringElement(queryResult.getElement());
					violationOccured = true;
				}
				if (violationOccured) {
					resultEntry.getKey().add(violatingSequence);
					violations.addResult(resultEntry.getKey(), queryResult);
				}
			}
		}

		/*System.out.println("\n\nVIOLATIONS --------------------");
		violations.getResults().forEach((sequence, resultDTO) -> {
			resultDTO.forEach(it -> System.out.println(getElementRepresentation(it.getElement())));

			System.out.println("-- with sequence --");

			// Find path from newest entry level system call (might not be enough though)
			var pruneSequence = sequence.stream().takeWhile(
					it -> !resultDTO.stream().map(e -> e.getElement()).collect(Collectors.toList()).contains(it))
					.collect(Collectors.toList());

			var sysCalls = pruneSequence.stream()
					.filter(e -> ((Entity) e.getElement()).eClass().getName().equals("EntryLevelSystemCall"))
					.collect(Collectors.toList());
			var lastEntryLevelSystemCall = sysCalls.stream().skip(sysCalls.stream().count() - 1).findFirst().get();

			var finalSequence = pruneSequence.stream().dropWhile(it -> !it.equals(lastEntryLevelSystemCall));

			finalSequence.forEach(it -> System.out.println(getElementRepresentation(it)));

			System.out.println("\n\n");
		});*/

		return violations;
	}

}
