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

package org.onap.dcaegen2.collectors.datafile.service;

import static org.mockito.Mockito.spy;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onap.dcaegen2.collectors.datafile.exceptions.DmaapNotFoundException;
import org.onap.dcaegen2.collectors.datafile.model.FileData;
import org.onap.dcaegen2.collectors.datafile.model.ImmutableFileData;
import org.onap.dcaegen2.collectors.datafile.utils.JsonMessage;
import org.onap.dcaegen2.collectors.datafile.utils.JsonMessage.AdditionalField;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/8/18
 * @author <a href="mailto:henrik.b.andersson@est.tech">Henrik Andersson</a>
 */
class DmaapConsumerJsonParserTest {
    private static final String PRODUCT_NAME = "NrRadio";
    private static final String VENDOR_NAME = "Ericsson";
    private static final String LAST_EPOCH_MICROSEC = "1519837825682";
    private static final String SOURCE_NAME = "5GRAN_DU";
    private static final String START_EPOCH_MICROSEC = "1519837825682";
    private static final String TIME_ZONE_OFFSET = "UTC+05:00";
    private static final String PM_FILE_NAME = "A20161224.1030-1045.bin.gz";
    private static final String LOCATION = "ftpes://192.168.0.101:22/ftp/rop/" + PM_FILE_NAME;
    private static final String GZIP_COMPRESSION = "gzip";
    private static final String FILE_FORMAT_TYPE = "org.3GPP.32.435#measCollec";
    private static final String FILE_FORMAT_VERSION = "V10";
    private static final String CHANGE_IDENTIFIER = "PM_MEAS_FILES";
    private static final String INCORRECT_CHANGE_IDENTIFIER = "INCORRECT_PM_MEAS_FILES";
    private static final String CHANGE_TYPE = "FileReady";
    private static final String INCORRECT_CHANGE_TYPE = "IncorrectFileReady";
    private static final String NOTIFICATION_FIELDS_VERSION = "1.0";

    @Test
    void whenPassingCorrectJson_validationNotThrowingAnException() throws DmaapNotFoundException {
        // @formatter:off
        AdditionalField additionalField = new JsonMessage.AdditionalFieldBuilder()
                .name(PM_FILE_NAME)
                .location(LOCATION)
                .compression(GZIP_COMPRESSION)
                .fileFormatType(FILE_FORMAT_TYPE)
                .fileFormatVersion(FILE_FORMAT_VERSION)
                .build();
        JsonMessage message = new JsonMessage.JsonMessageBuilder()
                .changeIdentifier(CHANGE_IDENTIFIER)
                .changeType(CHANGE_TYPE)
                .notificationFieldsVersion(NOTIFICATION_FIELDS_VERSION)
                .addAdditionalField(additionalField)
                .build();

        FileData expectedFileData = ImmutableFileData.builder()
                .productName(PRODUCT_NAME)
                .vendorName(VENDOR_NAME)
                .lastEpochMicrosec(LAST_EPOCH_MICROSEC)
                .sourceName(SOURCE_NAME)
                .startEpochMicrosec(START_EPOCH_MICROSEC)
                .timeZoneOffset(TIME_ZONE_OFFSET)
                .changeIdentifier(CHANGE_IDENTIFIER)
                .changeType(CHANGE_TYPE)
                .name(PM_FILE_NAME)
                .location(LOCATION)
                .compression(GZIP_COMPRESSION)
                .fileFormatType(FILE_FORMAT_TYPE)
                .fileFormatVersion(FILE_FORMAT_VERSION)
                .build();
        // @formatter:on
        String messageString = message.toString();
        String parsedString = message.getParsed();
        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsedString);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject())).when(dmaapConsumerJsonParser)
                .getJsonObjectFromAnArray(jsonElement);

        StepVerifier.create(dmaapConsumerJsonParser.getJsonObject(Mono.just(messageString))).expectSubscription()
                .expectNext(expectedFileData).verifyComplete();
    }

    @Test
    void whenPassingCorrectJsonWithoutName_noFileData() {
        // @formatter:off
        AdditionalField additionalField = new JsonMessage.AdditionalFieldBuilder()
                .location(LOCATION)
                .compression(GZIP_COMPRESSION)
                .fileFormatType(FILE_FORMAT_TYPE)
                .fileFormatVersion(FILE_FORMAT_VERSION)
                .build();
        JsonMessage message = new JsonMessage.JsonMessageBuilder()
                .changeIdentifier(CHANGE_IDENTIFIER)
                .changeType(CHANGE_TYPE)
                .notificationFieldsVersion(NOTIFICATION_FIELDS_VERSION)
                .addAdditionalField(additionalField)
                .build();
        // @formatter:on
        String messageString = message.toString();
        String parsedString = message.getParsed();
        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsedString);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject())).when(dmaapConsumerJsonParser)
                .getJsonObjectFromAnArray(jsonElement);

        StepVerifier.create(dmaapConsumerJsonParser.getJsonObject(Mono.just(messageString))).expectSubscription()
                .expectNextCount(0).verifyComplete();
    }

    @Test
    void whenPassingCorrectJsonWithoutLocation_noFileData() {
        // @formatter:off
        AdditionalField additionalField = new JsonMessage.AdditionalFieldBuilder()
                .name(PM_FILE_NAME)
                .compression(GZIP_COMPRESSION)
                .fileFormatType(FILE_FORMAT_TYPE)
                .fileFormatVersion(FILE_FORMAT_VERSION)
                .build();
        JsonMessage message = new JsonMessage.JsonMessageBuilder()
                .changeIdentifier(CHANGE_IDENTIFIER)
                .changeType(CHANGE_TYPE)
                .notificationFieldsVersion(NOTIFICATION_FIELDS_VERSION)
                .addAdditionalField(additionalField)
                .build();
        // @formatter:on
        String messageString = message.toString();
        String parsedString = message.getParsed();
        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsedString);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject())).when(dmaapConsumerJsonParser)
                .getJsonObjectFromAnArray(jsonElement);

        StepVerifier.create(dmaapConsumerJsonParser.getJsonObject(Mono.just(messageString))).expectSubscription()
                .expectNextCount(0).verifyComplete();
    }

    @Test
    void whenPassingCorrectJsonWithoutCompression_noFileData() {
        // @formatter:off
        AdditionalField additionalField = new JsonMessage.AdditionalFieldBuilder()
                .name(PM_FILE_NAME)
                .location(LOCATION)
                .fileFormatType(FILE_FORMAT_TYPE)
                .fileFormatVersion(FILE_FORMAT_VERSION)
                .build();
        JsonMessage message = new JsonMessage.JsonMessageBuilder()
                .changeIdentifier(CHANGE_IDENTIFIER)
                .changeType(CHANGE_TYPE)
                .notificationFieldsVersion(NOTIFICATION_FIELDS_VERSION)
                .addAdditionalField(additionalField)
                .build();
        // @formatter:on
        String messageString = message.toString();
        String parsedString = message.getParsed();
        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsedString);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject())).when(dmaapConsumerJsonParser)
                .getJsonObjectFromAnArray(jsonElement);

        StepVerifier.create(dmaapConsumerJsonParser.getJsonObject(Mono.just(messageString))).expectSubscription()
                .expectNextCount(0).verifyComplete();
    }

    @Test
    void whenPassingCorrectJsonWithoutFileFormatType_noFileData() {
        // @formatter:off
        AdditionalField additionalField = new JsonMessage.AdditionalFieldBuilder()
                .name(PM_FILE_NAME)
                .location(LOCATION)
                .compression(GZIP_COMPRESSION)
                .fileFormatVersion(FILE_FORMAT_VERSION)
                .build();
        JsonMessage message = new JsonMessage.JsonMessageBuilder()
                .changeIdentifier(CHANGE_IDENTIFIER)
                .changeType(CHANGE_TYPE)
                .notificationFieldsVersion(NOTIFICATION_FIELDS_VERSION)
                .addAdditionalField(additionalField)
                .build();
        // @formatter:on
        String messageString = message.toString();
        String parsedString = message.getParsed();
        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsedString);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject())).when(dmaapConsumerJsonParser)
                .getJsonObjectFromAnArray(jsonElement);

        StepVerifier.create(dmaapConsumerJsonParser.getJsonObject(Mono.just(messageString))).expectSubscription()
                .expectNextCount(0).verifyComplete();
    }

    @Test
    void whenPassingOneCorrectJsonWithoutFileFormatVersionAndOneCorrect_oneFileData() {
        // @formatter:off
        AdditionalField additionalFaultyField = new JsonMessage.AdditionalFieldBuilder()
                .name(PM_FILE_NAME)
                .location(LOCATION)
                .compression(GZIP_COMPRESSION)
                .fileFormatType(FILE_FORMAT_TYPE)
                .build();
        AdditionalField additionalField = new JsonMessage.AdditionalFieldBuilder()
                .name(PM_FILE_NAME)
                .location(LOCATION)
                .compression(GZIP_COMPRESSION)
                .fileFormatType(FILE_FORMAT_TYPE)
                .fileFormatVersion(FILE_FORMAT_VERSION)
                .build();
        JsonMessage message = new JsonMessage.JsonMessageBuilder()
                .changeIdentifier(CHANGE_IDENTIFIER)
                .changeType(CHANGE_TYPE)
                .notificationFieldsVersion(NOTIFICATION_FIELDS_VERSION)
                .addAdditionalField(additionalFaultyField)
                .addAdditionalField(additionalField)
                .build();

        FileData expectedFileData = ImmutableFileData.builder()
                .productName(PRODUCT_NAME)
                .vendorName(VENDOR_NAME)
                .lastEpochMicrosec(LAST_EPOCH_MICROSEC)
                .sourceName(SOURCE_NAME)
                .startEpochMicrosec(START_EPOCH_MICROSEC)
                .timeZoneOffset(TIME_ZONE_OFFSET)
                .changeIdentifier(CHANGE_IDENTIFIER)
                .changeType(CHANGE_TYPE)
                .name(PM_FILE_NAME)
                .location(LOCATION)
                .compression(GZIP_COMPRESSION)
                .fileFormatType(FILE_FORMAT_TYPE)
                .fileFormatVersion(FILE_FORMAT_VERSION)
                .build();
        // @formatter:on
        String messageString = message.toString();
        String parsedString = message.getParsed();
        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsedString);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject())).when(dmaapConsumerJsonParser)
                .getJsonObjectFromAnArray(jsonElement);

        StepVerifier.create(dmaapConsumerJsonParser.getJsonObject(Mono.just(messageString))).expectSubscription()
                .expectNext(expectedFileData).verifyComplete();
    }

    @Test
    void whenPassingJsonWithoutMandatoryHeaderInformation_validationThrowingAnException() {
        // @formatter:off
        JsonMessage message = new JsonMessage.JsonMessageBuilder()
                .changeIdentifier("PM_MEAS_FILES_INVALID")
                .changeType("FileReady_INVALID")
                .notificationFieldsVersion("1.0_INVALID")
                .build();
        // @formatter:on
        String incorrectMessageString = message.toString();
        String parsedString = message.getParsed();
        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsedString);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject())).when(dmaapConsumerJsonParser)
                .getJsonObjectFromAnArray(jsonElement);

        StepVerifier.create(dmaapConsumerJsonParser.getJsonObject(Mono.just(incorrectMessageString)))
                .expectSubscription().expectError(DmaapNotFoundException.class).verify();
    }

    @Test
    void whenPassingJsonWithNullJsonElement_validationThrowingAnException() {
        JsonMessage message = new JsonMessage.JsonMessageBuilder().build();

        String incorrectMessageString = message.toString();
        String parsedString = message.getParsed();
        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsedString);

        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject())).when(dmaapConsumerJsonParser)
                .getJsonObjectFromAnArray(jsonElement);

        StepVerifier.create(dmaapConsumerJsonParser.getJsonObject(Mono.just(incorrectMessageString)))
                .expectSubscription().expectError(DmaapNotFoundException.class).verify();
    }

    @Test
    void whenPassingCorrectJsonWithIncorrectChangeType_validationThrowingAnException() {
        // @formatter:off
        AdditionalField additionalField = new JsonMessage.AdditionalFieldBuilder()
                .name(PM_FILE_NAME)
                .location(LOCATION)
                .compression(GZIP_COMPRESSION)
                .fileFormatVersion(FILE_FORMAT_VERSION)
                .build();
        JsonMessage message = new JsonMessage.JsonMessageBuilder()
                .changeIdentifier(CHANGE_IDENTIFIER)
                .changeType(INCORRECT_CHANGE_TYPE)
                .notificationFieldsVersion(NOTIFICATION_FIELDS_VERSION)
                .addAdditionalField(additionalField)
                .build();
        // @formatter:on
        String messageString = message.toString();
        String parsedString = message.getParsed();
        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsedString);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject())).when(dmaapConsumerJsonParser)
                .getJsonObjectFromAnArray(jsonElement);

        StepVerifier.create(dmaapConsumerJsonParser.getJsonObject(Mono.just(messageString))).expectSubscription()
                .expectNextCount(0).expectError(DmaapNotFoundException.class).verify();
    }

    @Test
    void whenPassingCorrectJsonWithIncorrectChangeIdentifier_validationThrowingAnException() {
        // @formatter:off
        AdditionalField additionalField = new JsonMessage.AdditionalFieldBuilder()
                .name(PM_FILE_NAME)
                .location(LOCATION)
                .compression(GZIP_COMPRESSION)
                .fileFormatVersion(FILE_FORMAT_VERSION)
                .build();
        JsonMessage message = new JsonMessage.JsonMessageBuilder()
                .changeIdentifier(INCORRECT_CHANGE_IDENTIFIER)
                .changeType(CHANGE_TYPE)
                .notificationFieldsVersion(NOTIFICATION_FIELDS_VERSION)
                .addAdditionalField(additionalField)
                .build();
        // @formatter:on
        String messageString = message.toString();
        String parsedString = message.getParsed();
        DmaapConsumerJsonParser dmaapConsumerJsonParser = spy(new DmaapConsumerJsonParser());
        JsonElement jsonElement = new JsonParser().parse(parsedString);
        Mockito.doReturn(Optional.of(jsonElement.getAsJsonObject())).when(dmaapConsumerJsonParser)
                .getJsonObjectFromAnArray(jsonElement);

        StepVerifier.create(dmaapConsumerJsonParser.getJsonObject(Mono.just(messageString))).expectSubscription()
                .expectNextCount(0).expectError(DmaapNotFoundException.class).verify();
    }
}
