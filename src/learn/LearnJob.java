package learn;

import org.eclipse.core.runtime.IProgressMonitor;

import de.uka.ipd.sdq.workflow.blackboard.Blackboard;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.IJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.SequentialBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public class LearnJob extends SequentialBlackboardInteractingJob<Blackboard<Integer>> {	
	private int number = 0;
	
	public LearnJob(int number) {
		this.number = number;
	}

	public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
		System.out.println(Integer.toString(this.number));
		Blackboard<Integer> blackboard = getBlackboard();
		Object partition = blackboard.getPartition("sum");
		int newSum = 0;
		if (partition != null) {
			newSum = (int) partition + this.number;
		} else {
			newSum = this.number;
		}
		blackboard.addPartition("sum", newSum);
		System.out.println("Current sum: " + newSum);
	}

	public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
		// TODO Auto-generated method stub
		
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Learn Job";
	}
	
	public void setBlackboard(Blackboard<Integer> blackboard) {
		this.myBlackboard = blackboard;
	}

	
}
