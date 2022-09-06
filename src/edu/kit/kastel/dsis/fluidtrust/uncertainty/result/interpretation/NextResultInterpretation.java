package edu.kit.kastel.dsis.fluidtrust.uncertainty.result.interpretation;

import java.util.ArrayList;

import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.impl.EnumerationImpl;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.impl.LiteralImpl;
import org.palladiosimulator.pcm.repository.impl.BasicComponentImpl;
import org.palladiosimulator.pcm.seff.impl.ExternalCallActionImpl;
import org.palladiosimulator.pcm.seff.impl.GuardedBranchTransitionImpl;
import org.palladiosimulator.pcm.seff.impl.SetVariableActionImpl;
import org.palladiosimulator.pcm.usagemodel.impl.BranchTransitionImpl;
import org.palladiosimulator.pcm.usagemodel.impl.EntryLevelSystemCallImpl;

import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.AbstractActionSequenceElement;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult;

public class NextResultInterpretation implements ResultInterpretation {

	@Override
	public Object getInterpretation(ArrayList<ActionBasedQueryResult> violations) {
		for (ActionBasedQueryResult violation : violations) {
			var entrySet = violation.getResults().entrySet();
			var contextString = "";
			var elementString = "";
			var test2String = "";
			int i = 0;
			
			for (var result : entrySet) {
				var key = result.getKey();
				var value = result.getValue();
				
				for (var element : key) {
					var test = element.getElement().eContainmentFeature().getName();
					
					var element2 = element.getElement();
					var elementName = "";
					if (element2 instanceof EntryLevelSystemCallImpl) {
						elementName = ((EntryLevelSystemCallImpl) element2).getEntityName();
					} else if (element2 instanceof ExternalCallActionImpl) {
						elementName = ((ExternalCallActionImpl) element2).getEntityName();
					} else if (element2 instanceof SetVariableActionImpl) {
						elementName = ((SetVariableActionImpl) element2).getEntityName();
					}
					
					var test2 = element2.eContainer().eContainer();
					var test2Name = "";
					
					if (test2 instanceof BasicComponentImpl) {
						test2Name = ((BasicComponentImpl) test2).getEntityName();
					} else if (test2 instanceof BranchTransitionImpl) {
						test2Name = ((BranchTransitionImpl) test2).getBranch_BranchTransition().getEntityName();
					} else if (test2 instanceof GuardedBranchTransitionImpl) {
						test2Name = ((GuardedBranchTransitionImpl) test2).getEntityName();
					} else {
						int n = 1;
					}
					
					if (i != 0) {
						elementString += " -> " + elementName;
						test2String += " -> " + test2Name;
					} else {
						elementString += elementName;
						test2String += test2Name;
					}
					
					for (var context : element.getContext()) {
						if (i != 0) {
							contextString = contextString + " -> " + context.getEntityName();
						} else {
							contextString = contextString + context.getEntityName();
						}
						
						i++;
					}
				}
				
				// Get Constraint Name
				var violationLiteralName = "";
				var typeName = "";
				var literalNames = new ArrayList<String>();
				
				for (var entry : value) {
					var nodeCharacteristics = entry.getNodeCharacteristics();
					for (var node : nodeCharacteristics) {
						var violationLiteral = node.getCharacteristicLiteral();
						var type = node.getCharacteristicType();
						
						var literalEnum = (EnumerationImpl) violationLiteral.getEnum();
						var literals = literalEnum.getLiterals();
						
						for (Literal literal : literals) {
							literalNames.add(literal.getName());
						}

						violationLiteralName = violationLiteral.getName();
						typeName = type.getName();
						
						literalNames.remove(literalNames.indexOf(violationLiteralName));
						
					}
				}
				
				System.out.println("VIOLATION: " + typeName + " - " + violationLiteralName);
				System.out.println("SHOULD BE: " + literalNames.toString() + "\n");
				System.out.println("CALL SEQUENCE");
				System.out.println(elementString + "\n");
				System.out.println("???");
				System.out.println(test2String + "\n");
				System.out.println("AFFECTED OBJECTS");
				System.out.println(contextString);
				System.out.println("\n\n\n");
			}
		}
		return null;
	}

}
