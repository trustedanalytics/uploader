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
package org.trustedanalytics.uploader.rest;

import static com.google.common.base.Preconditions.checkArgument;

import org.trustedanalytics.uploader.client.DataAcquisitionClient;
import org.trustedanalytics.uploader.core.listener.FileUploadListener;
import org.trustedanalytics.uploader.rest.UploadResponse.UploadResponseBuilder;
import org.trustedanalytics.uploader.security.PermissionVerifier;
import org.trustedanalytics.uploader.service.UploadService;

import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * By default spring and servlets use classic approach of downloading file to temporary
 * directory and then using it. We want to immediately redirect stream to destination
 * and therefore we need to disable MultipartAutoConfiguration and work with HttpServletRequest.
 *
 * https://commons.apache.org/proper/commons-fileupload/streaming.html
 * http://stackoverflow.com/questions/24388294/spring-mvc-file-upload-controller-id-like-the-controller-to-be-called-as-soon
 */
@RestController
public class UploadController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadController.class);

    private final UploadService uploadService;
    private final Function<Authentication, String> tokenExtractor;
    private final DataAcquisitionClient dasClient;
    private final PermissionVerifier permissionVerifier;

    @Autowired
    public UploadController(UploadService uploadService,
            Function<Authentication, String> tokenExtractor, DataAcquisitionClient dasClient,
        PermissionVerifier permissionVerifier) {
        this.uploadService = uploadService;
        this.tokenExtractor = tokenExtractor;
        this.dasClient = dasClient;
        this.permissionVerifier = permissionVerifier;
    }


    @ApiOperation(
            value = "Uploads file as multipart content together with metadata.",
            notes = "Privilege level: Consumer of this endpoint must be a member of specified organization."
    )
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "The request has succeeded", response = UploadResponse.class),
        @ApiResponse(code = 400, message = "The request could not be understood by the server due to malformed syntax"),
        @ApiResponse(code = 403, message = "User is not permitted to perform the requested operation"),
        @ApiResponse(code = 500, message = "Service encountered an unexpected condition which prevented it from fulfilling the request")
    })
    @RequestMapping(value = "/rest/upload/{orgGuid}", method = RequestMethod.POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public UploadResponse upload(HttpServletRequest request, @PathVariable("orgGuid") UUID orgGuid)
            throws IOException, FileUploadException {
        checkArgument(ServletFileUpload.isMultipartContent(request), "No multipart content");

        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        permissionVerifier.checkOrganizationAccess(orgGuid, auth);

        final ServletFileUpload upload = new ServletFileUpload();
        final FileUploadListener listener = new FileUploadListener();
        upload.setProgressListener(listener);

        final UploadRequest uploadRequest = new UploadRequest(orgGuid, listener);
        final UploadResponse uploadResponse = processUpload(upload.getItemIterator(request), uploadRequest);
        dasClient.uploadCompleted(uploadResponse, "bearer " + tokenExtractor.apply(auth));

        return uploadResponse;
    }

    private UploadResponse processUpload(FileItemIterator iterator, UploadRequest request)
            throws IOException, FileUploadException {
        final UploadResponseBuilder uploadResponseBuilder = UploadResponse.builder();

        LOGGER.info("Upload started");
        while (iterator.hasNext()) {
            FileItemStream stream = iterator.next();
            if (!stream.isFormField()) {
                processFile(stream, request, uploadResponseBuilder);
            } else {
                processFormFiled(stream, uploadResponseBuilder);
            }
        }
        LOGGER.info("Upload completed");

        return uploadResponseBuilder.build();
    }

    private void processFile(FileItemStream stream, UploadRequest request, UploadResponseBuilder responseBuilder)
        throws IOException {
        final String fileName = stream.getName();
        LOGGER.info("file: {}", fileName);
        request.getListener().setFilename(fileName);
        uploadService.upload(stream.openStream(), responseBuilder.setSource(fileName), request.getOrg());
    }

    private void processFormFiled(FileItemStream stream, UploadResponseBuilder responseBuilder)
        throws IOException {
        final String fieldName = stream.getFieldName();
        final String fieldValue = Streams.asString(stream.openStream());
        LOGGER.info("{} : {}", fieldName, fieldValue);
        responseBuilder.setProperty(fieldName, fieldValue);
    }
}
