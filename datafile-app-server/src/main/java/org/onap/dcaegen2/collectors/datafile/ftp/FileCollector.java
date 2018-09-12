/*-
 * ============LICENSE_START======================================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
 * ===============================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END========================================================================
 */

package org.onap.dcaegen2.collectors.datafile.ftp;

import java.net.URI;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.onap.dcaegen2.collectors.datafile.model.ConsumerDmaapModel;
import org.onap.dcaegen2.collectors.datafile.model.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.collectors.datafile.service.FileData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

/**
 * @author <a href="mailto:henrik.b.andersson@est.tech">Henrik Andersson</a>
 *
 */
@Component
public class FileCollector { // TODO: Should be final, but that means adding PowerMock or Mockito
                             // 2.x for testing so it is left for later improvement.
    private static final String FTPES = "ftpes";
    private static final String FTPS = "ftps";
    private static final String SFTP = "sftp";

    private static final Logger logger = LoggerFactory.getLogger(FileCollector.class);

    private final FtpsClient ftpsClient;
    private final SftpClient sftpClient;
    private String keyCertPath;
    private String keyCertPassword;
    private String trustedCAPath;
    private String trustedCAPassword;

    @Autowired
    protected FileCollector(FtpsClient ftpsClient, SftpClient sftpClient) {
        this.ftpsClient = ftpsClient;
        this.sftpClient = sftpClient;
        this.keyCertPath = "";
        this.keyCertPassword = "";
        this.trustedCAPath = "";
        this.trustedCAPassword = "";
    }

    protected FileCollector(FtpsClient ftpsClient, SftpClient sftpClient, String keyCert, String keyCertPassword,
            String trustedCA, String trustedCAPassword) {
        this.ftpsClient = ftpsClient;
        this.sftpClient = sftpClient;
        this.keyCertPath = keyCert;
        this.keyCertPassword = keyCertPassword;
        this.trustedCAPath = trustedCA;
        this.trustedCAPassword = trustedCAPassword;
    }

    public String getKeyCertPath() {
        return keyCertPath;
    }

    public void setKeyCertPath(String keyCertPath) {
        this.keyCertPath = keyCertPath;
    }

    public String getKeyCertPassword() {
        return keyCertPassword;
    }

    public void setKeyCertPassword(String keyCertPassword) {
        this.keyCertPassword = keyCertPassword;
    }

    public String getTrustedCAPath() {
        return trustedCAPath;
    }

    public void setTrustedCAPath(String trustedCAPath) {
        this.trustedCAPath = trustedCAPath;
    }

    public String getTrustedCAPassword() {
        return trustedCAPassword;
    }

    public void setTrustedCAPassword(String trustedCAPassword) {
        this.trustedCAPassword = trustedCAPassword;
    }

    public Mono<ArrayList<ConsumerDmaapModel>> getFilesFromSender(ArrayList<FileData> listOfFileData) {
        ArrayList<ConsumerDmaapModel> consumerModels = new ArrayList<ConsumerDmaapModel>();
        for (FileData fileData : listOfFileData) {
            String localFile = null;
            localFile = collectFile(fileData);

            if (localFile != null) {
                ConsumerDmaapModel consumerDmaapModel = getConsumerDmaapModel(fileData, localFile);
                consumerModels.add(consumerDmaapModel);
            }
        }
        return Mono.just(consumerModels);
    }

    private String collectFile(FileData fileData) {
        String location = fileData.location();
        URI uri = URI.create(location);
        String[] userInfo = getUserNameAndPasswordIfGiven(uri.getUserInfo());
        FileServerData fileServerData = ImmutableFileServerData.builder().serverAddress(uri.getHost())
                .userId(userInfo != null ? userInfo[0] : "").password(userInfo != null ? userInfo[1] : "")
                .port(uri.getPort()).ftpKeyPath(this.getKeyCertPath()).ftpKeyPassword(this.getKeyCertPassword())
                .trustedCAPath(this.getTrustedCAPath()).trustedCAPassword(this.getTrustedCAPassword()).build();
        String remoteFile = uri.getPath();
        String localFile = "target/" + FilenameUtils.getName(remoteFile);
        String scheme = uri.getScheme();

        // TODO: Refactor for better error handling. Will be done as an improvement after first
        // version committed.
        if (FTPES.equals(scheme) || FTPS.equals(scheme)) {
            ftpsClient.collectFile(fileServerData, remoteFile, localFile);
        } else if (SFTP.equals(scheme)) {
            sftpClient.collectFile(fileServerData, remoteFile, localFile);
        } else {
            logger.trace("DFC does not support protocol {}. Supported protocols are " + FTPES + ", " + FTPS + ", and "
                    + SFTP + ".", scheme);
            localFile = null;
        }
        return localFile;
    }

    private String[] getUserNameAndPasswordIfGiven(String userInfoString) {
        String[] userInfo = null;
        if (userInfoString != null && !userInfoString.isEmpty()) {
            userInfo = userInfoString.split(":");
        }
        return userInfo;
    }

    private ConsumerDmaapModel getConsumerDmaapModel(FileData fileData, String localFile) {
        String compression = fileData.compression();
        String fileFormatType = fileData.fileFormatType();
        String fileFormatVersion = fileData.fileFormatVersion();

        return ImmutableConsumerDmaapModel.builder().location(localFile).compression(compression)
                .fileFormatType(fileFormatType).fileFormatVersion(fileFormatVersion).build();
    }
}
