package com.marginallyclever.robotOverlord;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Test;

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
				for(int i=0;i<app1.links.length;++i) {
					app1.links[i].d    .set(Math.random()*360.0-180.0);
					app1.links[i].theta.set(Math.random()*360.0-180.0);
					app1.links[i].r    .set(Math.random()*360.0-180.0);
					app1.links[i].alpha.set(Math.random()*360.0-180.0);
				}
				app1.saveToFolder(new File(TEST_FOLDER));
				
				DHBuilderApp app2 = new DHBuilderApp();
				app2.loadFromFolder(new File(TEST_FOLDER));
				assert(app2.links.length==app1.links.length);
				Double d1,d2;
				for(int i=0;i<app1.links.length;++i) {
					d1 = (app1.links[i].d.get()); 
					d2 = (app2.links[i].d.get()); 
					assert(Math.abs(d1-d2)<1e-6);
					d1 = (app1.links[i].theta.get()); 
					d2 = (app2.links[i].theta.get()); 
					assert(Math.abs(d1-d2)<1e-6);
					d1 = (app1.links[i].r.get()); 
					d2 = (app2.links[i].r.get()); 
					assert(Math.abs(d1-d2)<1e-6);
					d1 = (app1.links[i].alpha.get()); 
					d2 = (app2.links[i].alpha.get()); 
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
