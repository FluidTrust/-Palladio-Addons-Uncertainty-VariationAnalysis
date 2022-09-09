package edu.kit.kastel.dsis.fluidtrust.uncertainty.result.interpretation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.impl.AssemblyContextImpl;
import org.palladiosimulator.pcm.seff.impl.ExternalCallActionImpl;
import org.palladiosimulator.pcm.seff.impl.SetVariableActionImpl;
import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.impl.EntryLevelSystemCallImpl;

import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequence;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.CallingSEFFActionSequenceElementImpl;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.CallingUserActionSequenceElementImpl;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.SEFFActionSequenceElementImpl;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.dataflow.analysis.ViolatingConstraintActionSequence;

public class NewResultInterpretation implements ResultInterpretation {

	@Override
	public Object getInterpretation(ArrayList<ActionBasedQueryResult> violations) {
		for (ActionBasedQueryResult violation : violations) {
			var entrySet = violation.getResults().entrySet();

			for (var result : entrySet) {
				ActionSequence key = result.getKey();
				// TODO: entry.getValue();

				// Jedes element in key ist ein Assembly.
				// Der Call läuft also über die Interfaces durch die Assemblies durch das
				// System.
				HashMap<String, String> assemblies = new HashMap<>();
				String overallString = "";
				String directString = "";
				String assemblyString = "";
				String encapsulatedString = "";

				var violatingConstraint = key.get(key.size() - 1);
				key.remove(key.size() - 1);

				// TODO: How to properly do this?

				for (var element : key) {
					if (element instanceof CallingUserActionSequenceElementImpl) {
						overallString += "(";
						EntryLevelSystemCallImpl e = (EntryLevelSystemCallImpl) element.getElement();

						Branch branch = e.getScenarioBehaviour_AbstractUserAction()
								.getBranchTransition_ScenarioBehaviour().getBranch_BranchTransition();
						var branchName = branch.getEntityName();
						overallString += branchName + " ";

						var name = e.getEntityName();
						directString += name + " ";
						overallString += name + " ";

						var id = e.getId();

						overallString += ") ";
					} else if (element instanceof CallingSEFFActionSequenceElementImpl) {
						overallString += "(";
						ExternalCallActionImpl e = (ExternalCallActionImpl) element.getElement();

						var name = e.getEntityName();
						directString += name + " ";
						overallString += name + " ";

						Stack<AssemblyContext> c = element.getContext();

						for (var cE : c) {
							var assembly = (AssemblyContextImpl) cE; // Aus Assembly Diagram

							var assemblyName = assembly.getEntityName();
							assemblyString += assemblyName + " ";
							overallString += assemblyName + " ";

							var encapsulatedComponent = assembly.getEncapsulatedComponent__AssemblyContext();
							var eCName = encapsulatedComponent.getEntityName(); // Component aus Repository Diagram
							encapsulatedString += eCName + " ";
							overallString += eCName + " ";

							int i = 1;
						}

						overallString += ") ";
					} else if (element instanceof SEFFActionSequenceElementImpl) {
						overallString += "(";
						SetVariableActionImpl e = (SetVariableActionImpl) element.getElement();

						var name = e.getEntityName();
						directString += name + " ";
						overallString += name + " ";

						Stack<AssemblyContext> c = element.getContext();

						for (var cE : c) {
							var assembly = (AssemblyContextImpl) cE; // Aus Assembly Diagram

							var assemblyName = assembly.getEntityName();
							assemblyString += assemblyName + " ";
							overallString += assemblyName + " ";

							var encapsulatedComponent = assembly.getEncapsulatedComponent__AssemblyContext();
							var eCName = encapsulatedComponent.getEntityName(); // Component aus Repository Diagram
							encapsulatedString += eCName + " ";
							overallString += eCName + " ";

							int i = 1;
						}

						overallString += ") ";
					} else {
						throw new Error();
					}
				}
				System.out.println("OVERALL SEQUENCE");
				System.out.println(overallString);
				System.out.println("DIRECT SEQUENCE");
				System.out.println(directString);
				System.out.println("ASSEMBLY SEQUENCE");
				System.out.println(assemblyString);
				System.out.println("ENCAPSULATED SEQUENCE");
				System.out.println(encapsulatedString);
				System.out.println("\n\n");
			}

		}

		return null;
	}

}
