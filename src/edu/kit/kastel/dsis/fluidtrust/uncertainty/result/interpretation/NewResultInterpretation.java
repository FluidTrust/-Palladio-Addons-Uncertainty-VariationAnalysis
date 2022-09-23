package edu.kit.kastel.dsis.fluidtrust.uncertainty.result.interpretation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.impl.AssemblyContextImpl;
import org.palladiosimulator.pcm.core.entity.impl.EntityImpl;
import org.palladiosimulator.pcm.seff.impl.AbstractActionImpl;
import org.palladiosimulator.pcm.seff.impl.BranchActionImpl;
import org.palladiosimulator.pcm.seff.impl.ExternalCallActionImpl;
import org.palladiosimulator.pcm.seff.impl.SetVariableActionImpl;
import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.impl.BranchImpl;
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

				ViolatingConstraintActionSequence violatingConstraint = (ViolatingConstraintActionSequence) key
						.get(key.size() - 1);
				SEFFActionSequenceElementImpl occuringElement = (SEFFActionSequenceElementImpl) violatingConstraint
						.getOccuringElement();
				key.remove(key.size() - 1);

				// TODO: How to properly do this?

				Set<String> ids = new HashSet<String>();

				for (var element : key) {
					overallString += "(";

					var branchName = "";
					// CHECK IF WE HAVE SEFF BRANCH OR USER SCENARIO BRANCH
					// NEEDED FOR UNCERTAINTY ANALYSIS (Branch in Sequence? Branch is Uncertainty?)
					if (element.getElement().eContainer().eContainer().eContainer() instanceof BranchActionImpl
							|| element.getElement().eContainer().eContainer().eContainer() instanceof BranchImpl) {

						var branch = (EntityImpl) element.getElement().eContainer().eContainer().eContainer();
						branchName = branch.getEntityName();
						ids.add(branch.getId());
					}

					if (branchName != "") {
						overallString += branchName + " ";
					}

					if (element instanceof CallingUserActionSequenceElementImpl) {
						// CALL IN USER SCENARIO
						EntryLevelSystemCallImpl e = (EntryLevelSystemCallImpl) element.getElement();

						var name = e.getEntityName();
						directString += name + " ";
						overallString += name + " ";

						ids.add(e.getId());
					} else if (element instanceof CallingSEFFActionSequenceElementImpl || element instanceof SEFFActionSequenceElementImpl) {
						// EXTERNAL CALL IN SEFF DIAGRAM
						AbstractActionImpl e = (AbstractActionImpl) element.getElement();
						//ExternalCallActionImpl e = (ExternalCallActionImpl) element.getElement();

						var name = e.getEntityName();
						ids.add(e.getId());
						directString += name + " ";
						overallString += name + " ";

						// GET ASSEMBLY CONTEXT TO FIND OUT SPECIFIC ASSEMBLY
						Stack<AssemblyContext> c = element.getContext();

						for (var cE : c) {
							var assembly = (AssemblyContextImpl) cE; // From Assembly Diagram

							var assemblyName = assembly.getEntityName();
							assemblyString += assemblyName + " ";
							overallString += assemblyName + " ";

							var encapsulatedComponent = assembly.getEncapsulatedComponent__AssemblyContext();
							var eCName = encapsulatedComponent.getEntityName(); // Component in Repository Diagram
							encapsulatedString += eCName + " ";
							overallString += eCName + " ";
							
							ids.add(assembly.getId());
							ids.add(encapsulatedComponent.getId());

							int i = 1;
						}
					} else {
						throw new Error();
					}

					overallString += ") ";
				}
				/*System.out.println("OVERALL SEQUENCE");
				System.out.println(overallString);
				System.out.println("DIRECT SEQUENCE");
				System.out.println(directString);
				System.out.println("ASSEMBLY SEQUENCE");
				System.out.println(assemblyString);
				System.out.println("ENCAPSULATED SEQUENCE");
				System.out.println(encapsulatedString);
				System.out.println("VIOLATED CONSTRAINT");
				violatingConstraint.getLiterals().forEach(l -> System.out.println(l.getName()));
				System.out.println("LOCATION OF VIOLATION");
				System.out.println(occuringElement.toString());
				System.out.println("\n\n");*/
				System.out.println(ids.toString());
			}

		}

		return null;
	}

}
