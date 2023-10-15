package com.marginallyclever.robotoverlord.swing.robotlibrarypanel;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GithubFetcherTest {
    private static String githubUrl = "https://github.com/marginallyclever/Sixi-";
    @Test
    public void testFetchGitHubTags() {
        List<String> tags = GithubFetcher.fetchTags(githubUrl);
        for(String tag : tags) {
            System.out.println(tag);
        }
    }

    @Test
    public void testLoadProperties() {
        try {
            Map<String,String> map = GithubFetcher.fetchRobotProperties(githubUrl);
            String propertyValue = map.get("url");
            System.out.println("Value of url: " + propertyValue);
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }
}
