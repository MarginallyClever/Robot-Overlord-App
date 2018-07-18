package com.marginallyclever.robotOverlord;

import org.junit.Test;
import org.tensorflow.*;

public class TensorFlowTest {
	
	@Test
	public void testHello() throws Exception {
	    try (Graph graph = new Graph()) {
	    	final String value = "Hello from " + TensorFlow.version();

	    	// Construct the computation graph with a single operation, a constant
	    	// named "MyConst" with a value "value".
	    	try (Tensor<?> t = Tensor.create(value.getBytes("UTF-8"))) {
	    		// The Java API doesn't yet include convenience functions for adding operations.
	    		graph.opBuilder("Const", "MyConst").setAttr("dtype", t.dataType()).setAttr("value", t).build();
	    	}

	    	// Execute the "MyConst" operation in a Session.
	    	try (Session s = new Session(graph);
	    			Tensor<?> output = s.runner().fetch("MyConst").run().get(0)) 
	    	{
	    		System.out.println(new String(output.bytesValue(), "UTF-8"));
	    	}
	    }
	}
}
