package com.github.sitture.envconfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.MapConfiguration;
import org.linguafranca.pwdb.Database;
import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.jackson.JacksonDatabase;
import org.linguafranca.pwdb.kdbx.jackson.JacksonEntry;
import org.linguafranca.pwdb.kdbx.jackson.JacksonGroup;
import org.linguafranca.pwdb.kdbx.jackson.JacksonIcon;

class EnvConfigKeepassConfiguration implements EnvConfigConfiguration {

    private static final String KEEPASS_DB_FILE_EXTENSION = ".kdbx";
    private final Database<JacksonDatabase, JacksonGroup, JacksonEntry, JacksonIcon> database;
    private final List<String> environments;

    EnvConfigKeepassConfiguration(final EnvConfigProperties configProperties) {
        this(configProperties.getKeepassProperties(), configProperties.getEnvironments());
    }

    private EnvConfigKeepassConfiguration(final EnvConfigKeepassProperties keepassProperties,
                                          final List<String> environments) {
        this.environments = environments;
        final String groupName = keepassProperties.filename();
        if (null == groupName || groupName.isBlank()) {
            throw new EnvConfigException("Keepass filename must not be null or blank");
        }
        final String databaseFileName = groupName.endsWith(KEEPASS_DB_FILE_EXTENSION)
            ? groupName
            : groupName.concat(KEEPASS_DB_FILE_EXTENSION);
        try {
            database = JacksonDatabase.load(new KdbxCreds(keepassProperties.masterKey().getBytes(StandardCharsets.UTF_8)),
                getKeepassDatabase(databaseFileName));
        } catch (IOException e) {
            throw new EnvConfigException("Error opening database!", e);
        }
    }

    @Override
    public Map<String, Configuration> getConfiguration() {
        return this.environments.stream()
            .collect(Collectors.toMap(Function.identity(), this::getConfigurationForEnvironment));
    }

    private Configuration getConfigurationForEnvironment(final String env) {
        final String keePassGroupName = !database.getRootGroup().getGroups().isEmpty()
            ? database.getRootGroup().getGroups().get(0).getName()
            : "Root";
        return new MapConfiguration(getKeepassEntriesMap(keePassGroupName, env));
    }

    private InputStream getKeepassDatabase(final String fileName) {
        final InputStream resource = ClassLoader.getSystemResourceAsStream(fileName);
        if (null == resource) {
            throw new EnvConfigException("Database %s does not exist!".formatted(fileName));
        }
        return resource;
    }

    private Map<String, String> getKeepassEntriesMap(final String groupName, final String env) {
        final Optional<JacksonGroup> projectGroup = database.getRootGroup().getGroups().stream()
            .filter(group -> group.getName().trim().equals(groupName)).findFirst();
        if (projectGroup.isEmpty()) {
            throw new IllegalArgumentException("Group %s not found in the database!".formatted(groupName));
        }
        final Optional<JacksonGroup> envGroup = projectGroup.get().getGroups().stream().filter(group -> group.getName().trim().equals(env)).findFirst();
        final Map<String, String> entriesMap = new HashMap<>();
        envGroup.ifPresent(group -> group.getEntries()
            .forEach(entry -> {
                entriesMap.put(entry.getTitle().trim(), entry.getPassword());
                entriesMap.put(EnvConfigUtils.getProcessedPropertyKey(entry.getTitle().trim()), entry.getPassword());
            }));
        return entriesMap;
    }

}
