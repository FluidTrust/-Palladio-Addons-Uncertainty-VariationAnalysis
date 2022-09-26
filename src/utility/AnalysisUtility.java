package utility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.eclipse.emf.common.util.EList;
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
import org.palladiosimulator.pcm.repository.RepositoryComponent;
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
import edu.kit.kastel.dsis.fluidtrust.uncertainty.dataflow.analysis.ViolatedConstraintsActionSequence;
import edu.kit.kastel.dsis.fluidtrust.uncertainty.variation.analysis.TestInitializer;

public class AnalysisUtility {
	public final static String NO_VIOLATIONS_KEY = "resultNoViolationsKey";
	public final static String VIOLATIONS_KEY = "resultViolationsKey";
	
	public static HashSet<String> getIdsFromActionSequence(ActionSequence actionSequence) {
		HashMap<String, AllocationContext> assemblyContextIdToAllocationContext = getAssemblyToAllocationMapping(
				getAllocationModel().getAllocationContexts_Allocation());

		HashSet<String> ids = new HashSet<>();

		// iterate over every element and collect relevant ids
		for (var element : actionSequence) {
			if (!(element instanceof ViolatedConstraintsActionSequence)) { // ignore the violation information as it is actually not an action sequence

				// Check if the element of action sequence is contained in a branch. We need the
				// id of the branch for the analysis of influence of uncertainty points
				if (element.getElement().eContainer().eContainer().eContainer() instanceof BranchActionImpl
						|| element.getElement().eContainer().eContainer().eContainer() instanceof BranchImpl) {

					var branch = (EntityImpl) element.getElement().eContainer().eContainer().eContainer();
					ids.add(branch.getId());
				}

				if (element instanceof CallingUserActionSequenceElementImpl) {
					EntryLevelSystemCallImpl e = (EntryLevelSystemCallImpl) element.getElement();

					ids.add(e.getId());
				} else if (element instanceof CallingSEFFActionSequenceElementImpl
						|| element instanceof SEFFActionSequenceElementImpl) {
					AbstractActionImpl e = (AbstractActionImpl) element.getElement();

					ids.add(e.getId());

					// GET ASSEMBLY CONTEXT TO FIND OUT SPECIFIC ASSEMBLY
					Stack<AssemblyContext> contextStack = element.getContext();

					for (AssemblyContext assemblyContext : contextStack) {
						RepositoryComponent encapsulatedComponent = assemblyContext
								.getEncapsulatedComponent__AssemblyContext();

						ids.add(assemblyContext.getId());
						ids.add(encapsulatedComponent.getId());
						if (assemblyContextIdToAllocationContext.containsKey(assemblyContext.getId())) {
							ids.add(assemblyContextIdToAllocationContext.get(assemblyContext.getId()).getId());
						}

						int i = 1;
					}
				} else {
					throw new UnsupportedOperationException();
				}
			}
		}

		return ids;
	}

	private static Allocation getAllocationModel() {
		final URI allocationURI = TestInitializer.getModelURI("models/source/default.allocation");

		// Load allocation model to be able to connect assemblies to allocations
		ResourceSet rs = new ResourceSetImpl();
		return (Allocation) ((AllocationResourceImpl) rs.getResource(allocationURI, true)).getContents().get(0);
	}

	private static HashMap<String, AllocationContext> getAssemblyToAllocationMapping(
			EList<AllocationContext> allocationContexts) {
		HashMap<String, AllocationContext> assemblyContextIdToAllocationContext = new HashMap<>();

		for (AllocationContext allocationContext : allocationContexts) {
			var assemblyContext = allocationContext.getAssemblyContext_AllocationContext();
			if (!assemblyContextIdToAllocationContext.containsKey(assemblyContext.getId())) {
				assemblyContextIdToAllocationContext.put(assemblyContext.getId(), allocationContext);
			}
		}

		return assemblyContextIdToAllocationContext;
	}
}
