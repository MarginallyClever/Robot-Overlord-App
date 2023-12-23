package com.marginallyclever.ro3.apps.logpanel;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.marginallyclever.ro3.apps.DockingPanel;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.Properties;

/**
 * {@link LogPanel} is a read-only panel that contains the log.  It cannot be derived from {@link DockingPanel}
 * because it is created before {@link ModernDocking.app.Docking} is initialized.
 */
public class LogPanel extends JPanel {
    private final JTextArea logArea = new JTextArea();

    public LogPanel() {
        super(new BorderLayout());

        logArea.setEditable(false);
        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(logArea);
        add(scroll, BorderLayout.CENTER);

        // append log events to this panel
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        LogPanelAppender appender = new LogPanelAppender(this);
        appender.setContext(lc);
        logger.addAppender(appender);
        appender.start();

        reportSystemInfo(logger);
    }

    private void reportSystemInfo(Logger logger) {
        logger.info("------------------------------------------------");
        Properties p = System.getProperties();
        for(String n : p.stringPropertyNames()) {
            logger.info(n+" = "+p.get(n));
        }
        logger.info("locale = "+ Locale.getDefault());
        logger.info("------------------------------------------------");
    }

    public void appendToLog(String message) {
        logArea.append(message + "\n");
    }
}
