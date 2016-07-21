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

import org.trustedanalytics.uploader.core.listener.FileUploadListener;
import org.trustedanalytics.uploader.security.PermissionVerifier;
import org.trustedanalytics.uploader.service.UploadService;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
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
import java.util.Collection;
import java.util.UUID;

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
    private final UploadService uploadService;
    private final PermissionVerifier permissionVerifier;

    @Autowired
    public UploadController(UploadService uploadService, PermissionVerifier permissionVerifier) {
        this.uploadService = uploadService;
        this.permissionVerifier = permissionVerifier;
    }

    @ApiOperation(
            value = "Uploads file as multipart content together with metadata.",
            notes = "Privilege level: Consumer of this endpoint must be a member of specified organization."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The request has succeeded", response = Transfer.class),
            @ApiResponse(code = 400, message = "The request could not be understood by the server due to malformed syntax"),
            @ApiResponse(code = 403, message = "User is not permitted to perform the requested operation"),
            @ApiResponse(code = 500, message = "Service encountered an unexpected condition which prevented it from fulfilling the request")
    })
    @RequestMapping(value = "/rest/upload/{orgGuid}", method = RequestMethod.POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public Transfer uploadFile(HttpServletRequest request, @PathVariable("orgGuid") UUID orgGuid)
            throws IOException, FileUploadException {

        final ServletFileUpload upload = new ServletFileUpload();
        final UploadRequest uploadRequest = toUploadRequest(request, orgGuid, upload);
        return uploadService.processUpload(upload.getItemIterator(request), uploadRequest, false).get(0);
    }


    @ApiOperation(
            value = "Uploads multiple files as multipart content together with metadata.",
            notes = "Privilege level: Consumer of this endpoint must be a member of specified organization."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The request has succeeded", response = Collection.class),
            @ApiResponse(code = 400, message = "The request could not be understood by the server due to malformed syntax"),
            @ApiResponse(code = 403, message = "User is not permitted to perform the requested operation"),
            @ApiResponse(code = 500, message = "Service encountered an unexpected condition which prevented it from fulfilling the request")
    })
    @RequestMapping(value = "/rest/v1/files/{orgGuid}", method = RequestMethod.POST)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public Collection<Transfer> uploadFiles(HttpServletRequest request, @PathVariable("orgGuid") UUID orgGuid)
            throws IOException, FileUploadException {

        final ServletFileUpload upload = new ServletFileUpload();
        final UploadRequest uploadRequest = toUploadRequest(request, orgGuid, upload);
        return uploadService.processUpload(upload.getItemIterator(request), uploadRequest, true);
    }


    private UploadRequest toUploadRequest(HttpServletRequest request, UUID orgGuid, ServletFileUpload upload) {
        checkArgument(ServletFileUpload.isMultipartContent(request), "No multipart content");

        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        permissionVerifier.checkOrganizationAccess(orgGuid, auth);

        final FileUploadListener listener = new FileUploadListener();
        upload.setProgressListener(listener);
        return new UploadRequest(orgGuid, listener);
    }
}
