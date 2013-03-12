/*******************************************************************************
 * Copyright 2012 Geoscience Australia
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
package au.gov.ga.gocadprojector.util;

import java.io.File;

import org.gdal.gdal.gdal;

/**
 * Helper class containing some utility functions for handling GDAL datasets.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GDALUtil
{
	public static final String GDAL_DATUM_FILE = "gdal_datum.csv";

	protected final static String GDAL_DATA_PATH = "GDAL_DATA";
	protected final static String GDAL_DRIVER_PATH = "GDAL_DRIVER_PATH";

	/**
	 * Initialize the GDAL library. This should be called before any use of the
	 * library.
	 */
	public static void init()
	{
		File gdalDirectory = null;
		if (new File("gdal/data/" + GDAL_DATUM_FILE).exists())
		{
			gdalDirectory = new File("gdal");
		}

		if (gdalDirectory != null)
		{
			gdal.SetConfigOption(GDAL_DATA_PATH, new File(gdalDirectory, "data").getAbsolutePath());
			gdal.SetConfigOption(GDAL_DRIVER_PATH, new File(gdalDirectory, "plugins").getAbsolutePath());

			gdalDirectoryFound = true;
		}

		gdal.AllRegister();
	}

	private static boolean gdalDirectoryFound;

	/**
	 * @return Was the GDAL directory found by the init() function?
	 */
	public static boolean isGdalDirectoryFound()
	{
		return gdalDirectoryFound;
	}
}
