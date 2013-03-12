package au.gov.ga.gocadprojector.gui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import au.gov.ga.gocadprojector.application.Parameters;
import au.gov.ga.gocadprojector.application.Projector;
import au.gov.ga.gocadprojector.gui.Job.Status;

public class MainWindow
{
	private Parameters lastParameters;
	private boolean cancelled = false;

	public MainWindow()
	{
		int addButtonWidth = 100;
		int startButtonWidth = 60;
		int buttonHeight = 30;

		GridData data;
		Composite composite;

		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText("Geoscience Australia GOCAD Projector");
		shell.setLayout(new GridLayout());

		Group group = new Group(shell, SWT.SHADOW_ETCHED_IN);
		group.setText("Files to reproject");
		group.setLayout(new GridLayout());
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		group.setLayoutData(data);

		final Table table = new Table(group, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = 800;
		data.heightHint = 400;
		table.setLayoutData(data);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		String[] titles = new String[] { "Input file", "Source SRS", "Output", "Target SRS", "Status" };
		int[] widths = new int[] { 275, 100, 275, 100, 60 };
		for (int i = 0; i < titles.length; i++)
		{
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titles[i]);
			column.setWidth(widths[i]);
		}

		composite = new Composite(group, SWT.NONE);
		composite.setLayout(new GridLayout(4, false));
		data = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		composite.setLayoutData(data);

		final Button addButton = new Button(composite, SWT.PUSH);
		addButton.setText("Add file...");
		data = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		data.widthHint = addButtonWidth;
		data.heightHint = buttonHeight;
		addButton.setLayoutData(data);
		addButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Parameters parameters = new Parameters();
				if (lastParameters != null)
				{
					parameters.sourceSRS = lastParameters.sourceSRS;
					parameters.targetSRS = lastParameters.targetSRS;
				}
				EditDialog dialog = new EditDialog(shell, ((Button) e.widget).getText(), parameters);
				if (dialog.getResult() == SWT.OK)
				{
					addJob(table, parameters);
				}
			}
		});

		final Button directoryButton = new Button(composite, SWT.PUSH);
		directoryButton.setText("Add directory...");
		data = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		data.widthHint = addButtonWidth;
		data.heightHint = buttonHeight;
		directoryButton.setLayoutData(data);
		directoryButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				List<Parameters> parameters = new ArrayList<Parameters>();
				String sourceSRS = lastParameters != null ? lastParameters.sourceSRS : null;
				String targetSRS = lastParameters != null ? lastParameters.targetSRS : null;
				AddDirectoryDialog dialog =
						new AddDirectoryDialog(shell, ((Button) e.widget).getText(), parameters, sourceSRS, targetSRS);
				if (dialog.getResult() == SWT.OK)
				{
					for (Parameters p : parameters)
					{
						addJob(table, p);
					}
				}
			}
		});

		final Button editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit selected...");
		data = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		data.widthHint = addButtonWidth;
		data.heightHint = buttonHeight;
		editButton.setLayoutData(data);
		editButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				TableItem[] items = table.getSelection();
				for (TableItem item : items)
				{
					editItem(item, shell, ((Button) e.widget).getText());
				}
			}
		});

		final Button deleteButton = new Button(composite, SWT.PUSH);
		deleteButton.setText("Delete selected");
		data = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		data.widthHint = addButtonWidth;
		data.heightHint = buttonHeight;
		deleteButton.setLayoutData(data);
		deleteButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				deleteSelectedItems(table);
			}
		});

		Label separator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		data = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
		data.verticalIndent = 10;
		separator.setLayoutData(data);

		composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		data = new GridData(SWT.END, SWT.CENTER, true, false);
		composite.setLayoutData(data);

		final Button startButton = new Button(composite, SWT.PUSH);
		startButton.setText("Start");
		data = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		data.widthHint = startButtonWidth;
		data.heightHint = buttonHeight;
		startButton.setLayoutData(data);

		final Button cancelButton = new Button(composite, SWT.PUSH);
		cancelButton.setText("Cancel");
		data = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		data.widthHint = startButtonWidth;
		data.heightHint = buttonHeight;
		cancelButton.setLayoutData(data);
		cancelButton.setEnabled(false);

		SelectionListener tableSelectionListener = new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				boolean empty = table.getSelectionCount() == 0;
				editButton.setEnabled(!empty);
				deleteButton.setEnabled(!empty);
			}
		};
		table.addSelectionListener(tableSelectionListener);
		tableSelectionListener.widgetSelected(null);

		table.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseDoubleClick(MouseEvent e)
			{
				TableItem item = table.getItem(new Point(e.x, e.y));
				if (item != null)
				{
					editItem(item, shell, editButton.getText());
				}
			}
		});
		
		table.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent e)
			{
				if(e.keyCode == SWT.DEL)
				{
					deleteSelectedItems(table);
				}
			}
		});

		startButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Control[] controls =
						new Control[] { addButton, directoryButton, editButton, deleteButton, startButton };

				cancelButton.setEnabled(true);
				for (Control control : controls)
				{
					control.setEnabled(false);
				}

				for (final TableItem item : table.getItems())
				{
					if (cancelled)
						break;

					final Job job = (Job) item.getData();
					if (job.status != Status.Waiting)
						continue;

					job.status = Status.Projecting;
					updateItem(item);

					final boolean[] complete = new boolean[] { false };
					Thread thread = new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							Projector projector = new Projector();
							try
							{
								projector.project(job.parameters);
								job.status = Status.Complete;
							}
							catch (Exception e)
							{
								e.printStackTrace();
								job.status = Status.Error;
							}

							complete[0] = true;
							display.wake();
						}
					});
					thread.setDaemon(true);
					thread.start();

					while (!complete[0])
					{
						if (!display.readAndDispatch())
						{
							display.sleep();
						}
					}

					updateItem(item);
				}

				cancelButton.setEnabled(false);
				for (Control control : controls)
				{
					control.setEnabled(true);
				}
				cancelled = false;
			}
		});

		cancelButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				cancelled = true;
				cancelButton.setEnabled(false);
			}
		});


		shell.pack();
		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
		display.dispose();
	}

	protected void addJob(Table table, Parameters parameters)
	{
		lastParameters = parameters;
		Job job = new Job(parameters);
		TableItem item = new TableItem(table, SWT.NONE);
		item.setData(job);
		updateItem(item);
	}

	protected void updateItem(TableItem item)
	{
		Job job = (Job) item.getData();
		Parameters parameters = job.parameters;
		item.setText(0, parameters.inputFile);
		item.setText(1, parameters.sourceSRS);
		item.setText(2, parameters.outputFile);
		item.setText(3, parameters.targetSRS);
		item.setText(4, job.status.toString());
	}

	protected void editItem(TableItem item, Shell shell, String title)
	{
		Job job = (Job) item.getData();
		EditDialog dialog = new EditDialog(shell, title, job.parameters);
		if (dialog.getResult() == SWT.OK)
		{
			updateItem(item);
		}
	}
	
	protected void deleteSelectedItems(Table table)
	{
		int[] items = table.getSelectionIndices();
		table.remove(items);
	}
}
