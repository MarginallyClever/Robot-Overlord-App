import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.shape.ShapeLoadAndSave;
import com.marginallyclever.robotOverlord.dhRobotEntity.dhTool.DHTool;
	
module com.marginallyclever.robotOverlord {
	requires java.desktop;
	requires java.prefs;
	requires java.logging;
	requires junit;
	requires org.apache.commons.io;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;
	requires org.json;
	requires vecmath;
	requires jsch;
	requires jinput;
	requires jogamp.fat;
	requires slf4j.api;
	requires annotations;
	requires jssc;
	
	uses Entity;
	provides Entity with 
		com.marginallyclever.robotOverlord.dhRobotEntity.DHBuilderApp,
		com.marginallyclever.robotOverlord.robots.sixi3.Sixi3IK,
		com.marginallyclever.robotOverlord.robots.LinearStewartPlatform,
		com.marginallyclever.robotOverlord.robots.RotaryStewartPlatform,
		com.marginallyclever.robotOverlord.robots.skycam.Skycam,
		com.marginallyclever.robotOverlord.dhRobotEntity.sixi2.Sixi2,
		com.marginallyclever.robotOverlord.Camera,
		com.marginallyclever.robotOverlord.shape.Shape,
		com.marginallyclever.robotOverlord.Light,
		com.marginallyclever.robotOverlord.Decal,
		com.marginallyclever.robotOverlord.demoAssets.Box,
		com.marginallyclever.robotOverlord.demoAssets.Grid,
		com.marginallyclever.robotOverlord.dhRobotEntity.dhTool.Sixi2ChuckGripper,
		com.marginallyclever.robotOverlord.dhRobotEntity.dhTool.Sixi2LinearGripper,
		com.marginallyclever.robotOverlord.robots.SpotMicro;
	
	uses ShapeLoadAndSave;
	provides ShapeLoadAndSave with
		com.marginallyclever.robotOverlord.shape.shapeLoadAndSavers.ShapeLoadAndSaveAMF,
		com.marginallyclever.robotOverlord.shape.shapeLoadAndSavers.ShapeLoadAndSaveOBJ,
		com.marginallyclever.robotOverlord.shape.shapeLoadAndSavers.ShapeLoadAndSavePLY,
		com.marginallyclever.robotOverlord.shape.shapeLoadAndSavers.ShapeLoadAndSaveSTL;
	
	uses DHTool;
	provides DHTool with
		com.marginallyclever.robotOverlord.dhRobotEntity.dhTool.Sixi2LinearGripper;
}