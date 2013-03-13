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

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.gdal.ogr.ogrConstants;
import org.gdal.osr.SpatialReference;

import au.gov.ga.gocadprojector.application.Parameters;

/**
 * Dialog used for adding/editing the parameters for reprojecting a single GOCAD
 * object.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class EditDialog
{
	private final Shell shell;
	private final Display display;
	private final Button okButton;
	private int result = SWT.CANCEL;
	private boolean iValid = false, oValid = false, sValid = false, tValid = false;

	public EditDialog(Shell parent, String title, final Parameters parameters)
	{
		this.display = parent.getDisplay();

		final int buttonWidth = 60;
		final int buttonHeight = 30;
		final Color validColor = new Color(display, 0, 128, 0);
		final Color invalidColor = new Color(display, 255, 0, 0);

		GridData data;
		Composite composite;
		Button button;

		shell = new Shell(parent, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		shell.setText(title);
		shell.setLayout(new GridLayout(3, false));

		final Runnable validator = new Runnable()
		{
			@Override
			public void run()
			{
				boolean valid = iValid && oValid && sValid && tValid;
				if (okButton != null)
				{
					okButton.setEnabled(valid);
				}
			}
		};


		final Label inputLabel = new Label(shell, SWT.NONE);
		inputLabel.setText("Input file:");
		inputLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		final Text input = new Text(shell, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
		setTextText(input, parameters.inputFile);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.widthHint = 400;
		input.setLayoutData(data);

		ModifyListener inputListener = new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				iValid = input.getText().length() > 0 && new File(input.getText()).isFile();
				inputLabel.setForeground(iValid ? validColor : invalidColor);
				validator.run();
			}
		};
		input.addModifyListener(inputListener);
		inputListener.modifyText(null);

		button = new Button(shell, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				if (!input.getText().isEmpty())
				{
					dialog.setFileName(input.getText());
				}
				String selectedFile = dialog.open();
				if (selectedFile != null)
				{
					input.setText(selectedFile);
				}
			}
		});


		final Label sourceLabel = new Label(shell, SWT.NONE);
		sourceLabel.setText("Source SRS:");
		sourceLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		final Text source = new Text(shell, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
		setTextText(source, parameters.sourceSRS);
		source.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label sourceValidLabel = new Label(shell, SWT.NONE);
		sourceValidLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		ModifyListener sourceListener = new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				SpatialReference sr = new SpatialReference();
				boolean valid;
				try
				{
					valid = sr.SetFromUserInput(source.getText()) == ogrConstants.OGRERR_NONE;
				}
				catch (RuntimeException re)
				{
					valid = false;
				}
				sourceValidLabel.setText(valid ? "Valid" : "Invalid");
				sourceValidLabel.setForeground(valid ? validColor : invalidColor);
				sourceLabel.setForeground(valid ? validColor : invalidColor);
				sValid = valid;
				validator.run();
			}
		};
		source.addModifyListener(sourceListener);
		sourceListener.modifyText(null);


		final Label outputLabel = new Label(shell, SWT.NONE);
		outputLabel.setText("Output file:");
		outputLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		final Text output = new Text(shell, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
		setTextText(output, parameters.outputFile);
		output.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		ModifyListener outputListener = new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				oValid = output.getText().length() > 0;
				outputLabel.setForeground(oValid ? validColor : invalidColor);
				validator.run();
			}
		};
		output.addModifyListener(outputListener);
		outputListener.modifyText(null);

		button = new Button(shell, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				FileDialog dialog = new FileDialog(shell, SWT.SAVE);
				if (!output.getText().isEmpty())
				{
					dialog.setFileName(output.getText());
				}
				String selectedFile = dialog.open();
				if (selectedFile != null)
				{
					output.setText(selectedFile);
				}
			}
		});


		final Label targetLabel = new Label(shell, SWT.NONE);
		targetLabel.setText("Target SRS:");
		targetLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		final Text target = new Text(shell, SWT.LEFT | SWT.SINGLE | SWT.BORDER);
		setTextText(target, parameters.targetSRS);
		target.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label targetValidLabel = new Label(shell, SWT.NONE);
		targetValidLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		ModifyListener targetListener = new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				SpatialReference sr = new SpatialReference();
				boolean valid;
				try
				{
					valid = sr.SetFromUserInput(target.getText()) == ogrConstants.OGRERR_NONE;
				}
				catch (RuntimeException re)
				{
					valid = false;
				}
				targetValidLabel.setText(valid ? "Valid" : "Invalid");
				targetLabel.setForeground(valid ? validColor : invalidColor);
				targetValidLabel.setForeground(valid ? validColor : invalidColor);
				tValid = valid;
				validator.run();
			}
		};
		target.addModifyListener(targetListener);
		targetListener.modifyText(null);


		Label separator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		data = new GridData(SWT.FILL, SWT.BOTTOM, true, false, 3, 1);
		data.verticalIndent = 10;
		separator.setLayoutData(data);

		composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		data = new GridData(SWT.END, SWT.CENTER, true, false, 3, 1);
		composite.setLayoutData(data);

		okButton = new Button(composite, SWT.PUSH);
		okButton.setText("OK");
		shell.setDefaultButton(okButton);
		data = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		data.widthHint = buttonWidth;
		data.heightHint = buttonHeight;
		okButton.setLayoutData(data);
		okButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				result = SWT.OK;
				parameters.inputFile = input.getText();
				parameters.sourceSRS = source.getText();
				parameters.outputFile = output.getText();
				parameters.targetSRS = target.getText();
				shell.dispose();
			}
		});

		Button cancelButton = new Button(composite, SWT.PUSH);
		cancelButton.setText("Cancel");
		data = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		data.widthHint = buttonWidth;
		data.heightHint = buttonHeight;
		cancelButton.setLayoutData(data);
		cancelButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				shell.dispose();
			}
		});

		validator.run();


		shell.pack();
		int x = parent.getBounds().x + (parent.getBounds().width - shell.getBounds().width) / 2;
		int y = parent.getBounds().y + (parent.getBounds().height - shell.getBounds().height) / 2;
		shell.setLocation(x, y);
		shell.open();
	}

	private void setTextText(Text text, String s)
	{
		text.setText(s == null ? "" : s);
	}

	public int getResult()
	{
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
		return result;
	}
}
