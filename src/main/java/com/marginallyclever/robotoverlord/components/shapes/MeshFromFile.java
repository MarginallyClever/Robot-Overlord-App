package com.marginallyclever.robotoverlord.components.shapes;

import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.mesh.load.MeshFactory;
import com.marginallyclever.robotoverlord.parameters.StringEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.ArrayList;

public class MeshFromFile extends ShapeComponent {
    protected final StringEntity filename = new StringEntity("File","");

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
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jo = super.toJSON();

        Scene myScene = getScene();
        if(myScene!=null) {
            StringEntity newFilename = new StringEntity("File",myScene.removeScenePath(filename.get()));
            jo.put("filename",newFilename.toJSON());
        } else {
            jo.put("filename",filename.toJSON());
        }

        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo) throws JSONException {
        super.parseJSON(jo);

        StringEntity newFilename = new StringEntity("File","");
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
