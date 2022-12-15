package com.marginallyclever.robotoverlord.components.shapes;

import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.mesh.load.MeshFactory;
import com.marginallyclever.robotoverlord.parameters.StringEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.filechooser.FileFilter;
import java.util.ArrayList;

public class MeshFromFile extends ShapeComponent {
    protected final StringEntity filename = new StringEntity("File","");

    public MeshFromFile() {
        super();
        filename.addPropertyChangeListener((e)->{
            Scene myScene = getScene();
            if(myScene!=null) {
                myScene.warnIfAssetPathIsNotInScenePath(filename.get());
            }

            setModel(MeshFactory.load(filename.get()));
        });
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
        jo.put("filename",filename.toJSON());
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo) throws JSONException {
        super.parseJSON(jo);
        filename.parseJSON(jo.getJSONObject("filename"));
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
