package com.github.sitture.envconfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.jackson.JacksonDatabase;
import org.linguafranca.pwdb.kdbx.jackson.JacksonEntry;
import org.linguafranca.pwdb.kdbx.jackson.JacksonGroup;
import org.linguafranca.pwdb.kdbx.jackson.JacksonIcon;

class KeepassConfiguration {

    private static final String KEEPASS_DB_FILE_EXTENSION = ".kdbx";
    private final Database<JacksonDatabase, JacksonGroup, JacksonEntry, JacksonIcon> database;

    KeepassConfiguration(final EnvConfigKeepassProperties keepassProperties) {
        final String groupName = keepassProperties.getFilename();
        final String keePassGroupName = null != groupName && groupName.endsWith(KEEPASS_DB_FILE_EXTENSION)
            ? groupName.split(KEEPASS_DB_FILE_EXTENSION)[0]
            : groupName;
        try {
            database = JacksonDatabase.load(new KdbxCreds(keepassProperties.getMasterKey().getBytes(StandardCharsets.UTF_8)),
                getKeepassDatabase(keePassGroupName.concat(KEEPASS_DB_FILE_EXTENSION)));
        } catch (IOException e) {
            throw new EnvConfigException("Error opening database!", e);
        }
    }

    private static String getProcessedPropertyKey(final String envVar) {
        return envVar.replaceAll("_", ".").toLowerCase();
    }

    public Configuration getConfiguration(final String env) {
        final String keePassGroupName = !database.getRootGroup().getGroups().isEmpty()
            ? database.getRootGroup().getGroups().get(0).getName()
            : "Root";
        return new MapConfiguration(getEntriesMap(keePassGroupName, env));
    }

    private InputStream getKeepassDatabase(final String fileName) {
        final InputStream resource = ClassLoader.getSystemResourceAsStream(fileName);
        if (null == resource) {
            throw new EnvConfigException(String.format("Database %s does not exist!", fileName));
        }
        return resource;
    }

    private Map<String, String> getEntriesMap(final String groupName, final String env) {
        final Optional<JacksonGroup> projectGroup = database.getRootGroup().getGroups().stream()
            .filter(group -> group.getName().trim().equals(groupName)).findFirst();
        if (projectGroup.isEmpty()) {
            throw new IllegalArgumentException(String.format("Group %s not found in the database!", groupName));
        }
        final Optional<JacksonGroup> envGroup = projectGroup.get().getGroups().stream().filter(group -> group.getName().trim().equals(env)).findFirst();
        final Map<String, String> entriesMap = new HashMap<>();
        envGroup.ifPresent(group -> group.getEntries()
            .forEach(entry -> {
                entriesMap.put(entry.getTitle().trim(), entry.getPassword());
                entriesMap.put(getProcessedPropertyKey(entry.getTitle().trim()), entry.getPassword());
            }));
        return entriesMap;
    }

}
