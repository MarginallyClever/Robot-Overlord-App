package Generators;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.marginallyclever.evilOverlord.MainGUI;

public class YourMessageHereGenerator implements GcodeGenerator {
	// machine properties
	protected float feed_rate=1800.0f;
	protected float feed_rate_rapid=3000.0f;
	protected float z_up=3.0f;
	protected float z_down=1.8f;
	protected float scale=1.0f;
	
	// font properties
	protected float kerning=0.20f;
	protected float letter_width=1.0f;
	protected float letter_height=2.0f;
	protected float line_spacing=0.50f;
	protected float padding=0.20f;
	static final String alphabetFolder = System.getProperty("user.dir") + "/" + "ALPHABET/";
	protected String outputFile = System.getProperty("user.dir") + "/" + "TEMP.NGC";
	protected int chars_per_line=35;
	protected String lastMessage = "";

	// text position and alignment
	public enum VAlign { TOP, MIDDLE, BOTTOM };
	public enum Align { LEFT, CENTER, RIGHT };
	protected VAlign align_vertical = VAlign.MIDDLE;
	protected Align  align_horizontal = Align.CENTER;
	protected float posx=0;
	protected float posy=0;
	
	// debugging
	protected boolean draw_bounding_box = true;
	

	
	MainGUI gui;
	
	
	public YourMessageHereGenerator(MainGUI _gui) {
		super();
		gui = _gui;
	}
	
	public String GetMenuName() {
		return "Your message here";
	}
	
	
	public void Generate() {
		final JDialog driver = new JDialog(gui.GetMainFrame(),"Your Message Here",true);
		driver.setLayout(new GridLayout(0,1));

		final JTextArea text = new JTextArea(lastMessage,40,4);

		driver.add(new JLabel("Message"));
		driver.add(new JScrollPane(text));

		final JTextField field_up = new JTextField(Float.toString(z_up));
		final JTextField field_down = new JTextField(Float.toString(z_down));
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
					lastMessage=text.getText();
					TextCreateMessageNow(lastMessage);
					// open the file automatically to save a click.
					gui.OpenFile(outputFile);
					
					driver.dispose();
				}
				if(subject == buttonCancel) {
					driver.dispose();
				}
			}
		};
		
		buttonSave.addActionListener(driveButtons);
		buttonCancel.addActionListener(driveButtons);

		driver.setSize(300,300);
		driver.setVisible(true);
	}

	
	protected String [] SplitForLength(String src) {
		String [] test_lines = src.split(";\n");
		int i,j;
		
		int num_lines = 0;
		for(i=0;i<test_lines.length;++i) {
			if( test_lines[i].length() > chars_per_line ) {
				int x = (int)Math.ceil( (double)test_lines[i].length() / (double)chars_per_line );
				num_lines += x;	
			} else {
				num_lines++;
			}
		}

		String [] lines = new String[num_lines];
		j=0;
		for(i=0;i<test_lines.length;++i) {
			if(test_lines[i].length() < chars_per_line) {
				lines[j++] = test_lines[i];
			}
			if(test_lines[i].length()>chars_per_line) {
				String [] temp = test_lines[i].split("(?<=\\G.{"+chars_per_line+"})");
				for(int k=0;k<temp.length;++k) {
					lines[j++] = temp[k];
				}
			}
		}
		
		return lines;
	}
	
	protected int LongestLine(String [] lines) {
		int len=0;
		for(int i=0;i<lines.length;++i) {
			if(len < lines[i].length()) len = lines[i].length();
		}
		return len;
	}
	
	protected void TextCreateMessageNow(String text) {
		System.out.println("output file = "+outputFile);
		
		try {
			OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(outputFile),"UTF-8");
			output.write("G28;\n");
			output.write("G90;\n");
			//posy=250;
			output.write("G54 Y26;\n");
			output.write("G0 X20 F"+feed_rate_rapid+";\n");
			output.write("G0 Y0;\n");
			output.write("G0 X0;\n");

			TextCreateMessageNow(text,output);

			output.write(("G90;\n"));
			output.write(("G0 Z15 F"+feed_rate_rapid+";\n"));
			
        	output.flush();
	        output.close();
		}
		catch(IOException ex) {}
	}
	
	
	protected void TextCreateMessageNow(String text,OutputStreamWriter output) throws IOException {
		if(chars_per_line<=0) return;
		
		// find size of text block
		// TODO count newlines
		Rectangle2D r = TextCalculateBounds(text);

		output.write("G90;\n");
		liftPen(output);
		
		if(draw_bounding_box) {
			// draw bounding box
			output.write("G0 X"+TX((float)r.getMinX())+" Y"+TY((float)r.getMaxY())+";\n");
			lowerPen(output);
			output.write("G0 X"+TX((float)r.getMaxX())+" Y"+TY((float)r.getMaxY())+";\n");
			output.write("G0 X"+TX((float)r.getMaxX())+" Y"+TY((float)r.getMinY())+";\n");
			output.write("G0 X"+TX((float)r.getMinX())+" Y"+TY((float)r.getMinY())+";\n");
			output.write("G0 X"+TX((float)r.getMinX())+" Y"+TY((float)r.getMaxY())+";\n");
			liftPen(output);
		}
		
		// move to first line height
		// assumes we are still G90
		float message_start = TX((float)r.getMinX()) + SX(padding);
		float firstline = TY((float)r.getMaxY()) - SY(padding + letter_height);
		float interline = -SY(letter_height + line_spacing); 

		output.write("G0 X"+message_start+" Y"+firstline+";\n");
		output.write("G91;\n");

		// draw line of text
		String [] lines = TextWrapToLength(text);
		for(int i=0; i<lines.length; i++) {
			if(i>0) {
				// newline
				output.write("G0 Y"+interline+";\n");
				// carriage return
				output.write("G90;\n");
				output.write("G0 X"+message_start+";\n");
				output.write("G91;\n");
			}
			
			TextDrawLine(lines[i],output);
		}

		output.write("G90;\n");
		liftPen(output);
	}
	
	
 	protected void TextDrawLine(String a1,OutputStreamWriter output) throws IOException {
		//System.out.println(a1+" ("+a1.length()+")");
		
		int i=0;
		for(i=0;i<a1.length();++i) {
			char letter = a1.charAt(i);
			
			if(letter=='\n' || letter=='\r') continue;

			String name;

			// find the file that goes with this character
			// TODO load these from an XML description
			if('a'<= letter && letter <= 'z') {
				name="SMALL_" + Character.toUpperCase(letter);
			} else {
				switch(letter) {
				case ' ':  name="SPACE";  break;
				case '!':  name="EXCLAMATION";  break;
				case '"':  name="DOUBLEQ";  break;
				case '$':  name="DOLLAR";  break;
				case '#':  name="POUND";  break;
				case '%':  name="PERCENT";  break;
				case '&':  name="AMPERSAND";  break;				
				case '\'':  name="SINGLEQ";  break;
				case '(':  name="B1OPEN";  break;
				case ')':  name="B1CLOSE";  break;
				case '*':  name="ASTERIX";  break;
				case '+':  name="PLUS";  break;
				case ',':  name="COMMA";  break;
				case '-':  name="HYPHEN";  break;
				case '.':  name="PERIOD";  break;
				case '/':  name="FSLASH";  break;
				case ':':  name="COLON";  break;
				case ';':  name="SEMICOLON";  break;
				case '<':  name="GREATERTHAN";  break;
				case '=':  name="EQUAL";  break;
				case '>':  name="LESSTHAN";  break;
				case '?':  name="QUESTION";  break;
				case '@':  name="AT";  break;
				case '[':  name="B2OPEN";  break;
				case ']':  name="B2CLOSE";  break;
				case '^':  name="CARET";  break;
				case '_':  name="UNDERSCORE";  break;
				case '`':  name="GRAVE";  break;
				case '{':  name="B3OPEN";  break;
				case '|':  name="BAR";  break;
				case '}':  name="B3CLOSE";  break;
				case '~':  name="TILDE";  break;
				case '\\':  name="BSLASH";  break;
				case '�':  name="SPACE";  break;
				default: name=Character.toString(letter);  break;
				}
			}
			String fn = alphabetFolder + name  + ".NGC";
			//System.out.print(fn);
			
			
			if(new File(fn).isFile()) {
				if(i>0 && kerning!=0) {
					output.write("G0 X"+SX(kerning)+";\n");
				}
				
				// file found. copy/paste it into the temp file
				//System.out.println(" OK");
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fn),"UTF-8"));

				String b;
		        while ( (b = in.readLine()) != null ) {
		        	if(b.trim().length()==0) continue;
		        	if(b.equals("UP")) {
		         		output.write("G90;\n");
		        		liftPen(output);
		         		output.write("G91;\n");
		        	} else if(b.equals("DOWN")) { 
		         		output.write("G90;\n");
						lowerPen(output);
		         		output.write("G91;\n");
		        	} else {
		        		StringTokenizer st = new StringTokenizer(b);
		        		String gap="";
		        		while (st.hasMoreTokens()) {
		        			String c=st.nextToken();
		        			if(c.startsWith("G")) {
		        				output.write(gap+c);
		        			} else if(c.startsWith("X")) {
				        		// translate coordinates
		        				float x = Float.parseFloat(c.substring(1));  // cm to mm
		        				output.write(gap+"X"+SX(x));
		        			} else if(c.startsWith("Y")) {
				        		// translate coordinates
		        				float x = Float.parseFloat(c.substring(1));  // cm to mm
		        				output.write(gap+"Y"+SY(x));
		        			} else {
				        		output.write(gap+c);
		        			}
		        			gap=" ";
		        		}
		        		output.write(";\n");
		        	}
		        }
		        in.close();
			} else {
				// file not found
				System.out.print(fn);
				System.out.println(" NOK");
			}
		}
	}

	protected float TX(float x) {
		return SX(x-posx);
	}
	
	protected float TY(float y) {
		return SY(y-posy);
	}
	
 	protected float SX(float x) {
 		return x*scale;
 	}
 	
 	protected float SY(float y) {
 		return y*scale;
 	}
 	
 	protected void liftPen(OutputStreamWriter output) throws IOException {
 		output.write("G0 F"+feed_rate_rapid+" Z"+z_up+";\n");
 	}
 	
 	protected void lowerPen(OutputStreamWriter output) throws IOException {
		output.write("G0 F"+feed_rate+" Z"+z_down+";\n");
	}

	
	public void TextSetPosition(float x,float y) {
		posx=x;
		posy=y;
	}
	
	public void TextSetAlign(Align x) {
		align_horizontal = x;
	}
	
	public void TextSetVAlign(VAlign x) {
		align_vertical = x;
	}
	
	public void TextSetCharsPerLine(int numChars) {
		chars_per_line = numChars;
		//System.out.println("MAX="+numChars);
	}
	
	public void TextFindCharsPerLine(float width) {
		chars_per_line=(int)Math.floor( (float)(width - padding*2.0f) / (float)(letter_width+kerning) );
		//System.out.println("MAX="+chars_per_line);
	}
	
	protected Rectangle2D TextCalculateBounds(String text) {
		String [] lines = TextWrapToLength(text);
		int len = TextLongestLine(lines);
		
		int num_lines = lines.length;
		float h = padding*2 + ( letter_height + line_spacing ) * num_lines;//- line_spacing; removed because of letters that hang below the line
		float w = padding*2 + ( letter_width + kerning ) * len - kerning;
		float xmax=0, xmin=0, ymax=0, ymin=0;
		
		switch(align_horizontal) {
		case LEFT:
			xmax=posx + w;
			xmin=posx;
			break;
		case CENTER:
			xmax = posx + w/2;
			xmin = posx - w/2;
			break;
		case RIGHT:
			xmax = posx;
			xmin = posx - w;
			break;
		default:
			assert(false);
		}
		
		switch(align_vertical) {
		case BOTTOM:
			ymax=posy + h;
			ymin=posy;
			break;
		case MIDDLE:
			ymax = posy + h/2;
			ymin = posy - h/2;
			break;
		case TOP:
			ymax = posy;
			ymin = posy - h;
			break;
		default:
			assert(false);
		}
		/*
		System.out.println(num_lines + " lines");
		System.out.println("longest "+len+" chars");
		System.out.println("x "+xmin+" to "+xmax);
		System.out.println("y "+ymin+" to "+ymax);
		*/
		Rectangle2D r = new Rectangle2D.Float();
		r.setRect((double)xmin, (double)ymin, (double)(xmax - xmin), (double)(ymax - ymin));
		
		return r;
	}
	
	

	protected String [] TextWrapToLength(String src) {
		String [] test_lines = src.split("\n");
		int i,j;
		
		int num_lines = 0;
		for(i=0;i<test_lines.length;++i) {
			if( test_lines[i].length() > chars_per_line ) {
				int x = (int)Math.ceil( (double)test_lines[i].length() / (double)chars_per_line );
				num_lines += x;	
			} else {
				num_lines++;
			}
		}

		String [] lines = new String[num_lines];
		j=0;
		for(i=0;i<test_lines.length;++i) {
			if(test_lines[i].length() < chars_per_line) {
				lines[j++] = test_lines[i];
			}
			if(test_lines[i].length()>chars_per_line) {
				String [] temp = test_lines[i].split("(?<=\\G.{"+chars_per_line+"})");
				for(int k=0;k<temp.length;++k) {
					lines[j++] = temp[k];
				}
			}
		}
		
		return lines;
	}
	
	
	
	protected int TextLongestLine(String [] lines) {
		int len=0;
		for(int i=0;i<lines.length;++i) {
			if(len < lines[i].length()) len = lines[i].length();
		}
		
		return len;
	}
	
}
