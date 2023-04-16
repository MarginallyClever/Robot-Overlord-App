package com.marginallyclever.robotoverlord.swinginterface.pluginExplorer;

import javax.swing.*;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ReadmePanel extends JPanel {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String GITHUB_API = "https://api.github.com/repos/";

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
        String repoPath = url.getPath().substring(1);
        String apiUrl = GITHUB_API + repoPath + "/readme";

        Request request = new Request.Builder().url(apiUrl).build();
        Response response = client.newCall(request).execute();

        if (response.isSuccessful() && response.body() != null) {
            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);
            String readmeContent = jsonResponse.getString("content");
            String decodedReadmeContent = decodeBase64(readmeContent);
            return convertMarkdownToHtml(decodedReadmeContent);
        } else {
            throw new IOException("Failed to fetch README.md content");
        }
    }

    private String decodeBase64(String base64String) {
        String cleanBase64String = base64String.replaceAll("\\s", "");
        byte[] decodedBytes = Base64.getDecoder().decode(cleanBase64String);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    private String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
