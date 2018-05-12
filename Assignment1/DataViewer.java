import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

public class DataViewer {

	protected Shell shlTopologyData;
	private Text textBox;
	private String topText;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		String text = "";
		try {
			DataViewer window = new DataViewer();
			window.open(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open(String topText) {
		Display display = Display.getDefault();
		this.topText = topText;
		createContents();
		shlTopologyData.open();
		shlTopologyData.layout();
		while (!shlTopologyData.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlTopologyData = new Shell();
		shlTopologyData.setSize(1125, 795);
		shlTopologyData.setText("Data Viewer");
		
		textBox = new Text(shlTopologyData, SWT.READ_ONLY | SWT.V_SCROLL);
		textBox.setText(topText);
		textBox.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		textBox.setBounds(10, 10, 1089, 737);

	}
}
