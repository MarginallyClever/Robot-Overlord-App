package com.marginallyclever.robotoverlord.components.shapes;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.ComponentDependency;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.FilenameParameter;
import com.marginallyclever.robotoverlord.systems.render.material.MaterialFactory;
import com.marginallyclever.robotoverlord.systems.render.mesh.load.MeshFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A mesh loaded from a file.
 *
 * @author Dan Royer
 * @since 1.0.0
 */
@ComponentDependency(components = {MaterialComponent.class})
public class MeshFromFile extends ShapeComponent {
    private static final Logger logger = LoggerFactory.getLogger(MeshFromFile.class);

    public final FilenameParameter filename = new FilenameParameter("File","");

    public MeshFromFile() {
        super();
    }

    public MeshFromFile(String filename) {
        this();
        setFilename(filename);
    }

    @Override
    public void onAttach() {
        super.onAttach();
        Entity entity = getEntity();
        String absolutePath = filename.get();
        if(!absolutePath.trim().isEmpty() && MeshFactory.hasMaterial(absolutePath)) {
            logger.debug("MeshFromFile: adding material for "+absolutePath);
            String materialPath = MeshFactory.getMaterialPath(absolutePath);
            entity.removeComponent(entity.getComponent(MaterialComponent.class));
            entity.addComponent(MaterialFactory.load(materialPath));
        }
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        jo.put("filename",filename.toJSON(context));
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
        super.parseJSON(jo,context);
        filename.parseJSON(jo.getJSONObject("filename"),context);
        load();
    }

    public void setFilename(String name) {
        filename.set(name);
    }

    public String getFilename() {
        return filename.get();
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
