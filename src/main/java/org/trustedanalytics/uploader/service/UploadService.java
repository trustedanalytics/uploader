/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.uploader.service;

import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.trustedanalytics.uploader.client.DataAcquisitionClient;
import org.trustedanalytics.uploader.core.stream.consumer.TriConsumer;
import org.trustedanalytics.uploader.rest.UploadException;
import org.trustedanalytics.uploader.rest.Transfer;

import org.apache.hadoop.security.AccessControlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.trustedanalytics.uploader.rest.UploadMetadata;
import org.trustedanalytics.uploader.rest.UploadMetadata.UploadMetadataBuilder;
import org.trustedanalytics.uploader.rest.UploadRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import javax.security.auth.login.LoginException;

@Service
public class UploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadService.class);

    private final Function<InputStream, InputStream> streamDecoder;

    private final TriConsumer<InputStream, Transfer, UUID> streamConsumer;

    private DataAcquisitionClient dataAcquisitionClient;

    private Function<Authentication, String> tokenExtractor;

    @Autowired
    public UploadService(Function<InputStream, InputStream> streamDecoder,
                         TriConsumer<InputStream, Transfer, UUID> streamConsumer, DataAcquisitionClient client,
                         Function<Authentication, String> tokenExtractor) {
        this.streamDecoder = streamDecoder;
        this.streamConsumer = streamConsumer;
        this.dataAcquisitionClient = Objects.requireNonNull(client);
        this.tokenExtractor = Objects.requireNonNull(tokenExtractor);
    }

    public List<Transfer> processUpload(FileItemIterator iterator, UploadRequest request, boolean multipleFiles)
            throws IOException, FileUploadException, UploadException {
        final UploadMetadataBuilder uploadMetadataBuilder = UploadMetadata.builder();
        final List<Transfer> transfers = new ArrayList<>();
        while(iterator.hasNext()) {
            FileItemStream stream = iterator.next();
            if(!stream.isFormField()) {
                resolveFileName(uploadMetadataBuilder, stream, request);
                if(multipleFiles) {
                    doProcess(stream, uploadMetadataBuilder.build(), t -> new Transfer(t).setTitle(FilenameUtils.getBaseName(t.getSource())), transfers);
                } else {
                    doProcess(stream, uploadMetadataBuilder.build(), t -> t, transfers);
                    // returns only one file
                    return transfers;
                }
            } else {
                processFormField(uploadMetadataBuilder, stream);
            }
        }
        return transfers;
    }

    private void doProcess(FileItemStream fileItemStream, UploadMetadata uploadMetadata, Function<Transfer, Transfer> mapper,
                           List<Transfer> transfers) throws UploadException {
        try (InputStream input = streamDecoder.apply(fileItemStream.openStream())) {
            final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Transfer transfer = new Transfer(uploadMetadata);
            streamConsumer.accept(input, transfer, UUID.fromString(uploadMetadata.getOrgUUID()));
            transfer = mapper.apply(transfer);
            dataAcquisitionClient.uploadCompleted(transfer, "bearer " + tokenExtractor.apply(auth));
            transfers.add(transfer);

        } catch (AccessControlException ex) {
            throw new AccessDeniedException("Permission denied", ex);
        } catch (IOException | LoginException | InterruptedException ex) {
            throw new UploadException(transfers, ex);
        }
    }

    private void resolveFileName(UploadMetadataBuilder builder, FileItemStream stream, UploadRequest request) {
        final String fileName = stream.getName();
        LOGGER.info("file: {}", fileName);
        builder.setSource(fileName);
        request.getListener().setFilename(fileName);
    }

    private void processFormField(UploadMetadataBuilder builder, FileItemStream stream) throws IOException {
        final String fieldName = stream.getFieldName();
        final String fieldValue = Streams.asString(stream.openStream());
        LOGGER.info("{} : {}", fieldName, fieldValue);
        builder.setProperty(fieldName, fieldValue);
    }
}
