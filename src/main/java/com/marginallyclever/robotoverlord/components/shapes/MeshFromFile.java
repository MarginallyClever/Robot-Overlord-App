package com.marginallyclever.robotoverlord.components.shapes;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.components.material.MaterialFactory;
import com.marginallyclever.robotoverlord.components.shapes.mesh.load.MeshFactory;
import com.marginallyclever.robotoverlord.parameters.StringParameter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeshFromFile extends ShapeComponent {
    private static final Logger logger = LoggerFactory.getLogger(MeshFromFile.class);

    public final StringParameter filename = new StringParameter("File","");

    public MeshFromFile() {
        super();
        filename.addPropertyChangeListener(e->load());
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
        load();
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

    public void reload() {
        MeshFactory.reload(myMesh);
    }

    public void load() {
        setModel(MeshFactory.load(filename.get()));
    }
}
