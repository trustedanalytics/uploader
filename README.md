[![Build Status](https://travis-ci.org/trustedanalytics/uploader.svg?branch=master)](https://travis-ci.org/trustedanalytics/uploader)
[![Dependency Status](https://www.versioneye.com/user/projects/5729c2a7a0ca35005083fc0f/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5729c2a7a0ca35005083fc0f)

# uploader
Service for uploading data

# Local development

For locally file upload is used directory "../uploader/target/uploads" (to retrieve this path we are using "user.dir" system property):
To run the service locally, the following environment variables need to be defined:

* `VCAP_SERVICES`
* `HDFS_USER`

In Kerberos environment two additional variables are required:

* `KERBEROS_USER`
* `KERBEROS_PASS`

To run the application, type:

    mvn spring-boot:run -Dspring.profiles.active=local

To change the default listening port (8080), add an additional option -Dserver.port=9994

For generating proper request to uploader you could use: src/test/resources/upload_html/upload_test.html and multiple_files_upload_test.html
After open it in web browser, choose files from disk. Form in multiple_files_upload_test.html allows multiple selections.

# Calling upload with REST API

* Upload single file

  Path: `/rest/upload/{orgGuid}`

Example response:
```
{
    "idInObjectStore": "4ad99778-1ce2-4dcf-a84a-26a08de63b9f",
    "objectStoreId": "hdfs://nameservice1/org/31e3c350-e937-4c59-ae15-25ddf227babd/brokers/userspace/da31788b-fad1-4b26-9772-722e90d7f057",
    "publicAccess": false,
    "category": "business",
    "title": "upload-data",
    "orgUUID": "31e3c350-e937-4c59-ae15-25ddf227babd",
    "source": "test1.txt"
}
```
* Upload multiple files

  Path: `/rest/v1/files/{orgGuid}`

Example response:
```
[
  {
    "idInObjectStore": "4ad99778-1ce2-4dcf-a84a-26a08de63b9f",
    "objectStoreId": "hdfs://nameservice1/org/31e3c350-e937-4c59-ae15-25ddf227babd/brokers/userspace/da31788b-fad1-4b26-9772-722e90d7f057",
    "publicAccess": false,
    "category": "business",
    "title": "test1",
    "orgUUID": "31e3c350-e937-4c59-ae15-25ddf227babd",
    "source": "test1.txt"
  },
  {
    "idInObjectStore": "442cd341-4886-4939-be81-25c187320b82",
    "objectStoreId": "hdfs://nameservice1/org/31e3c350-e937-4c59-ae15-25ddf227babd/brokers/userspace/da31788b-fad1-4b26-9772-722e90d7f057",
    "publicAccess": false,
    "category": "business",
    "title": "test2",
    "orgUUID": "31e3c350-e937-4c59-ae15-25ddf227babd",
    "source": "test2.txt"
  },
  {
    "idInObjectStore": "92869b23-db9d-4015-b91e-5d1d70aec9fa",
    "objectStoreId": "hdfs://nameservice1/org/31e3c350-e937-4c59-ae15-25ddf227babd/brokers/userspace/da31788b-fad1-4b26-9772-722e90d7f057",
    "publicAccess": false,
    "category": "business",
    "title": "test",
    "orgUUID": "31e3c350-e937-4c59-ae15-25ddf227babd",
    "source": "test.txt"
  }
]

```

`ObjectStoreId` is the path to file on HDFS.

Files are stored on HDFS and title of the dataset is the same as filename without extension.
To upload files you can run bash scripts multiple_upload_curl and upload_curl from upload_scripts directory.
To use it:
 1. Log in to Cloud Foundry
 2. Give script executable permissions
 3. Run the script and specify organization and catalog with files or path to file

```
./upload_curl --org=<organization name> --file='<path>'
```
or
```
./multiple_upload_curl --org=<organization name> --dir='<path>'
```

Default files are uploaded on localhost.
If you provide environment domain as argument, you can upload files using uploader service on environment

```
./upload_curl --org=<organization name> --file='<path>' --domain=<domain>
```
or
```
./multiple_upload_curl --org=<organization name> --dir='<path>' --domain=<domain>
```

If you change port while starting application, you should run script with additional argument `--port=<port number>`
# Uploading files with curl

* upload single file
```
curl -H "Authorization: `cf oauth-token | grep bearer`" -v -F orgUUID=<org guid> -F category=<category> -F title=<title> -F upload=@<path to file> http://localhost:8080/rest/upload/<org guid>
```
* upload multiple files
```
curl -H "Authorization: `cf oauth-token | grep bearer`" -v -F orgUUID=<org guid> -F category=<category> -F title=<title> -F upload=@<path to file> http://localhost:8080/rest/v1/files/<org guid>
```
You can add as many `-F upload=@<file>` as you want if you upload multiple files. You can put path to file which you want upload or filename from current directory.
