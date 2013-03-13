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
package au.gov.ga.gocadprojector.gui;

import au.gov.ga.gocadprojector.application.Parameters;

/**
 * Reprojection job, contains the job status and the reprojection
 * {@link Parameters}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
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
