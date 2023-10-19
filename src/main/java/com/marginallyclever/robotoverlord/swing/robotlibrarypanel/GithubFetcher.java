package com.marginallyclever.robotoverlord.swing.robotlibrarypanel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.marginallyclever.convenience.helpers.PathHelper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Methods for fetching data from <a href="https://www.github.com/">GitHub</a>.
 * @author Dan Royer
 * @since 2.5.0
 */
public class GithubFetcher {
    private static final Logger logger = LoggerFactory.getLogger(GithubFetcher.class);
    private static final String GITHUB_API_BASE_URL = "https://api.github.com/repos";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();
    private static final String ROBOT_PROPERTIES_FILE = "robot.properties";
    private static final String ALL_ROBOTS_TXT = "all_robots.txt";
    public static final String ALL_ROBOTS_PATH = PathHelper.APP_CACHE + File.separator + "all_robots.txt";

    /**
     * Fetches the robot.properties file from the given repository and branch.
     * @param repositoryUrl The URL of the repository to fetch from.
     * @param version The branch to fetch from.
     * @return A map of the properties.
     * @throws IOException If the properties file could not be fetched.
     */
    public static Map<String, String> fetchRobotProperties(String repositoryUrl, String version) throws IOException {
        return fetchRobotPropertiesFromGithub(repositoryUrl,version);
    }

    /**
     * Fetches the robot.properties file from the main branch of the given repository.
     * @param repositoryUrl The URL of the repository to fetch from.
     * @return A map of the properties.
     * @throws IOException If the properties file could not be fetched.
     */
    public static Map<String, String> fetchRobotProperties(String repositoryUrl) throws IOException {
        return fetchRobotPropertiesFromGithub(repositoryUrl,"main");
    }

    /**
     * Fetches the robot.properties file from the given repository and branch.
     * @param repositoryUrl The URL of the repository to fetch from.
     * @param branch The branch to fetch from.
     * @return A map of the properties.
     * @throws IOException If the properties file could not be fetched.
     */
    private static Map<String, String> fetchRobotPropertiesFromGithub(String repositoryUrl, String branch) throws IOException {
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

    /**
     * Fetches the list of tags from the given repository.
     * @param githubUrl The URL of the repository to fetch from.
     * @return A list of the tags.
     */
    public static List<String> fetchTags(String githubUrl) {
        String tagFilePath = getTagFileFromURL(githubUrl);
        if(doesFileExist(tagFilePath)) {
            if(isFileMoreThanOneDayOld(tagFilePath)) {
                deleteFileFromCache(tagFilePath);
            }
        }
        if(!doesFileExist(tagFilePath)) {
            writeFileToCache(tagFilePath,getTagsFromGithub(githubUrl));
        }
        return getFileFromCache(tagFilePath);
    }

    private static String getTagFileFromURL(String githubUrl) {
        String[] urlParts = githubUrl.split("/");
        String owner = urlParts[urlParts.length - 2];
        String repo = urlParts[urlParts.length - 1];
        return PathHelper.APP_CACHE + File.separator + owner+"_"+repo+"_tags.txt";
    }

    private static List<String> getTagsFromGithub(String githubUrl) {
        String[] urlParts = githubUrl.split("/");
        String owner = urlParts[urlParts.length - 2];
        String repo = urlParts[urlParts.length - 1];

        String apiUrl = GITHUB_API_BASE_URL + "/" + owner + "/" + repo + "/tags";

        Request request = new Request.Builder()
                .url(apiUrl)
                .header("Accept", "application/vnd.github+json")
                .build();

        List<String> results = new ArrayList<>();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if(response.code()==403) {
                    System.err.println("Github API rate limit exceeded.  Try again later.");
                    return results;
                } else {
                    throw new IOException("Unexpected code " + response);
                }
            }

            String json = response.body().string();
            Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> tags = gson.fromJson(json, listType);

            for (Map<String, Object> tag : tags) {
                results.add(tag.get("name").toString());
            }
        } catch (IOException e) {
            logger.error("Failed to fetch tags from GitHub", e);
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Fetches the list of locally-installed tags for the given repository.
     * @param githubUrl The URL of the repository to fetch from.
     * @return A list of the tags.
     */
    public static List<String> lookForLocallyInstalledTags(String githubUrl) {
        String[] urlParts = githubUrl.split("/");
        String owner = urlParts[urlParts.length - 2];
        String repo = urlParts[urlParts.length - 1];

        File f = new File(getLocalPath(owner, repo));
        if(f.exists()) {
            return findFoldersContainingPropertiesFile(f);
        }
        return new ArrayList<>();
    }

    /**
     * Searches for all folders that contain a file named ROBOT_PROPERTIES_FILE.
     *
     * @param rootFolder The root folder from which to start the search.
     * @return the list of folder names that meet the search criteria.
     */
    private static List<String> findFoldersContainingPropertiesFile(File rootFolder) {
        List<String> result = new ArrayList<>();

        if (rootFolder != null && rootFolder.isDirectory()) {
            try {
                Files.walk(rootFolder.toPath())
                        .filter(path -> path.toFile().isDirectory())
                        .forEach(path -> {
                            File propertiesFile = path.resolve(ROBOT_PROPERTIES_FILE).toFile();
                            if (propertiesFile.exists() && propertiesFile.isFile()) {
                                result.add(path.toString());
                            }
                        });
            } catch (IOException e) {
                logger.error("Failed to search for robot.properties files", e);
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Gets the local path for the given repository.
     * @param owner The owner of the repository.
     * @param repoName The name of the repository.
     * @param tag The tag of the repository.
     * @return The local path.
     */
    public static String getLocalPath(String owner, String repoName, String tag) {
        Path destinationPath = Paths.get(PathHelper.APP_PLUGINS, owner, repoName, tag);
        return destinationPath.toString();
    }

    /**
     * Gets the local path for the given repository.
     * @param owner The owner of the repository.
     * @param repoName The name of the repository.
     * @return The local path.
     */
    private static String getLocalPath(String owner, String repoName) {
        Path destinationPath = Paths.get(PathHelper.APP_PLUGINS, owner, repoName);
        return destinationPath.toString();
    }

    /**
     * Installs the given repository and tag.
     * @param githubRepositoryUrl The URL of the repository to install.
     * @param tag The tag to install.
     */
    public static void installRepository(String githubRepositoryUrl,String tag) {
        try {
            URI uri = new URI(githubRepositoryUrl);
            String[] urlParts = uri.getPath().split("/");
            String owner = urlParts[1];
            String repoName = urlParts[2];

            File destination = new File(GithubFetcher.getLocalPath(owner,repoName,tag));

            if (!destination.exists()) {
                destination.mkdirs();

                try (Git git = Git.cloneRepository()
                        .setURI(githubRepositoryUrl)
                        .setDirectory(destination)
                        .call()) {

                    Ref tagRef = git.tagList().call().stream()
                            .filter(ref -> ref.getName().equals(Constants.R_TAGS + tag))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Tag not found: " + tag));

                    RevWalk revWalk = new RevWalk(git.getRepository());
                    ObjectId objectId = tagRef.getObjectId();
                    RevCommit commit = revWalk.parseCommit(objectId);
                    git.checkout().setName(commit.getName()).call();
                } catch (GitAPIException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the contents of the readme file from a github repository.
     * @param url The URL of the repository.
     * @param file The name of the file to fetch.
     * @return The contents of the file.
     * @throws IOException If the file could not be fetched.
     */
    public static String getAPIFileFromRepo(URL url, String file) throws IOException {
        String repoPath = url.getPath().substring(1);
        String apiUrl = GITHUB_API_BASE_URL + repoPath + "/" + file;

        Request request = new Request.Builder().url(apiUrl).build();
        Response response = client.newCall(request).execute();

        if (response.isSuccessful() && response.body() != null) {
            String responseBody = response.body().string();
            JSONObject jsonResponse = new JSONObject(responseBody);
            return decodeBase64(jsonResponse.getString("content"));
        } else {
            logger.error("Failed to fetch " + file + " content from " + url+": "+response.code() + " "+response.message());
            throw new IOException("Failed to fetch README.md content");
        }
    }

    /**
     * Check for the "all_robots.txt" file in the local cache.  if it is older than one day, delete it.
     * Then, if there is no local copy, fetch it from github.
     * Then, return the contents of the cache file.
     * @return The contents of the file.
     */
    public static List<String> getAllRobotsFile() {
        if(doesFileExist(ALL_ROBOTS_PATH)) {
            if(isFileMoreThanOneDayOld(ALL_ROBOTS_PATH)) {
                deleteFileFromCache(ALL_ROBOTS_PATH);
            }
        }
        if(!doesFileExist(ALL_ROBOTS_PATH)) {
            writeFileToCache(ALL_ROBOTS_PATH,getAllRobotsFileFromGithub("MarginallyClever/RobotOverlordArms"));
        }
        return getFileFromCache(ALL_ROBOTS_PATH);
    }

    public static void deleteFileFromCache(String path) {
        File f = new File(path);
        logger.info("Deleting cache file "+path);
        if(!f.delete()) {
            logger.error("Cache file could not be deleted");
        }
    }

    public static boolean doesFileExist(String path) {
        File f = new File(path);
        return f.exists();
    }

    private static boolean isFileMoreThanOneDayOld(String path) {
        File f = new File(path);
        if(f.exists()) {
            // check if file is older than 1 day
            long currentDate = new Date().getTime();
            return currentDate - f.lastModified() > 86400000;
        }
        return false;
    }

    /**
     * Cache the contents of the "all_robots.txt" file.
     * @param values The contents of the file.
     */
    private static void writeFileToCache(String path,List<String> values) {
        logger.info("Writing cache file "+path);
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path), StandardCharsets.UTF_8)) {
            for (String value : values) {
                writer.write(value);
                writer.newLine();
            }
        } catch (IOException e) {
            logger.error("Error while writing cache file: "+e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the contents of the "all_robots.txt" file from the local cache.
     * @return The contents of the file.
     */
    private static List<String> getFileFromCache(String path) {
        logger.info("Fetching cache file "+path);
        List<String> results = new ArrayList<>();

        try(BufferedReader reader = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                results.add(line);
            }
        } catch( IOException ex ) {
            logger.error("Error while reading cache file: "+ex.getMessage());
            ex.printStackTrace();
        }
        return results;
    }

    /**
     * Get the contents of the "all_robots.txt" file from github.
     * @param repo The name of the repository to fetch from.
     * @return The contents of the file.
     */
    private static List<String> getAllRobotsFileFromGithub(String repo) {
        logger.info("Fetching " + ALL_ROBOTS_TXT + " file from github");
        List<String> results = new ArrayList<>();

        try {
            URL url = new URL(GITHUB_API_BASE_URL + "/" + repo + "/contents/"+ALL_ROBOTS_TXT);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.VERSION.raw");

            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Failed: HTTP error code: " + connection.getResponseCode());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                results.add(line);
            }

            reader.close();
            connection.disconnect();
        } catch( IOException ex ) {
            logger.error("Error while reading robots.txt file from github:"+ex.getMessage());
            ex.printStackTrace();
        }
        return results;
    }

    private static String decodeBase64(String base64String) {
        String cleanBase64String = base64String.replaceAll("\\s", "");
        byte[] decodedBytes = Base64.getDecoder().decode(cleanBase64String);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
