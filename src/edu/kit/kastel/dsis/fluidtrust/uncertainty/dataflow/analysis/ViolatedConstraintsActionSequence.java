package edu.kit.kastel.dsis.fluidtrust.uncertainty.dataflow.analysis;

import java.util.ArrayList;
import java.util.Stack;

import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.impl.LiteralImpl;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;

import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.ActionSequenceElement;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.CallingActionSequenceElement;
import edu.kit.kastel.dsis.fluidtrust.casestudy.pcs.analysis.dto.SEFFActionSequenceElementImpl;

/*
 * TODO: This class must be renamed
 */
public class ViolatedConstraintsActionSequence implements CallingActionSequenceElement<ExternalCallAction> {
	private ArrayList<LiteralImpl> literals;
	private ArrayList<ActionSequenceElement<?>> occuringElements;
	
	public ViolatedConstraintsActionSequence() {
		this.literals = new ArrayList<>();
		this.occuringElements = new ArrayList<>();
	}

	@Override
	public ExternalCallAction getElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stack<AssemblyContext> getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCallingPart() {
		// TODO Auto-generated method stub
		return false;
	}

	public ArrayList<LiteralImpl> getLiterals() {
		return literals;
	}

	public void addLiteral(LiteralImpl literal) {
		this.literals.add(literal);
	}
	
	public ArrayList<ActionSequenceElement<?>> getOccuringElements() {
		return this.occuringElements;
	}
	
	public void addOccuringElement(ActionSequenceElement<?> occuringElement) {
		this.occuringElements.add(occuringElement);
	}

}
