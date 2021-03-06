/*
 * ============LICENSE_START======================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property, 2018 Nordix Foundation. All rights reserved.
 * ===============================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END========================================================================
 */

package org.onap.dcaegen2.collectors.datafile.configuration;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ServiceLoader;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.onap.dcaegen2.collectors.datafile.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.collectors.datafile.config.DmaapPublisherConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapterFactory;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/9/18
 * @author <a href="mailto:henrik.b.andersson@est.tech">Henrik Andersson</a>
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("app")
public abstract class DatafileAppConfig implements Config {

    private static final String CONFIG = "configs";
    private static final String DMAAP = "dmaap";
    private static final String DMAAP_PRODUCER = "dmaapProducerConfiguration";
    private static final String DMAAP_CONSUMER = "dmaapConsumerConfiguration";
    private static final String FTP = "ftp";
    private static final String FTPES_CONFIGURATION = "ftpesConfiguration";


    private static final Logger logger = LoggerFactory.getLogger(DatafileAppConfig.class);

    DmaapConsumerConfiguration dmaapConsumerConfiguration;

    DmaapPublisherConfiguration dmaapPublisherConfiguration;

    FtpesConfig ftpesConfig;

    @NotEmpty
    private String filepath;


    @Override
    public DmaapConsumerConfiguration getDmaapConsumerConfiguration() {
        return dmaapConsumerConfiguration;
    }

    @Override
    public DmaapPublisherConfiguration getDmaapPublisherConfiguration() {
        return dmaapPublisherConfiguration;
    }

    @Override
    public FtpesConfig getFtpesConfiguration() {
        return ftpesConfig;
    }

    @Override
    public void initFileStreamReader() {

        GsonBuilder gsonBuilder = new GsonBuilder();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(gsonBuilder::registerTypeAdapterFactory);
        JsonParser parser = new JsonParser();
        JsonObject jsonObject;
        try (InputStream inputStream = getInputStream(filepath)) {
            JsonElement rootElement = getJsonElement(parser, inputStream);
            if (rootElement.isJsonObject()) {
                jsonObject = rootElement.getAsJsonObject();
                ftpesConfig = deserializeType(gsonBuilder,
                        jsonObject.getAsJsonObject(CONFIG).getAsJsonObject(FTP).getAsJsonObject(FTPES_CONFIGURATION),
                        FtpesConfig.class);
                dmaapConsumerConfiguration = deserializeType(gsonBuilder,
                        jsonObject.getAsJsonObject(CONFIG).getAsJsonObject(DMAAP).getAsJsonObject(DMAAP_CONSUMER),
                        DmaapConsumerConfiguration.class);

                dmaapPublisherConfiguration = deserializeType(gsonBuilder,
                        jsonObject.getAsJsonObject(CONFIG).getAsJsonObject(DMAAP).getAsJsonObject(DMAAP_PRODUCER),
                        DmaapPublisherConfiguration.class);
            }
        } catch (IOException e) {
            logger.error("Problem with file loading, file: {}", filepath, e);
        } catch (JsonSyntaxException e) {
            logger.error("Problem with Json deserialization", e);
        }
    }

    JsonElement getJsonElement(JsonParser parser, InputStream inputStream) {
        return parser.parse(new InputStreamReader(inputStream));
    }

    private <T> T deserializeType(@NotNull GsonBuilder gsonBuilder, @NotNull JsonObject jsonObject,
            @NotNull Class<T> type) {
        return gsonBuilder.create().fromJson(jsonObject, type);
    }

    InputStream getInputStream(@NotNull String filepath) throws IOException {
        return new BufferedInputStream(new FileInputStream(filepath));
    }

    String getFilepath() {
        return this.filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

}
