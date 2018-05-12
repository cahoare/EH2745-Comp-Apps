import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.custom.StyledText;

public class Help {

	protected Shell shlUsingMatpower;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Help window = new Help();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlUsingMatpower.open();
		shlUsingMatpower.layout();
		while (!shlUsingMatpower.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlUsingMatpower = new Shell();
		shlUsingMatpower.setSize(639, 294);
		shlUsingMatpower.setText("Using MatPower");
		
		StyledText styledText = new StyledText(shlUsingMatpower, SWT.READ_ONLY);
		styledText.setText("To run the power flow uses the Matlab Engine API. It requires:\r\n \t- Matlab 2016 or later\r\n\t- matpower to be installed\r\nThis may require the OS application path to be updated. Guidance is given below for Windows, for other OS\r\nplease consult Matlab Engine JAVA API documentation:\r\n\t- Navigate to \"System Control Panel\"\r\n\t- Enter \"Advanced system settings\"\r\n\t- Enter \"Environment variables\"\r\n\t- Edit \"Path\" so that the first path is \"<matlabroot>\\bin\\win64\", where <matlabroot> is the Matlab file path\r\nIf running application as a .java file, ensure that the appropriate Matlab Engine JAR is added to the project:\r\n\t- <matlabroot>\\extern\\engines\\java\\jar\\engine.jar is in the project build path\r\n\r\nThe application will produce an .m file which must be located in the same directory as the Matpower files. \r\nThis file is named \"casefile\" and will overwrite any existing files named \"casefile\" in the directory. ");
		styledText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		styledText.setMarginColor(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		styledText.setSelectionForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		styledText.setSelectionBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		styledText.setBounds(10, 10, 603, 235);

	}
}
