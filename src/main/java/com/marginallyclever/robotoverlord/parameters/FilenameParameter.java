package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.SerializationContext;
import org.json.JSONException;
import org.json.JSONObject;

public class FilenameParameter extends StringParameter {
    public FilenameParameter(String name, String value) {
        super(name, value);
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        String filename = get();
        if(filename!=null && !filename.trim().isEmpty() && filename.startsWith(context.getProjectAbsPath())) {
            jo.put("value", filename.substring(context.getProjectAbsPath().length()));
            jo.put("absolute", false);
        } else {
            jo.put("value", filename);
            jo.put("absolute", true);
        }
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
        super.parseJSON(jo,context);
        // if value is null it will not appear in the JSON.
        if(jo.has("value")) {
            String filename = jo.getString("value");
            if(filename==null || filename.trim().isEmpty()) return;

            if(!jo.has("absolute") || !jo.getBoolean("absolute")) {
                filename = context.getProjectAbsPath() + filename;
            }
            set(filename);
        }
    }
}
