package com.marginallyclever.ro3.urdf;

import com.marginallyclever.ro3.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveURDF {
    private static final Logger logger = LoggerFactory.getLogger(SaveURDF.class);
    private Node root;
    private StringBuilder sb = new StringBuilder();

    /**
     * Save a Node and its children as a URDF XML string.
     * @param root The root Node to save.
     * @return A string containing the URDF XML representation of the Node.
     */
    public String saveAsXML(Node root) {
        this.root = root;
        buildVisuals();
        buildJoints();
        writeXML();
        return sb.toString();
    }

    private void buildVisuals() {
        logger.error("SaveURDF.buildVisuals() not yet implemented.");
    }

    private void buildJoints() {
        logger.error("SaveURDF.buildJoints() not yet implemented.");
    }

    private void writeXML() {
        logger.error("SaveURDF.writeXML() not yet implemented.");
    }
}
