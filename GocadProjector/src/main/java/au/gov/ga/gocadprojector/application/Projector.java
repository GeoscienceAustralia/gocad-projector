package au.gov.ga.gocadprojector.application;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteOrder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gdal.ogr.ogrConstants;
import org.gdal.osr.CoordinateTransformation;
import org.gdal.osr.SpatialReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Projector
{
	private final static Logger logger = LoggerFactory.getLogger(Projector.class);
	private final static String NEW_LINE = "\r\n";

	private final static Pattern GOCAD_TYPE_REGEX = Pattern.compile("(?i)GOCAD\\s+(\\w+).*");
	private final static Pattern VERTEX_REGEX = Pattern
			.compile("(?i)P?VRTX\\s+\\d+\\s+([\\d.\\-e]+)\\s+([\\d.\\-e]+)\\s+([\\d.\\-e]+).*");
	private final static Pattern AXIS_REGEX = Pattern
			.compile("(?i)(?:AXIS_(\\w+)|(O)RIGIN)\\s+([\\d.\\-e]+)\\s+([\\d.\\-e]+)\\s+([\\d.\\-e]+).*");
	private final static Pattern NAME_REGEX = Pattern.compile("(?i)name:(.*)");
	private final static Pattern ASCII_DATA_FILE_REGEX = Pattern.compile("(?i)ASCII_DATA_FILE\\s+(.*)");
	private final static Pattern DATA_FILE_LINE_REGEX = Pattern
			.compile("\\s*([\\d.\\-e]+)\\s+([\\d.\\-e]+)\\s+([\\d.\\-e]+).*");
	private final static Pattern POINTS_FILE_REGEX = Pattern.compile("(?i)POINTS_FILE\\s+(.*)");
	private final static Pattern POINTS_OFFSET_REGEX = Pattern.compile("(?i)POINTS_OFFSET\\s+(\\d+).*");

	private final double[] transformed = new double[3];
	private double[] axisOoriginal;
	private double[] axisOprojected;
	private String name;
	private int pointsOffset = 0;

	private enum Mode
	{
		UNKNOWN,
		SIMPLE,
		VOXET,
		GSURF,
		SGRID
	}

	public void project(Parameters parameters) throws Exception
	{
		/*
		 * 1. VRTX and PVRTX are easy, simply load the x,y,z coordinates, and reproject
		 * 2. VOXETS: 
		 * 	- load AXIS_O, reproject
		 *  - load AXIS_U, add to original AXIS_O, reproject, from result subtract reprojected AXIS_O
		 *  - load AXIS_V, add to original AXIS_O, reproject, from result subtract reprojected AXIS_O
		 *  - load AXIS_W, add to original AXIS_O, reproject, from result subtract reprojected AXIS_O
		 * 3. GSURFS: same as VOXET, but only for AXIS_U and AXIS_V lines
		 * 4. SGRIDS: open ASCII_DATA_FILE, and reproject x,y,z coordinates (first 3 doubles)
		 */

		File input = new File(parameters.inputFile);
		File output = new File(parameters.outputFile);

		if (!input.exists())
		{
			throw new IOException("Could not find input file: " + input);
		}
		if (output.exists() && !parameters.overwrite)
		{
			throw new IOException("Output file already exists: " + output);
		}

		if (!output.getParentFile().exists())
		{
			output.getParentFile().mkdirs();
		}

		SpatialReference sSRS = new SpatialReference();
		int ret = sSRS.SetFromUserInput(parameters.sourceSRS);
		if (ret != ogrConstants.OGRERR_NONE)
		{
			throw new IllegalArgumentException("Unknown spatial reference: " + parameters.sourceSRS);
		}

		SpatialReference tSRS = new SpatialReference();
		ret = tSRS.SetFromUserInput(parameters.targetSRS);
		if (ret != ogrConstants.OGRERR_NONE)
		{
			throw new IllegalArgumentException("Unknown spatial reference: " + parameters.targetSRS);
		}

		CoordinateTransformation transformation = new CoordinateTransformation(sSRS, tSRS);
		Mode mode = Mode.UNKNOWN;

		BufferedReader reader = new BufferedReader(new FileReader(input));
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		try
		{
			int lineNumber = 1;
			String line = reader.readLine();
			while (line != null)
			{
				if (mode == Mode.UNKNOWN)
				{
					Matcher matcher = GOCAD_TYPE_REGEX.matcher(line);
					if (matcher.matches())
					{
						//clear axis values
						axisOoriginal = axisOprojected = null;
						name = null;
						pointsOffset = 0;

						String type = matcher.group(1);
						if (type == null || type.toLowerCase().contains("group"))
						{
							//ignore
						}
						else if ("voxet".equalsIgnoreCase(type))
						{
							mode = Mode.VOXET;
						}
						else if ("gsurf".equalsIgnoreCase(type))
						{
							mode = Mode.GSURF;
						}
						else if ("sgrid".equalsIgnoreCase(type))
						{
							mode = Mode.SGRID;
						}
						else
						{
							mode = Mode.SIMPLE;
						}
					}
					writer.write(line + NEW_LINE);
				}
				else
				{
					if (line.trim().equalsIgnoreCase("END"))
					{
						mode = Mode.UNKNOWN;
						writer.write(line + NEW_LINE);
					}
					else
					{
						switch (mode)
						{
						case SIMPLE:
							handleSimpleLine(line, lineNumber, writer, transformation);
							break;
						case VOXET:
							handleVoxetLine(line, lineNumber, writer, transformation);
							break;
						case GSURF:
							handleGSurfLine(line, lineNumber, writer, transformation);
							break;
						case SGRID:
							handleSGridLine(line, lineNumber, writer, transformation, input, output);
							break;
						default:
							writer.write(line + NEW_LINE);
							break;
						}
					}
				}

				line = reader.readLine();
				lineNumber++;
			}
		}
		finally
		{
			reader.close();
			writer.close();
		}
	}

	protected void handleSimpleLine(String line, int lineNumber, Writer writer, CoordinateTransformation transformation)
			throws IOException
	{
		Matcher matcher = VERTEX_REGEX.matcher(line);
		if (matcher.matches())
		{
			try
			{
				double x = Double.parseDouble(matcher.group(1));
				double y = Double.parseDouble(matcher.group(2));
				double z = Double.parseDouble(matcher.group(3));
				transformation.TransformPoint(transformed, x, y, z);
				x = transformed[0];
				y = transformed[1];
				z = transformed[2];
				line =
						line.substring(0, matcher.start(1)) + removeTrailingZero(x)
								+ line.substring(matcher.end(1), matcher.start(2)) + removeTrailingZero(y)
								+ line.substring(matcher.end(2), matcher.start(3)) + removeTrailingZero(z)
								+ line.substring(matcher.end(3), line.length());
			}
			catch (NumberFormatException e)
			{
				logger.error("Error parsing line " + lineNumber + ": ", e);
			}
		}
		
		writer.write(line + NEW_LINE);
	}

	protected void handleVoxetLine(String line, int lineNumber, Writer writer, CoordinateTransformation transformation)
			throws IOException
	{
		handleVoxetOrGSurfLine(line, lineNumber, writer, transformation, false);
	}

	protected void handleGSurfLine(String line, int lineNumber, Writer writer, CoordinateTransformation transformation)
			throws IOException
	{
		handleVoxetOrGSurfLine(line, lineNumber, writer, transformation, true);
	}

	protected void handleVoxetOrGSurfLine(String line, int lineNumber, Writer writer,
			CoordinateTransformation transformation, boolean ignoreW) throws IOException
	{
		Matcher matcher = AXIS_REGEX.matcher(line);
		if (matcher.matches())
		{
			try
			{
				String type = matcher.group(1);
				if (type == null)
				{
					type = matcher.group(2);
				}
				double[] axis =
						new double[] { Double.parseDouble(matcher.group(3)), Double.parseDouble(matcher.group(4)),
								Double.parseDouble(matcher.group(5)) };
				double[] output = axis;

				if ("O".equalsIgnoreCase(type))
				{
					axisOoriginal = axis;
					axisOprojected = new double[3];
					transformation.TransformPoint(axisOprojected, axis[0], axis[1], axis[2]);
					output = axisOprojected;
				}
				else if ("U".equalsIgnoreCase(type) || "V".equalsIgnoreCase(type)
						|| ("W".equalsIgnoreCase(type) && !ignoreW))
				{
					transformation.TransformPoint(transformed, axisOoriginal[0] + axis[0], axisOoriginal[1] + axis[1],
							axisOoriginal[2] + axis[2]);
					transformed[0] -= axisOprojected[0];
					transformed[1] -= axisOprojected[1];
					transformed[2] -= axisOprojected[2];
					output = transformed;
				}
				else if ("MIN".equalsIgnoreCase(type))
				{
					if (axis[0] != 0 || axis[1] != 0 || axis[2] != 0)
					{
						logger.error("Line " + lineNumber + ": 'AXIS_MIN 0 0 0' expected, '" + line + "' actual");
					}
				}
				else if ("MAX".equalsIgnoreCase(type))
				{
					if (axis[0] != 1 || axis[1] != 1 || axis[2] != 1)
					{
						logger.error("Line " + lineNumber + ": 'AXIS_MAX 1 1 1' expected, '" + line + "' actual");
					}
				}

				line =
						line.substring(0, matcher.start(3)) + removeTrailingZero(output[0])
								+ line.substring(matcher.end(3), matcher.start(4)) + removeTrailingZero(output[1])
								+ line.substring(matcher.end(4), matcher.start(5)) + removeTrailingZero(output[2])
								+ line.substring(matcher.end(5), line.length());
			}
			catch (NumberFormatException e)
			{
				logger.error("Error parsing line " + lineNumber + ": ", e);
			}
		}
		
		writer.write(line + NEW_LINE);
	}

	protected String removeTrailingZero(double d)
	{
		return (long) d == d ? "" + (long) d : "" + d;
	}

	protected void handleSGridLine(String line, int lineNumber, Writer writer, CoordinateTransformation transformation,
			File inputFile, File outputFile) throws IOException
	{
		Matcher matcher = NAME_REGEX.matcher(line);
		if (matcher.matches())
		{
			name = matcher.group(1);
		}

		matcher = ASCII_DATA_FILE_REGEX.matcher(line);
		if (matcher.matches())
		{
			String asciiDataFileOriginal = matcher.group(1).trim();
			String prefix = name;
			if (prefix == null)
			{
				prefix = outputFile.getName().replace(" ", "_");
			}
			String asciiDataFileProjected = renameUntilFileNotExists(outputFile.getParentFile(), prefix, "__ascii@@");
			line =
					line.substring(0, matcher.start(1)) + asciiDataFileProjected
							+ line.substring(matcher.end(1), line.length());

			File input = new File(inputFile.getParentFile(), asciiDataFileOriginal);
			File output = new File(outputFile.getParentFile(), asciiDataFileProjected);
			handleAsciiDataFile(input, output, transformation);
		}

		matcher = POINTS_OFFSET_REGEX.matcher(line);
		if (matcher.matches())
		{
			pointsOffset = Integer.parseInt(matcher.group(1));
		}

		matcher = POINTS_FILE_REGEX.matcher(line);
		if (matcher.matches())
		{
			String pointsFileOriginal = matcher.group(1).trim();
			String prefix = name;
			if (prefix == null)
			{
				prefix = outputFile.getName().replace(" ", "_");
			}
			String pointsFileProjected = renameUntilFileNotExists(outputFile.getParentFile(), prefix, "__points@@");
			line =
					line.substring(0, matcher.start(1)) + pointsFileProjected
							+ line.substring(matcher.end(1), line.length());

			File input = new File(inputFile.getParentFile(), pointsFileOriginal);
			File output = new File(outputFile.getParentFile(), pointsFileProjected);
			handlePointsFile(input, pointsOffset, output, transformation);
		}

		writer.write(line + NEW_LINE);
	}

	protected String renameUntilFileNotExists(File parent, String prefix, String suffix)
	{
		if (!(new File(parent, prefix + suffix).exists()))
		{
			return prefix + suffix;
		}
		int i = 2;
		while (true)
		{
			String f = prefix + "_" + (i++) + suffix;
			if (!(new File(parent, f).exists()))
			{
				return f;
			}
		}
	}

	protected void handleAsciiDataFile(File inputFile, File outputFile, CoordinateTransformation transformation)
			throws IOException
	{
		logger.info("Reprojecting SGrid ASCII_DATA_FILE: " + inputFile);

		if (outputFile.exists())
		{
			throw new IOException("Could not reproject SGrid ASCII_DATA_FILE, output file already exists: "
					+ outputFile);
		}

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

		try
		{
			String line = reader.readLine();
			while (line != null)
			{
				Matcher matcher = DATA_FILE_LINE_REGEX.matcher(line);
				if (matcher.matches())
				{
					double x = Double.parseDouble(matcher.group(1));
					double y = Double.parseDouble(matcher.group(2));
					double z = Double.parseDouble(matcher.group(3));
					transformation.TransformPoint(transformed, x, y, z);
					x = transformed[0];
					y = transformed[1];
					z = transformed[2];
					line =
							line.substring(0, matcher.start(1)) + removeTrailingZero(x)
									+ line.substring(matcher.end(1), matcher.start(2)) + removeTrailingZero(y)
									+ line.substring(matcher.end(2), matcher.start(3)) + removeTrailingZero(z)
									+ line.substring(matcher.end(3), line.length());
				}
				writer.write(line + NEW_LINE);
				line = reader.readLine();
			}
		}
		finally
		{
			reader.close();
			writer.close();
		}
	}

	protected void handlePointsFile(File inputFile, int inputOffset, File outputFile,
			CoordinateTransformation transformation) throws IOException
	{
		logger.info("Reprojecting SGrid POINTS_FILE: " + inputFile);

		if (outputFile.exists())
		{
			throw new IOException("Could not reproject SGrid POINTS_FILE, output file already exists: " + outputFile);
		}

		InputStream is = new BufferedInputStream(new FileInputStream(inputFile));
		OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));

		try
		{
			for (int i = 0; i < inputOffset; i++)
			{
				os.write(is.read());
			}

			while (true)
			{
				Float x = readNextFloat(is, ByteOrder.LITTLE_ENDIAN);
				Float y = readNextFloat(is, ByteOrder.LITTLE_ENDIAN);
				Float z = readNextFloat(is, ByteOrder.LITTLE_ENDIAN);
				if (z == null)
				{
					break;
				}
				transformation.TransformPoint(transformed, x, y, z);
				x = (float) transformed[0];
				y = (float) transformed[1];
				z = (float) transformed[2];
				writeFloat(x, os, ByteOrder.LITTLE_ENDIAN);
				writeFloat(y, os, ByteOrder.LITTLE_ENDIAN);
				writeFloat(z, os, ByteOrder.LITTLE_ENDIAN);
			}
		}
		finally
		{
			is.close();
			os.close();
		}
	}

	public static Float readNextFloat(InputStream is, ByteOrder byteOrder) throws IOException
	{
		int b0, b1, b2, b3;
		if (byteOrder == ByteOrder.LITTLE_ENDIAN)
		{
			b3 = is.read();
			b2 = is.read();
			b1 = is.read();
			b0 = is.read();

			if (b0 < 0)
			{
				return null;
			}
		}
		else
		{
			b0 = is.read();
			b1 = is.read();
			b2 = is.read();
			b3 = is.read();

			if (b3 < 0)
			{
				return null;
			}
		}
		return Float.intBitsToFloat((b0) | (b1 << 8) | (b2 << 16) | b3 << 24);
	}

	public static void writeFloat(float f, OutputStream os, ByteOrder byteOrder) throws IOException
	{
		int i = Float.floatToIntBits(f);
		int b0 = i & 0xff;
		int b1 = (i >> 8) & 0xff;
		int b2 = (i >> 16) & 0xff;
		int b3 = (i >> 24) & 0xff;

		if (byteOrder == ByteOrder.LITTLE_ENDIAN)
		{
			os.write(b3);
			os.write(b2);
			os.write(b1);
			os.write(b0);
		}
		else
		{
			os.write(b0);
			os.write(b1);
			os.write(b2);
			os.write(b3);
		}
	}
}
