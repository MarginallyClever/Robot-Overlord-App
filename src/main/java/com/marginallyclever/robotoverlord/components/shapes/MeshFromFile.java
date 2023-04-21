package com.marginallyclever.robotoverlord.components.shapes;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.components.material.MaterialFactory;
import com.marginallyclever.robotoverlord.components.shapes.mesh.load.MeshFactory;
import com.marginallyclever.robotoverlord.parameters.StringParameter;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.ArrayList;

public class MeshFromFile extends ShapeComponent {
    private static final Logger logger = LoggerFactory.getLogger(MeshFromFile.class);

    protected final StringParameter filename = new StringParameter("File","");

    public MeshFromFile() {
        super();
        filename.addPropertyChangeListener((e)->{
            String fn = checkForScenePath(filename.get());
            setModel(MeshFactory.load(fn));
        });
    }

    public MeshFromFile(String filename) {
        this();
        setFilename(filename);
    }

    @Override
    public void setEntity(Entity entity) {
        if(entity != null && entity.findFirstComponent(MaterialComponent.class)==null) {
            // no material, add one.
            String absolutePath = filename.get();
            if(!absolutePath.trim().isEmpty() && MeshFactory.hasMaterial(absolutePath)) {
                logger.debug("MeshFromFile: adding material for "+absolutePath);
                String materialPath = MeshFactory.getMaterialPath(absolutePath);
                MaterialComponent material = MaterialFactory.load(materialPath);
                entity.addComponent(material);
            } else {
                entity.addComponent(new MaterialComponent());
            }
        }

        super.setEntity(entity);
    }

    private String checkForScenePath(String fn) {
        Scene myScene = getScene();
        if(myScene!=null) {
            if (!myScene.isAssetPathInScenePath(fn)) {
                String fn2 = myScene.addScenePath(fn);
                if ((new File(fn2)).exists()) {
                    return fn2;
                }
            } else {
                myScene.warnIfAssetPathIsNotInScenePath(fn);
            }
        }
        return fn;
    }

    @Override
    public void getView(ViewPanel view) {
        super.getView(view);
        ArrayList<FileFilter> filters = MeshFactory.getAllExtensions();
        view.addFilename(filename,filters);
        view.addButton("Reload").addActionEventListener(e -> {
            MeshFactory.reload(myMesh);
        });
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jo = super.toJSON();

        Scene myScene = getScene();
        if(myScene!=null) {
            StringParameter newFilename = new StringParameter("File",myScene.removeScenePath(filename.get()));
            jo.put("filename",newFilename.toJSON());
        } else {
            jo.put("filename",filename.toJSON());
        }

        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo) throws JSONException {
        super.parseJSON(jo);

        StringParameter newFilename = new StringParameter("File","");
        newFilename.parseJSON(jo.getJSONObject("filename"));

        String fn = newFilename.get();
        if(!(new File(fn)).exists()) {
            Scene myScene = getScene();
            if(myScene!=null) {
                newFilename.set(myScene.addScenePath(fn));
            }
        }
        filename.set(newFilename.get());
    }

    public void setFilename(String name) {
        filename.set(name);
    }

    public String getFilename() {
        return new String(filename.get());
    }

    @Override
    public String toString() {
        return super.toString()+",\n"
                + filename.toString();
    }

}
