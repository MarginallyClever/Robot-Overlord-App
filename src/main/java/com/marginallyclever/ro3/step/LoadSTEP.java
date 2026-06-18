package com.marginallyclever.ro3.step;

import com.marginallyclever.ro3.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LoadSTEP {
    private static final Logger logger = LoggerFactory.getLogger(LoadSTEP.class);
    private final List<String> headers = new ArrayList<>();

    public Node createFromStream(BufferedInputStream bis) throws IOException {
        var reader = new BufferedReader(new InputStreamReader(bis));
        verifyLine(reader,"ISO-10303-21;");
        loadHeader(reader);
        loadData(reader);
        verifyLine(reader,"END-ISO-10303-21;");
        return null;
    }

    private void verifyLine(BufferedReader reader,String match) throws IOException {
        // read one line of bis and confirm it says "ISO-10303-21"
        String line = reader.readLine();
        if(!line.equalsIgnoreCase(match)) {
            throw new IOException("Expected "+match+" but found " + line);
        }
    }

    private void loadHeader(BufferedReader reader) throws IOException {
        verifyLine(reader,"HEADER;");

        String line;
        while((line = reader.readLine()) != null) {
            if (line.equalsIgnoreCase("ENDSEC;")) break;
            headers.add(line);
        }
    }

    private void loadData(BufferedReader reader) throws IOException {
        verifyLine(reader, "DATA;");

        String line;
        while((line = reader.readLine()) != null) {
            if (line.equalsIgnoreCase("ENDSEC;")) break;
        }
    }
}
