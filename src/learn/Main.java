package learn;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.eclipse.core.runtime.NullProgressMonitor;

import de.uka.ipd.sdq.workflow.Workflow;
import de.uka.ipd.sdq.workflow.blackboard.Blackboard;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.SequentialBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.SequentialJob;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;  

public class Main {
	public static void main(String[] args) throws JobFailedException, UserCanceledException {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%m%n")));
		
		LearnJob job1 = new LearnJob(42);
		LearnJob job2 = new LearnJob(33);
		LearnJob job3 = new LearnJob(16);
		
		SequentialBlackboardInteractingJob<Blackboard<Integer>> jobSequence = new SequentialBlackboardInteractingJob<Blackboard<Integer>>();

		
		jobSequence.add(job1);
		jobSequence.add(job2);
		jobSequence.add(job3);
		
		Workflow workflow = new Workflow(jobSequence);
		workflow.run();
	}
}
