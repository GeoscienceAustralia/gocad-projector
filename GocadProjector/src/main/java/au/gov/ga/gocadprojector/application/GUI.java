package au.gov.ga.gocadprojector.application;

import au.gov.ga.gocadprojector.gui.MainWindow;
import au.gov.ga.gocadprojector.util.GDALUtil;

public class GUI
{
	public static void main(String[] args)
	{
		GDALUtil.init();
		new MainWindow();
	}
}
