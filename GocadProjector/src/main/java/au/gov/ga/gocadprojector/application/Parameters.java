package au.gov.ga.gocadprojector.application;

import com.beust.jcommander.Parameter;

public class Parameters
{
	@Parameter(names = { "-i", "-input" }, description = "The input GOCAD object to reproject.", required = true)
	public String inputFile = null;

	@Parameter(names = { "-o", "-output" }, description = "The output repojected GOCAD object.", required = true)
	public String outputFile = null;

	@Parameter(names = { "-s", "-s_srs" }, description = "The source spatial reference set. The coordinate systems that can be passed are anything supported by the OGRSpatialReference.SetFromUserInput() call, which includes EPSG PCS and GCSes (ie. EPSG:4326), PROJ.4 declarations (as above), or the name of a .prf file containing well known text.", required = true)
	public String sourceSRS = null;

	@Parameter(names = { "-t", "-t_srs" }, description = "The target spatial reference set. The coordinate systems that can be passed are anything supported by the OGRSpatialReference.SetFromUserInput() call, which includes EPSG PCS and GCSes (ie. EPSG:4326), PROJ.4 declarations (as above), or the name of a .prf file containing well known text.", required = true)
	public String targetSRS = null;

	@Parameter(names = { "-f", "-overwrite" }, description = "Force overwriting the output file if it already exists.", required = false)
	public boolean overwrite = false;

	@Parameter(names = { "-h", "-help" }, description = "Print these command line usage instructions")
	public boolean showUsage = false;
}
