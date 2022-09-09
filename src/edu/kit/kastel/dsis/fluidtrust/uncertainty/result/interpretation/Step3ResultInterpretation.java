package edu.kit.kastel.dsis.fluidtrust.uncertainty.result.interpretation;

import java.util.ArrayList;

import org.eclipse.emf.common.util.URI;
import org.palladiosimulator.pcm.allocation.impl.AllocationContextImpl;
import org.palladiosimulator.pcm.core.composition.impl.AssemblyContextImpl;
import org.palladiosimulator.pcm.seff.impl.BranchActionImpl;
import org.palladiosimulator.pcm.uncertainty.variation.UncertaintyVariationModel.gen.pcm.VariationManager;
import org.palladiosimulator.pcm.uncertainty.variation.UncertaintyVariationModel.gen.pcm.adapter.resource.ModelResourceAbstraction;
import org.palladiosimulator.pcm.uncertainty.variation.UncertaintyVariationModel.gen.pcm.adapter.resource.ResourceAbstraction;
import org.palladiosimulator.pcm.usagemodel.impl.BranchImpl;

import UncertaintyVariationModel.VariationPoint;
import UncertaintyVariationModel.impl.UncertaintyVariationsImpl;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionBasedQueryResult;

public class Step3ResultInterpretation implements ResultInterpretation {

	@Override
	public Object getInterpretation(ArrayList<ActionBasedQueryResult> violations) {
		final URI umURI = URI.createPlatformPluginURI(
	               "/edu.kit.kastel.dsis.fluidtrust.uncertainty.variation.analysis/models/My.uncertaintyvariationmodel", false);
		
		ResourceAbstraction rs = new ModelResourceAbstraction();
		VariationManager vm = new VariationManager(umURI, rs);
		
		UncertaintyVariationsImpl test = (UncertaintyVariationsImpl) vm.loadUncertaintyVariantModel();
		var test2 = test.getVariationPoints();
		
		String variationPoints = "";
		for (VariationPoint entry : test2) {
			var varyingSubjects = entry.getVaryingSubjects();
			for (var entry2: varyingSubjects) {
				var id = entry2.getId();
				if (entry2 instanceof AssemblyContextImpl) {
					var assemblyName = ((AssemblyContextImpl) entry2).getEntityName();
					variationPoints += assemblyName + " ";
					var x = 1;
				} else if (entry2 instanceof BranchActionImpl) { 
					var branchName = ((BranchActionImpl) entry2).getEntityName();
					variationPoints += branchName + " ";
					var x = 1;
				} else if (entry2 instanceof AllocationContextImpl) {
					var allocationName = ((AllocationContextImpl) entry2).getEntityName();
					variationPoints += allocationName + " ";
					var x = 1;
				} else if (entry2 instanceof BranchImpl) {
					var branchName = ((BranchImpl) entry2).getEntityName();
					variationPoints += branchName + " ";
					var x = 1;
				} else {
					throw new Error();
				}
			}
			
		}
		System.out.println(variationPoints);
		
		return null;
	}
	
}
