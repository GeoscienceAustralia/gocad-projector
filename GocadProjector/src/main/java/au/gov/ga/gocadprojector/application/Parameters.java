/*******************************************************************************
 * Copyright 2013 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.gocadprojector.application;

import com.beust.jcommander.Parameter;

/**
 * Contains the parameters required for reprojecting a GOCAD object.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
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
