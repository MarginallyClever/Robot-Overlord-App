open module com.marginallyclever.robotoverlord {
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
	requires modern_docking.api;
	requires modern_docking.single_app;
	requires modern_docking.ui_ext;
	requires com.formdev.flatlaf;
	requires com.github.weisj.jsvg;
	requires org.reflections;
	
	requires vecmath;
	requires jsch;
	requires org.jetbrains.annotations;
	requires jssc;
	requires batik.all;
	requires xml.apis.ext;
	requires java.datatransfer;
	requires org.ode4j;
	requires flexmark.util.ast;
	requires webcam.capture;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
}