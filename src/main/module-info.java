module com.marginallyclever.robotOverlord {
	requires transitive java.desktop;
	requires transitive java.prefs;
	requires transitive java.logging;
	requires org.apache.commons.io;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;
	requires org.json;
	requires org.slf4j;
	
	requires transitive vecmath;
	requires transitive jsch;
	requires transitive jinput;
	requires transitive jogamp.fat;
	requires transitive annotations;
	requires transitive jssc;
	requires transitive batik.all;
	requires transitive xml.apis.ext;

	exports com.marginallyclever.robotOverlord;
}