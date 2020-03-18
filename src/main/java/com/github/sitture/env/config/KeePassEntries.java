package com.github.sitture.env.config;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.KeePassFile;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;

class KeePassEntries {

    private static Configuration entriesConfiguration;
    private static KeePassFile keePassFile;
    private static final String KEEPASS_DB_FILE_EXTENSION = ".kdbx";

    KeePassEntries(final String masterKey, String groupName, final String env) {
        if (null != groupName && groupName.endsWith(KEEPASS_DB_FILE_EXTENSION)) {
            groupName = groupName.split(KEEPASS_DB_FILE_EXTENSION)[0];
        }
        keePassFile = KeePassDatabase.getInstance(getKeepassDatabaseFile(groupName.concat(".kdbx"))).openDatabase(masterKey);
        entriesConfiguration = new MapConfiguration(getEntriesMap(groupName, env));
    }

    Configuration getEntriesConfiguration() {
        return entriesConfiguration;
    }

    private File getKeepassDatabaseFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException(String.format("Database %s does not exist!", fileName));
        } else {
            return new File(resource.getFile());
        }
    }

    private static Map<String, String> getEntriesMap(final String groupName, final String env) {
        Map<String, String> entriesMap = new HashMap<String, String>();
        Optional<Group> projectGroup = keePassFile.getTopGroups().stream()
                .filter(group -> group.getName().trim().equals(groupName)).findFirst();
        if (!projectGroup.isPresent()) {
            throw new IllegalArgumentException(String.format("Group %s not found in the database!", groupName));
        }
        Optional<Group> envGroup = projectGroup.get().getGroups().stream().filter(group -> group.getName().trim().equals(env)).findFirst();
        envGroup.ifPresent(group -> group.getEntries()
                .forEach(entry -> {
                    entriesMap.put(entry.getTitle().trim(), entry.getPassword());
                    entriesMap.put(getProcessedEnvKey(entry.getTitle().trim()), entry.getPassword());
                }));
        return entriesMap;
    }

    private static String getProcessedEnvKey(final String envVar) {
        return envVar.replaceAll("_", ".").toLowerCase();
    }

}
