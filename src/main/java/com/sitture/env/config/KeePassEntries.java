package com.sitture.env.config;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.KeePassFile;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class KeePassEntries {



    public static void main(String[] args) {
        new KeePassEntries().getEntriesConfiguration("europa-e2e", "default");
    }

    private static Configuration entriesConfiguration;
    private static KeePassFile keePassFile;

    KeePassEntries() {
        keePassFile = KeePassDatabase.getInstance(getFileFromResources("testme.kdbx")).openDatabase("testme");
        entriesConfiguration = new MapConfiguration(getEntriesMap());
    }

    Configuration getEntriesConfiguration(final String groupName, final String environment) {
        return entriesConfiguration;
    }

    private File getFileFromResources(String fileName) {

        ClassLoader classLoader = getClass().getClassLoader();

        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return new File(resource.getFile());
        }

    }

    private static Map<String, String> getEntriesMap() {
        Map<String, String> envMap = new HashMap<String, String>();
        for (final Map.Entry<String, String> envVar : System.getenv().entrySet()) {
            envMap.put(envVar.getKey(), envVar.getValue());
        }

        List<Group> groups = keePassFile.getTopGroups();
        for (Group group : groups) {
            System.out.println(group.getName());
            if (group.getName().equals("europa-e2e")) {
                List<Group> subGroups = group.getGroups();
                for (Group g : subGroups) {
                    List<Entry> entries = g.getEntries();
                    for (Entry entry : entries) {
                        System.out.println("Title: " + entry.getUsername() + " Password: " + entry.getPassword());
                        envMap.put(getProcessedEnvKey(entry.getUsername()), entry.getPassword());
                    }
                }
            }
        }
        return envMap;
    }

    private static String getProcessedEnvKey(final String envVar) {
        return envVar.replaceAll("_", ".").toLowerCase();
    }

}
