module com.marginallyclever.robotoverlord {
	requires transitive java.desktop;
	requires transitive java.prefs;
	requires transitive java.logging;
	requires org.apache.commons.io;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;
	requires org.json;
	requires org.slf4j;
	requires flexmark;
	requires okhttp3;
	requires com.google.gson;
	requires org.eclipse.jgit;
	requires org.joml;
	requires jogl.all;
	
	requires vecmath;
	requires jsch;
	requires org.jetbrains.annotations;
	requires jssc;
	requires batik.all;
	requires xml.apis.ext;
	requires java.datatransfer;
	requires core;
	requires flexmark.util.ast;
	requires webcam.capture;

	// AFAIK this is only needed for the test 'GCodePathLoaderTest'.
	// I don't know why it throws `InaccessibleObjectException` without this.
    opens com.marginallyclever.robotoverlord.systems.render.gcodepath;
	opens com.marginallyclever.robotoverlord.components;
    opens com.marginallyclever.robotoverlord.components.program;
}