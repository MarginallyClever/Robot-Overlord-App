package com.marginallyclever.ro3.apps.logpanel;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class OpenLogFileLocation extends AbstractAction {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(OpenLogFileLocation.class);

    public OpenLogFileLocation() {
        super();
        putValue(Action.NAME,"Open Log File Location");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-folder-16.png"))));
        putValue(Action.SHORT_DESCRIPTION,"Open the folder containing the log file.");
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        String logFileName = getLogFile();
        if(logFileName==null) {
            logger.error("Failed to find log file.");
            return;
        }

        logger.debug("Opening log file location: "+logFileName);
        try {
            File file = new File(logFileName);
            String absolutePath = file.getAbsolutePath();
            File parentDirectory = new File(absolutePath).getParentFile();
            Desktop.getDesktop().open(parentDirectory);
        } catch(IOException ex) {
            logger.error("Failed to open log file location.",ex);
        }
    }

    private String getLogFile() {
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        for (Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders(); it.hasNext(); ) {
            Appender<ILoggingEvent> appender = it.next();
            if (appender instanceof FileAppender<ILoggingEvent>) {
                FileAppender<?> fileAppender = (FileAppender<?>) appender;
                return fileAppender.getFile();
            }
        }
        return null;
    }
}