package com.marginallyclever.convenience.log;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * A panel that displays the contents of a log file in a {@link JList}.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
@Deprecated
public class LogPanel2 extends JPanel {
    private final Path logFilePath;
    private final LogListModel logListModel = new LogListModel();
    private final JList<String> logList = new JList<>(logListModel);
    private final JScrollPane scrollPane = new JScrollPane(logList);

    public LogPanel2(String logFilePath) {
        super(new BorderLayout());
        this.logFilePath = Paths.get(logFilePath);

        logList.setCellRenderer(new LogListCellRenderer());

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
                logListModel.setTotalLines(totalLines);
                logListModel.fireContentsChanged();

                scrollToBottom();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void scrollToBottom() {
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }

    private boolean isScrolledToBottom(JViewport viewport) {
        Point currentPosition = viewport.getViewPosition();
        Dimension viewSize = viewport.getExtentSize();
        return currentPosition.y + viewSize.height >= logList.getHeight();
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

    private class LogListModel extends AbstractListModel<String> {
        private long totalLines;

        void setTotalLines(long totalLines) {
            this.totalLines = totalLines;
        }

        @Override
        public int getSize() {
            return (int) totalLines;
        }

        @Override
        public String getElementAt(int index) {
            try {
                return Files.lines(logFilePath, StandardCharsets.UTF_8)
                        .skip(index)
                        .findFirst()
                        .orElse("");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        void fireContentsChanged() {
            fireContentsChanged(this, 0, getSize() - 1);
        }
    }

    private static class LogListCellRenderer extends JLabel implements ListCellRenderer<String> {
        LogListCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            setText(value);

            Color background = isSelected ? list.getSelectionBackground() : list.getBackground();
            Color foreground = isSelected ? list.getSelectionForeground() : list.getForeground();

            setBackground(background);
            setForeground(foreground);

            return this;
        }
    }

    public static JFrame createFrame(String path) {
        JFrame frame = new JFrame(Log.getLogLocation());
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(600, 400));
        frame.add(new LogPanel2(path));
        frame.pack();
        return frame;
    }

    public static void main(String[] args) {
        Log.start();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Log Viewer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.add(new LogPanel2(Log.getLogLocation()));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
