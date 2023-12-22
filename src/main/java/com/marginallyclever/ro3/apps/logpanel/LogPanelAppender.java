package com.marginallyclever.ro3.apps.logpanel;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * {@link LogPanelAppender} is a {@link ch.qos.logback.core.Appender} that appends log messages to a {@link LogPanel}.
 */
public class LogPanelAppender extends AppenderBase<ILoggingEvent> {
    private final LogPanel logPanel;

    public LogPanelAppender(LogPanel logPanel) {
        this.logPanel = logPanel;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        logPanel.appendToLog(eventObject.getFormattedMessage());
    }
}