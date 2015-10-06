package Generators;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.evilOverlord.EvilOverlord;

// source http://introcs.cs.princeton.edu/java/32class/Hilbert.java.html
public class LoadGCodeGenerator implements GcodeGenerator {
	
	EvilOverlord gui;
	
	
	public LoadGCodeGenerator(EvilOverlord _gui) {
		super();
		gui = _gui;
	}
	
	
	public String GetMenuName() {
		return "Load GCode";
	}
	
	
	public void Generate() {
	    // Note: source for ExampleFileFilter can be found in FileChooserDemo,
	    // under the demo/jfc directory in the Java 2 SDK, Standard Edition.
		//String filename = (recentFiles[0].length()>0) ? filename=recentFiles[0] : "";
		String filename="";

		FileFilter filterGCODE = new FileNameExtensionFilter(("GCode"), "ngc");
		//FileFilter filterImage = new FileNameExtensionFilter(("Image"), "jpg", "jpeg", "png", "wbmp", "bmp", "gif");
		//FileFilter filterDXF   = new FileNameExtensionFilter(("DXF"), "dxf");
		 
		JFileChooser fc = new JFileChooser(new File(filename));
		//fc.addChoosableFileFilter(filterImage);
		//fc.addChoosableFileFilter(filterDXF);
		fc.addChoosableFileFilter(filterGCODE);
	    if(fc.showOpenDialog(gui.GetMainFrame()) == JFileChooser.APPROVE_OPTION) {
	    	String selectedFile=fc.getSelectedFile().getAbsolutePath();

			// open the file automatically to save a click.
			gui.OpenFile(selectedFile);
	    }
	}
}
