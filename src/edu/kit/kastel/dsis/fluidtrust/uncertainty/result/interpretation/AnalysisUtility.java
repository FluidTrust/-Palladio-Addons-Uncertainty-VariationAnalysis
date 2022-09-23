package edu.kit.kastel.dsis.fluidtrust.uncertainty.result.interpretation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.allocation.impl.AllocationContextImpl;
import org.palladiosimulator.pcm.allocation.impl.AllocationImpl;
import org.palladiosimulator.pcm.allocation.util.AllocationResourceImpl;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.impl.AssemblyContextImpl;
import org.palladiosimulator.pcm.core.entity.impl.EntityImpl;
import org.palladiosimulator.pcm.seff.impl.AbstractActionImpl;
import org.palladiosimulator.pcm.seff.impl.BranchActionImpl;
import org.palladiosimulator.pcm.usagemodel.impl.BranchImpl;
import org.palladiosimulator.pcm.usagemodel.impl.EntryLevelSystemCallImpl;

import de.uka.ipd.sdq.workflow.mdsd.blackboard.ModelLocation;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequence;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.CallingSEFFActionSequenceElementImpl;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.CallingUserActionSequenceElementImpl;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.SEFFActionSequenceElementImpl;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.dataflow.analysis.ViolatingConstraintActionSequence;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.variation.analysis.TestInitializer;

public class AnalysisUtility {
	/*
	 * IMPORTANT: LAST ELEMENT MUST BE REMOVED IF WE USE IT AS A PLACEHOLDER WITH
	 * VIOLATIONSEQUENCE
	 */
	public static HashSet<String> getIdsFromEntrySet(ActionSequence key) {
		final URI allocationURI = TestInitializer.getModelURI("models/source/default.allocation");

		// Load allocation model to be able to connect assemblies to allocations
		ResourceSet rs = new ResourceSetImpl();
		AllocationImpl allocationModel = (AllocationImpl) ((AllocationResourceImpl) rs.getResource(allocationURI, true))
				.getContents().get(0);

		// Jedes element in key ist ein Assembly.
		// Der Call läuft also über die Interfaces durch die Assemblies durch das
		// System.
		HashMap<String, String> assemblies = new HashMap<>();

		HashSet<String> ids = new HashSet<>();

		var allocationContexts = allocationModel.getAllocationContexts_Allocation();

		HashMap<String, AllocationContext> assemblyContextIdToAllocationContext = new HashMap<>();

		for (AllocationContext allocationContext : allocationContexts) {
			var assemblyContext = allocationContext.getAssemblyContext_AllocationContext();
			if (!assemblyContextIdToAllocationContext.containsKey(assemblyContext.getId())) {
				assemblyContextIdToAllocationContext.put(assemblyContext.getId(), allocationContext);
			}
		}

		for (var element : key) {
			// CHECK IF WE HAVE SEFF BRANCH OR USER SCENARIO BRANCH
			// NEEDED FOR UNCERTAINTY ANALYSIS (Branch in Sequence? Branch is Uncertainty?)
			if (element.getElement().eContainer().eContainer().eContainer() instanceof BranchActionImpl
					|| element.getElement().eContainer().eContainer().eContainer() instanceof BranchImpl) {

				var branch = (EntityImpl) element.getElement().eContainer().eContainer().eContainer();
				ids.add(branch.getId());
			}

			if (element instanceof CallingUserActionSequenceElementImpl) {
				// CALL IN USER SCENARIO
				EntryLevelSystemCallImpl e = (EntryLevelSystemCallImpl) element.getElement();

				ids.add(e.getId());
			} else if (element instanceof CallingSEFFActionSequenceElementImpl
					|| element instanceof SEFFActionSequenceElementImpl) {
				// EXTERNAL CALL IN SEFF DIAGRAM
				AbstractActionImpl e = (AbstractActionImpl) element.getElement();
				// ExternalCallActionImpl e = (ExternalCallActionImpl) element.getElement();

				ids.add(e.getId());

				// GET ASSEMBLY CONTEXT TO FIND OUT SPECIFIC ASSEMBLY
				Stack<AssemblyContext> c = element.getContext();

				for (var cE : c) {
					var assembly = (AssemblyContextImpl) cE; // From Assembly Diagram

					var encapsulatedComponent = assembly.getEncapsulatedComponent__AssemblyContext();

					ids.add(assembly.getId());
					ids.add(encapsulatedComponent.getId());
					if (assemblyContextIdToAllocationContext.containsKey(assembly.getId())) {
						var allocationContext = assemblyContextIdToAllocationContext.get(assembly.getId());
						ids.add(allocationContext.getId());
					}

					int i = 1;
				}
			} else {
				throw new Error();
			}
		}

		return ids;
	}
}
