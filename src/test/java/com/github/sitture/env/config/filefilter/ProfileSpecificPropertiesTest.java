package com.github.sitture.env.config.filefilter;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ProfileSpecificPropertiesTest {

    private static final String MATCH_NOT_FOUND = "Match not found when one was expected";
    private static final String MATCH_FOUND = "Match found when one was not expected";

    @Before
    public void clearProperties() {
        System.setProperty("CONFIG_ENV_PROFILE", "prof1");
    }

    @Test
    public void testAccept() {
        assertTrue(MATCH_NOT_FOUND, invoke("/some/path/to/someenv-prof1.properties"));
    }

    @Test
    public void testAcceptNonMatchingProfile() {
        assertFalse(MATCH_FOUND, invoke("/some/path/to/someenv-prof2.properties"));
    }

    @Test
    public void testAcceptEnvPropertiesFile() {
        assertTrue(MATCH_NOT_FOUND, invoke("/some/path/to/someenv.properties"));
    }

    @Test
    public void testAcceptDashButNoProfile() {
        assertFalse(MATCH_FOUND, invoke("/some/path/to/someenv-.properties"));
    }

    @Test
    public void testAcceptProfileThenDashButNoProfile() {
        assertFalse(MATCH_FOUND, invoke("/some/path/to/prof1-.properties"));
    }

    @Test
    public void testAcceptNonMatchingProfileThenDashButNoProfile() {
        assertFalse(MATCH_FOUND, invoke("/some/path/to/prof2-.properties"));
    }

    @Test
    public void testAcceptDashAndThenProfile() {
        assertTrue(MATCH_NOT_FOUND, invoke("/some/path/to/-prof1.properties"));
    }

    @Test
    public void testAcceptDashAndThenNonMatchingProfile() {
        assertFalse(MATCH_FOUND, invoke("/some/path/to/-prof2.properties"));
    }

    @Test
    public void testAcceptNoDashNonMatchingProfile() {
        assertTrue(MATCH_NOT_FOUND, invoke("/some/path/to/someenvprof1.properties"));
    }

    @Test
    public void testAcceptNoDashMatchingProfile() {
        assertTrue(MATCH_NOT_FOUND, invoke("/some/path/to/someenvprof2.properties"));
    }

    @Test
    public void testAcceptNoProfileProperty() {
        System.clearProperty("CONFIG_ENV_PROFILE");
        assertFalse(MATCH_FOUND, invoke("/some/path/to/someenv-prof1.properties"));
    }

    private boolean invoke(final String filePath) {
        return new ProfileSpecificProperties().accept(new File(filePath));
    }
}