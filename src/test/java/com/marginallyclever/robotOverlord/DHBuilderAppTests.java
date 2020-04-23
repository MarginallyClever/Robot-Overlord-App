package com.marginallyclever.robotOverlord;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Test;

import com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.DHLink;
import com.marginallyclever.robotOverlord.entity.scene.robotEntity.DHBuilderApp;

public class DHBuilderAppTests {
	final static String TEST_FOLDER = "src\\\\main\\resources\\Sixi";
	@Test
	public void testSave() throws IOException {
		DHBuilderApp app = new DHBuilderApp();
		app.saveToFolder(new File(TEST_FOLDER));
	}

	@Test
	public void testLoad() throws IOException {
		DHBuilderApp app = new DHBuilderApp();
		app.loadFromFolder(new File(TEST_FOLDER));
	}

	@Test
	public void testSaveAndLoad() throws IOException {
		try {
			for(int j=0;j<50;++j) {
				DHBuilderApp app1 = new DHBuilderApp();
				for(DHLink bone : app1.links ) {
					bone.d    .set(Math.random()*360.0-180.0);
					bone.theta.set(Math.random()*360.0-180.0);
					bone.r    .set(Math.random()*360.0-180.0);
					bone.alpha.set(Math.random()*360.0-180.0);
				}
				app1.saveToFolder(new File(TEST_FOLDER));
				
				DHBuilderApp app2 = new DHBuilderApp();
				app2.loadFromFolder(new File(TEST_FOLDER));
				assert(app2.links.size()==app1.links.size());
				Double d1,d2;
				for(int i=0;i<app1.links.size();++i) {
					DHLink bone1 = app1.links.get(i);
					DHLink bone2 = app2.links.get(i);
					
					d1 = (bone1.d.get()); 
					d2 = (bone2.d.get()); 
					assert(Math.abs(d1-d2)<1e-6);
					d1 = (bone1.theta.get()); 
					d2 = (bone2.theta.get()); 
					assert(Math.abs(d1-d2)<1e-6);
					d1 = (bone1.r.get()); 
					d2 = (bone2.r.get()); 
					assert(Math.abs(d1-d2)<1e-6);
					d1 = (bone1.alpha.get()); 
					d2 = (bone2.alpha.get()); 
					assert(Math.abs(d1-d2)<1e-6);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@After
	public void cleanup() {
		File f = new File(TEST_FOLDER);
		f.delete();
	}
}
