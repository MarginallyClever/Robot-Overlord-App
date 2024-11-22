package com.marginallyclever.ro3.mesh.proceduralmesh;

import com.marginallyclever.ro3.mesh.Mesh;
import org.json.JSONObject;

public abstract class ProceduralMesh extends Mesh {
    /**
     * Procedurally generate a list of triangles
     */
    abstract public void updateModel();

    abstract public String getEnglishName();

    public JSONObject toJSON() {
        var json = new JSONObject();
        json.put("type", getEnglishName());
        return json;
    }

    public void fromJSON(JSONObject from) {}
}
