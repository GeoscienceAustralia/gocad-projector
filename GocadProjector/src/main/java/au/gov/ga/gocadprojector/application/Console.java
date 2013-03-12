package au.gov.ga.gocadprojector.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.gocadprojector.util.GDALUtil;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Console
{
	private final static Logger logger = LoggerFactory.getLogger(Console.class);

	public static void main(String[] args)
	{
		GDALUtil.init();

		Parameters parameters = handleCommandLineArguments(args);
		if (parameters == null)
		{
			return;
		}
		Projector projector = new Projector();
		try
		{
			projector.project(parameters);
		}
		catch (Exception e)
		{
			logger.error("Error", e);
		}
	}

	private static Parameters handleCommandLineArguments(String[] args)
	{
		Parameters parameters = new Parameters();
		JCommander jCommander = null;
		try
		{
			jCommander = new JCommander();
			jCommander.setProgramName("gocadprojector");
			jCommander.addObject(parameters);
			jCommander.parse(args);
		}
		catch (ParameterException e)
		{
			if (!parameters.showUsage)
			{
				logger.error(e.getLocalizedMessage());
			}
			jCommander.usage();
			return null;
		}
		if (parameters.showUsage)
		{
			StringBuilder builder = new StringBuilder();
			jCommander.usage(builder);
			logger.info(builder.toString());
			return null;
		}
		return parameters;
	}
}