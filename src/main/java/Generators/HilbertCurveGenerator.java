package Generators;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.marginallyclever.robotOverlord.RobotOverlord;

// source http://introcs.cs.princeton.edu/java/32class/Hilbert.java.html
public class HilbertCurveGenerator implements GcodeGenerator {
	float turtle_x,turtle_y;
	float turtle_dx,turtle_dy;
	float turtle_step=10.0f;
	float xmax = 7;
	float xmin = -7;
	float ymax = 7;
	float ymin = -7;
	float tool_offset_z = 1.25f;
	float z_down=40;
	float z_up=90;
	int order=4; // controls complexity of curve
	
	RobotOverlord gui;
	
	
	public HilbertCurveGenerator(RobotOverlord _gui) {
		super();
		gui = _gui;
	}
	
	
	public String GetMenuName() {
		return "Hilbert Curve";
	}
	
	
	public void Generate() {
		final JDialog driver = new JDialog(gui.getMainFrame(),"Your Message Here",true);
		driver.setLayout(new GridLayout(0,1));

		final JTextField field_size = new JTextField(Integer.toString((int)xmax));
		final JTextField field_order = new JTextField(Integer.toString(order));
		final JTextField field_up = new JTextField(Integer.toString((int)z_up));
		final JTextField field_down = new JTextField(Integer.toString((int)z_down));

		driver.add(new JLabel("Size"));		driver.add(field_size);
		driver.add(new JLabel("Order"));	driver.add(field_order);
		driver.add(new JLabel("Up"));		driver.add(field_up);
		driver.add(new JLabel("Down"));		driver.add(field_down);

		final JButton buttonSave = new JButton("Go");
		final JButton buttonCancel = new JButton("Cancel");
		Box horizontalBox = Box.createHorizontalBox();
	    horizontalBox.add(Box.createGlue());
	    horizontalBox.add(buttonSave);
	    horizontalBox.add(buttonCancel);
	    driver.add(horizontalBox);
		
		ActionListener driveButtons = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object subject = e.getSource();
				
				if(subject == buttonSave) {
					z_up = Float.parseFloat(field_up.getText());
					z_down = Float.parseFloat(field_down.getText());
					xmax = Integer.parseInt(field_size.getText());
					ymax= xmax;
					xmin=-xmax;
					ymin=-xmax;
					order = Integer.parseInt(field_order.getText());
					CreateCurveNow();
					
					driver.dispose();
				}
				if(subject == buttonCancel) {
					driver.dispose();
				}
			}
		};
		
		buttonSave.addActionListener(driveButtons);
		buttonCancel.addActionListener(driveButtons);

		driver.setSize(300,400);
		driver.setVisible(true);
	}
	

	private void CreateCurveNow() {
		try {
			String outputFile = System.getProperty("user.dir") + "/" + "TEMP.NGC";
			System.out.println("output file = "+outputFile);
			OutputStream output = new FileOutputStream(outputFile);
			output.write("G28\n".getBytes(StandardCharsets.UTF_8));
			output.write("G90\n".getBytes(StandardCharsets.UTF_8));
			output.write(("G54 X-30 Z-"+tool_offset_z+"\n").getBytes(StandardCharsets.UTF_8));
			
			turtle_x=0;
			turtle_y=0;
			turtle_dx=0;
			turtle_dy=-1;
			turtle_step = (float)((xmax-xmin) / (Math.pow(2, order)));

			// Draw bounding box
			output.write("G90\n".getBytes(StandardCharsets.UTF_8));
			output.write(("G0 Z"+z_up+"\n").getBytes(StandardCharsets.UTF_8));
			output.write(("G0 X"+xmax+" Y"+ymax+"\n").getBytes(StandardCharsets.UTF_8));
			output.write(("G0 Z"+z_down+"\n").getBytes(StandardCharsets.UTF_8));
			output.write(("G0 X"+xmax+" Y"+ymin+"\n").getBytes(StandardCharsets.UTF_8));
			output.write(("G0 X"+xmin+" Y"+ymin+"\n").getBytes(StandardCharsets.UTF_8));
			output.write(("G0 X"+xmin+" Y"+ymax+"\n").getBytes(StandardCharsets.UTF_8));
			output.write(("G0 X"+xmax+" Y"+ymax+"\n").getBytes(StandardCharsets.UTF_8));
			output.write(("G0 Z"+z_up+"\n").getBytes(StandardCharsets.UTF_8));

			// move to starting position
			output.write("G91\n".getBytes(StandardCharsets.UTF_8));
			output.write(("G0 X"+(-turtle_step/2)+" Y"+(-turtle_step/2)+"\n").getBytes(StandardCharsets.UTF_8));
						
			// do the curve
			output.write("G90\n".getBytes(StandardCharsets.UTF_8));
			output.write(("G0 Z"+z_down+"\n").getBytes(StandardCharsets.UTF_8));
			
			output.write("G91\n".getBytes(StandardCharsets.UTF_8));
			hilbert(output,order);
			
			output.write("G90\n".getBytes(StandardCharsets.UTF_8));
			output.write(("G0 Z"+z_up+"\n").getBytes(StandardCharsets.UTF_8));

			// finish up
			output.write("G28\n".getBytes(StandardCharsets.UTF_8));
			
        	output.flush();
	        output.close();
	        
			// open the file automatically to save a click.
			gui.openFile(outputFile);
		}
		catch(IOException ex) {}
	}
	
	
    // Hilbert curve
    private void hilbert(OutputStream output,int n) throws IOException {
        if (n == 0) return;
        turtle_turn(90);
        treblih(output,n-1);
        turtle_goForward(output);
        turtle_turn(-90);
        hilbert(output,n-1);
        turtle_goForward(output);
        hilbert(output,n-1);
        turtle_turn(-90);
        turtle_goForward(output);
        treblih(output,n-1);
        turtle_turn(90);
    }


    // evruc trebliH
    public void treblih(OutputStream output,int n) throws IOException {
        if (n == 0) return;
        turtle_turn(-90);
        hilbert(output,n-1);
        turtle_goForward(output);
        turtle_turn(90);
        treblih(output,n-1);
        turtle_goForward(output);
        treblih(output,n-1);
        turtle_turn(90);
        turtle_goForward(output);
        hilbert(output,n-1);
        turtle_turn(-90);
    }
    

    public void turtle_turn(float degrees) {
    	double n = degrees * Math.PI / 180.0;
    	double newx =  Math.cos(n) * turtle_dx + Math.sin(n) * turtle_dy;
    	double newy = -Math.sin(n) * turtle_dx + Math.cos(n) * turtle_dy;
    	double len = Math.sqrt(newx*newx + newy*newy);
    	assert(len>0);
    	turtle_dx = (float)(newx/len);
    	turtle_dy = (float)(newy/len);
    }

    
    public void turtle_goForward(OutputStream output) throws IOException {
    	//turtle_x += turtle_dx * distance;
    	//turtle_y += turtle_dy * distance;
    	//output.write(new String("G0 X"+(turtle_x)+" Y"+(turtle_y)+"\n").getBytes());
    	output.write(("G0 X"+(turtle_dx*turtle_step)+" Y"+(turtle_dy*turtle_step)+"\n").getBytes(StandardCharsets.UTF_8));
    }
}
