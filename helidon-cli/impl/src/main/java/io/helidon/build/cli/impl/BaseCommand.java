/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.helidon.build.cli.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import static io.helidon.build.cli.impl.ProjectConfig.DOT_HELIDON;

/**
 * Class BaseCommand.
 */
public abstract class BaseCommand {

    static final String HELIDON_PROPERTIES = "/helidon.properties";

    private Properties cliConfig;
    private ProjectConfig projectConfig;
    private Path projectDir;

    protected ProjectConfig projectConfig(Path dir) {
        if (projectConfig != null && dir.equals(projectDir)) {
            return projectConfig;
        }
        File dotHelidon = dir.resolve(DOT_HELIDON).toFile();
        projectConfig = new ProjectConfig(dotHelidon);
        projectDir = dir;
        return projectConfig;
    }

    protected Properties cliConfig() {
        if (cliConfig != null) {
            return cliConfig;
        }
        try {
            InputStream sourceStream = getClass().getResourceAsStream(HELIDON_PROPERTIES);
            try (InputStreamReader isr = new InputStreamReader(sourceStream)) {
                cliConfig = new Properties();
                cliConfig.load(isr);
                return cliConfig;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Model readPomModel(File pomFile) {
        try {
            try (FileReader fr = new FileReader(pomFile)) {
                MavenXpp3Reader mvnReader = new MavenXpp3Reader();
                return mvnReader.read(fr);
            }
        } catch (IOException | XmlPullParserException e) {
            throw new RuntimeException(e);
        }
    }

    protected void writePomModel(File pomFile, Model model) {
        try {
            try (FileWriter fw = new FileWriter(pomFile)) {
                MavenXpp3Writer mvnWriter = new MavenXpp3Writer();
                mvnWriter.write(fw, model);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}