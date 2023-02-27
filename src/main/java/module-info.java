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
	
	requires vecmath;
	requires jsch;
	requires jinput;
	requires jogamp.fat;
	requires annotations;
	requires jssc;
	requires batik.all;
	requires xml.apis.ext;
	requires java.datatransfer;
	requires core;
}