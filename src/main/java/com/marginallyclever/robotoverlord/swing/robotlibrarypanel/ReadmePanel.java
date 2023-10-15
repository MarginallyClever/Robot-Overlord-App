package com.marginallyclever.robotoverlord.swing.robotlibrarypanel;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import okhttp3.OkHttpClient;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

/**
 * Displays the README.md file from a GitHub repository.
 * @author Dan Royer
 * @since 2.5.0
 */
public class ReadmePanel extends JPanel {
    private static final OkHttpClient client = new OkHttpClient();

    public ReadmePanel(URL url) {
        setLayout(new BorderLayout());

        JEditorPane contentPane = new JEditorPane();
        contentPane.setContentType("text/html");
        contentPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(contentPane);
        add(scrollPane, BorderLayout.CENTER);

        try {
            String readmeHtml = getReadmeHtml(url);
            contentPane.setText(readmeHtml);
        } catch (IOException e) {
            contentPane.setText("Error: Unable to load README.md file.");
            e.printStackTrace();
        }
    }

    private String getReadmeHtml(URL url) throws IOException {
        String responseBody = GithubFetcher.getAPIFileFromRepo(url, "readme");
        return convertMarkdownToHtml(responseBody);
    }

    private String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}