package com.redalert.backend.infrastructure.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

/**
 * Google OAuth2 Configuration for Gmail and Calendar APIs.
 * Implements OAuth2 flow for Desktop/Installed Application.
 * 
 * This configuration:
 * - Reads credentials.json from resources
 * - Opens browser for first-time authentication
 * - Stores tokens locally for subsequent runs
 * - Provides ready-to-use Gmail and Calendar service beans
 */
@Configuration
@Slf4j
public class GoogleConfig {

        private static final String APPLICATION_NAME = "Red Alert Email Monitor";
        private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

        @Value("${google.credentials.file-path}")
        private String credentialsFilePath;

        @Value("${google.tokens.directory}")
        private String tokensDirectoryPath;

        /**
         * Scopes required for Gmail operations.
         * GMAIL_READONLY: Read emails
         * GMAIL_MODIFY: Modify labels (remove UNREAD)
         */
        private static final List<String> GMAIL_SCOPES = Arrays.asList(
                        GmailScopes.GMAIL_READONLY,
                        GmailScopes.GMAIL_MODIFY);

        /**
         * Scopes required for Calendar operations.
         */
        private static final List<String> CALENDAR_SCOPES = Arrays.asList(
                        CalendarScopes.CALENDAR);

        /**
         * Combined scopes for both Gmail and Calendar.
         */
        private static final List<String> ALL_SCOPES = Arrays.asList(
                        GmailScopes.GMAIL_READONLY,
                        GmailScopes.GMAIL_MODIFY,
                        CalendarScopes.CALENDAR);

        /**
         * Creates and authorizes a Credential object.
         * 
         * @param httpTransport The network HTTP Transport
         * @return An authorized Credential object
         * @throws IOException If credentials file cannot be found
         */
        private Credential getCredentials(final NetHttpTransport httpTransport) throws IOException {
                // Load client secrets from credentials.json
                InputStream in = GoogleConfig.class
                                .getResourceAsStream("/" + credentialsFilePath.replace("classpath:", ""));
                if (in == null) {
                        throw new IOException("Resource not found: " + credentialsFilePath);
                }
                GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

                // Build flow and trigger user authorization request
                GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                                httpTransport, JSON_FACTORY, clientSecrets, ALL_SCOPES)
                                .setDataStoreFactory(new FileDataStoreFactory(new File(tokensDirectoryPath)))
                                .setAccessType("offline")
                                .build();

                LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

                log.info("Initiating OAuth2 authorization flow. Browser will open for authentication.");
                return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        }

        /**
         * Provides Gmail service bean.
         * Lazy initialization to avoid blocking startup with OAuth flow.
         * 
         * @return Configured Gmail service
         * @throws GeneralSecurityException If security configuration fails
         * @throws IOException              If credentials cannot be loaded
         */
        @Bean
        @Lazy
        public Gmail gmail() throws GeneralSecurityException, IOException {
                final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                Credential credential = getCredentials(httpTransport);

                log.info("Gmail service initialized successfully");
                return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                                .setApplicationName(APPLICATION_NAME)
                                .build();
        }

        /**
         * Provides Calendar service bean.
         * Lazy initialization to avoid blocking startup with OAuth flow.
         * 
         * @return Configured Calendar service
         * @throws GeneralSecurityException If security configuration fails
         * @throws IOException              If credentials cannot be loaded
         */
        @Bean
        @Lazy
        public Calendar calendar() throws GeneralSecurityException, IOException {
                final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                Credential credential = getCredentials(httpTransport);

                log.info("Calendar service initialized successfully");
                return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                                .setApplicationName(APPLICATION_NAME)
                                .build();
        }
}
