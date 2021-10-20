package com.github.sitture.env.config;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.KeePassFile;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class KeePassEntries {

    private final KeePassFile keePassFile;
    private static final String KEEPASS_DB_FILE_EXTENSION = ".kdbx";

    KeePassEntries(final String masterKey, final String groupName) {
        final String keePassGroupName = null != groupName && groupName.endsWith(KEEPASS_DB_FILE_EXTENSION)
                ? groupName.split(KEEPASS_DB_FILE_EXTENSION)[0]
                : groupName;
        keePassFile = KeePassDatabase.getInstance(getKeepassDatabaseFile(keePassGroupName.concat(KEEPASS_DB_FILE_EXTENSION))).openDatabase(masterKey);
    }

    protected Configuration getEntriesConfiguration(final String env) {
        final String keePassGroupName = keePassFile.getTopGroups().get(0).getName();
        return new MapConfiguration(getEntriesMap(keePassGroupName, env));
    }

    private File getKeepassDatabaseFile(final String fileName) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException(String.format("Database %s does not exist!", fileName));
        } else {
            return new File(resource.getFile());
        }
    }

    private Map<String, String> getEntriesMap(final String groupName, final String env) {
        final Optional<Group> projectGroup = keePassFile.getTopGroups().stream()
                .filter(group -> group.getName().trim().equals(groupName)).findFirst();
        if (!projectGroup.isPresent()) {
            throw new IllegalArgumentException(String.format("Group %s not found in the database!", groupName));
        }
        final Optional<Group> envGroup = projectGroup.get().getGroups().stream().filter(group -> group.getName().trim().equals(env)).findFirst();
        final Map<String, String> entriesMap = new HashMap<>();
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
