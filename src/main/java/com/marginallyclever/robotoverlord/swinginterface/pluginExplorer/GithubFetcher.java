package com.marginallyclever.robotoverlord.swinginterface.pluginExplorer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class GithubFetcher {

    private static final String GITHUB_API_BASE_URL = "https://api.github.com/repos";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();
    private static final String ROBOT_PROPERTIES_FILE = "robot.properties";

    public static Map<String, String> fetchRobotProperties(String repositoryUrl, String version) throws IOException {
        return fetchRobotPropertiesInternal(repositoryUrl,version);
    }

    public static Map<String, String> fetchRobotProperties(String repositoryUrl) throws IOException {
        return fetchRobotPropertiesInternal(repositoryUrl,"main");
    }

    private static Map<String, String> fetchRobotPropertiesInternal(String repositoryUrl, String branch) throws IOException {
        OkHttpClient client = new OkHttpClient();
        String propertiesUrl = repositoryUrl + "/raw/"+branch+"/"+ROBOT_PROPERTIES_FILE;

        Map<String, String> propertiesMap = new HashMap<>();

        Request request = new Request.Builder().url(propertiesUrl).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String propertiesContent = response.body().string();
                Properties properties = new Properties();
                properties.load(new StringReader(propertiesContent));

                for (String key : properties.stringPropertyNames()) {
                    propertiesMap.put(key, properties.getProperty(key));
                }
            } else {
                throw new IOException("Failed to load properties from URL: " + propertiesUrl);
            }
        }
        return propertiesMap;
    }

    public static List<String> fetchTags(String githubUrl) {
        String[] urlParts = githubUrl.split("/");
        String owner = urlParts[urlParts.length - 2];
        String repo = urlParts[urlParts.length - 1];

        String apiUrl = GITHUB_API_BASE_URL + "/" + owner + "/" + repo + "/tags";

        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Accept", "application/vnd.github+json")
                .build();

        List<Map<String, Object>> tags = null;

        List<String> results = new ArrayList<>();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if(response.code()==403) {
                    System.out.println("Github API rate limit exceeded.  Try again later.");
                    return results;
                } else {
                    throw new IOException("Unexpected code " + response);
                }
            }

            String json = response.body().string();
            Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            tags = gson.fromJson(json, listType);

            for (Map<String, Object> tag : tags) {
                results.add(tag.get("name").toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    public static List<String> lookForLocalCopy(String githubUrl) {
        String[] urlParts = githubUrl.split("/");
        String owner = urlParts[urlParts.length - 2];
        String repo = urlParts[urlParts.length - 1];

        File f = new File(getLocalPath(owner, repo));
        if(f.exists()) {
            List<String> results = findSubfoldersContainingPropertiesFile(f, ROBOT_PROPERTIES_FILE);

            return results;
        }
        return new ArrayList<>();
    }

    private static List<String> findSubfoldersContainingPropertiesFile(File rootFolder, String propertiesFileName) {
        List<String> result = new ArrayList<>();

        if (rootFolder != null && rootFolder.isDirectory()) {
            try {
                Files.walk(rootFolder.toPath())
                        .filter(path -> path.toFile().isDirectory())
                        .forEach(path -> {
                            File propertiesFile = path.resolve(propertiesFileName).toFile();
                            if (propertiesFile.exists() && propertiesFile.isFile()) {
                                result.add(path.toString());
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static String getLocalPath(String owner, String repoName, String tag) {
        Path destinationPath = Paths.get("./scenes", owner, repoName, tag);
        return destinationPath.toString();
    }

    public static String getLocalPath(String owner, String repoName) {
        Path destinationPath = Paths.get("./scenes", owner, repoName);
        return destinationPath.toString();
    }
}
