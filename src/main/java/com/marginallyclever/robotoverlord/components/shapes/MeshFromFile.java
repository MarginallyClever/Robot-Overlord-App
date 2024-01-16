package com.marginallyclever.robotoverlord.components.shapes;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.ComponentDependency;
import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.parameters.FilenameParameter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A mesh loaded from a file.
 *
 */
@ComponentDependency(components = {MaterialComponent.class})
@Deprecated
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
    }

    public void load() {
    }
}
