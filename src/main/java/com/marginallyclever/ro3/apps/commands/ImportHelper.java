package com.marginallyclever.ro3.apps.commands;

import com.marginallyclever.ro3.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ImportHelper {
    private static final Logger logger = LoggerFactory.getLogger(ImportHelper.class);

    /**
     * <p>When importing an asset it might already be loaded.  the two sets would have matching UUIDs,
     * which would confuse the system.  To avoid this, we replace all UUIDs in the content with new ones.
     * {@link Node#witnessProtection()} after the fact changes the UUIDs but not the {@link com.marginallyclever.ro3.node.NodePath}s that refer to them,
     * which breaks all internal links.  To solve this, we do the replacement before loading.</p>
     * <p>Search the content for all reference to "nodeID".  Get the UUID that follows it, and then
     * replace every instance of that UUID with a new one.</p>
     */
    public static String witnessProtectionBeforeLoad(String content) {
        logger.debug("Replacing UUIDs in content");
        int count=0;
        int first = 0;
        do {
            // find "nodeID"
            var location = content.indexOf("nodeID", first);
            if( location == -1 ) break; // no more found
            // find the UUID that follows it
            var start = content.indexOf(":", location) + 2;
            var end = content.indexOf("\"", start);
            var oldUUID = content.substring(start, end);
            var newUUID = UUID.randomUUID();
            content = content.replace(oldUUID,newUUID.toString());
            count++;
            first = start;
        } while(true);

        logger.debug("Replaced {} UUIDs in content",count);

        return content;
    }
}
