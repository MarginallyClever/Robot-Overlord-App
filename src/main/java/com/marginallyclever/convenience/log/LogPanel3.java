package com.marginallyclever.convenience.log;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * A panel that displays the contents of a log file in a JTable.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class LogPanel3 extends JPanel {
    private final Path logFilePath;
    private final JTable logTable;
    private final LogTableModel logTableModel;

    public LogPanel3(String logFilePath) {
        this.logFilePath = Paths.get(logFilePath);

        setLayout(new BorderLayout());

        logTableModel = new LogTableModel();
        logTable = new JTable(logTableModel);
        logTable.setFillsViewportHeight(true);
        logTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        logTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(logTable);
        add(scrollPane, BorderLayout.CENTER);

        loadLinesInView();

        scrollPane.getViewport().addChangeListener(e -> {
            JViewport viewport = (JViewport) e.getSource();
            if (isScrolledToBottom(viewport)) {
                loadLinesInView();
            }
        });

        watchLogFile();
    }

    private void loadLinesInView() {
        SwingUtilities.invokeLater(() -> {
            try {
                long totalLines = Files.lines(logFilePath, StandardCharsets.UTF_8).count();
                if (totalLines != logTableModel.getRowCount()) {
                    logTableModel.setTotalLines(totalLines);
                    logTableModel.fireTableDataChanged();
                    scrollToBottom();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            int lastRow = logTable.getRowCount() - 1;
            if (lastRow >= 0) {
                logTable.scrollRectToVisible(logTable.getCellRect(lastRow, 0, true));
            }
        });
    }

    private boolean isScrolledToBottom(JViewport viewport) {
        Point currentPosition = viewport.getViewPosition();
        Dimension viewSize = viewport.getExtentSize();
        return currentPosition.y + viewSize.height >= logTable.getHeight();
    }

    private void watchLogFile() {
        Thread watchThread = new Thread(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                logFilePath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.context().equals(logFilePath.getFileName())) {
                            loadLinesInView();
                        }
                    }
                    key.reset();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        watchThread.setDaemon(true);
        watchThread.start();
    }

    private class LogTableModel extends AbstractTableModel {
        private long totalLines;

        void setTotalLines(long totalLines) {
            this.totalLines = totalLines;
        }

        @Override
        public int getRowCount() {
            return (int) totalLines;
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            try {
                return Files.lines(logFilePath, StandardCharsets.UTF_8)
                        .skip(rowIndex)
                        .findFirst()
                        .orElse("");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getColumnName(int column) {
            return "Log";
        }
    }
    public static JFrame createFrame(String path) {
        JFrame frame = new JFrame(Log.getLogLocation());
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(600, 400));
        frame.add(new LogPanel3(path));
        frame.pack();
        return frame;
    }

    public static void main(String[] args) {
        Log.start();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Log Viewer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.add(new LogPanel3(Log.getLogLocation()));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

