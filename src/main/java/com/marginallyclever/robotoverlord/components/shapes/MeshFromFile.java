package com.marginallyclever.robotoverlord.components.shapes;

import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.mesh.load.MeshFactory;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import com.marginallyclever.robotoverlord.uiexposedtypes.StringEntity;

import javax.swing.filechooser.FileFilter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MeshFromFile extends ShapeComponent {
    protected final StringEntity filename = new StringEntity("File","");

    public MeshFromFile() {
        super();
        filename.addPropertyChangeListener((e)->{
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
    public void save(BufferedWriter writer) throws IOException {
        super.save(writer);
        filename.save(writer);
    }

    @Override
    public void load(BufferedReader reader) throws Exception {
        super.load(reader);
        filename.load(reader);
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
