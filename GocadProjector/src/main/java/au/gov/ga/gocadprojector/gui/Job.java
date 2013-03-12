package au.gov.ga.gocadprojector.gui;

import au.gov.ga.gocadprojector.application.Parameters;

public class Job
{
	public enum Status
	{
		Waiting,
		Projecting,
		Complete,
		Error
	}
	
	public final Parameters parameters;
	public Status status = Status.Waiting;
	
	public Job(Parameters parameters)
	{
		this.parameters = parameters;
	}
}
