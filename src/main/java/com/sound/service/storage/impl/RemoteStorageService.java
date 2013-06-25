package com.sound.service.storage.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.aliyun.openservices.ClientConfiguration;
import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.model.GetObjectRequest;
import com.aliyun.openservices.oss.model.ListObjectsRequest;
import com.aliyun.openservices.oss.model.OSSObjectSummary;
import com.aliyun.openservices.oss.model.ObjectListing;
import com.aliyun.openservices.oss.model.ObjectMetadata;
import com.sound.exception.RemoteStorageException;

@Service
@Scope("singleton")
public class RemoteStorageService implements
		com.sound.service.storage.itf.RemoteStorageService {

	private static final String OSS_CONFIG_FILE = "ossConfig.properties";

	private static final int DEFAULT_CONEECTIONTIMEOUT = 10000;

	private static final int DEFAULT_MAXCONNECTION = 1000;
	private static final int DEFAULT_MAXERRORRETRY = 3;
	private static final int DEFAULT_SOCKETIMEOUT = 2000;
	private static final String DEFAULT_USERAGENT = "dxd-oss";
	private static final String DEFAULT_BUCKET = "dxd";
	private static final String ENDPOINT = "http://oss.aliyuncs.com";

	/** The oss client instance */
	private OSSClient client;

	/** The client configuration. */
	private ClientConfiguration clientConfig;

	private PropertiesConfiguration config;

	private String bucket;

	public RemoteStorageService() throws RemoteStorageException {
		// Load the properties file.
		loadPropertiesConfiguration();

		initClientConfig();

		client = new OSSClient(ENDPOINT, config.getString("ACCESS_ID"),
				config.getString("ACCESS_KEY"), clientConfig);
		bucket = config.getString("Bucket", DEFAULT_BUCKET);

		try {
			client.listBuckets();
		} catch (Exception e) {
			client = null;
			throw new RemoteStorageException("Cannot access oss", e);
		}

	}

	private void initClientConfig() {
		// Initialized the configuration.
		clientConfig = new ClientConfiguration();
		clientConfig.setConnectionTimeout(config.getInt("ConnectionTimeout",
				DEFAULT_CONEECTIONTIMEOUT));
		clientConfig.setMaxConnections(config.getInt("MaxConnections",
				DEFAULT_MAXCONNECTION));
		clientConfig.setMaxErrorRetry(config.getInt("MaxErrorRetry",
				DEFAULT_MAXERRORRETRY));
		clientConfig.setSocketTimeout(config.getInt("SocketTimeout",
				DEFAULT_SOCKETIMEOUT));
		clientConfig.setUserAgent(config.getString("UserAgent",
				DEFAULT_USERAGENT));
	}

	private void loadPropertiesConfiguration() throws RemoteStorageException {
		try {
			config = new PropertiesConfiguration(OSS_CONFIG_FILE);

		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		if (!config.containsKey("ACCESS_ID")
				|| !config.containsKey("ACCESS_KEY")
				|| !config.containsKey("Bucket")) {
			throw new RemoteStorageException(
					"OSS config should have ACCESS_ID, ACCESS_KEY and Bucket config");
		}

	}

	@Override
	public List<String> listOwnedFiles(String ownerId)
			throws RemoteStorageException {
		if (ownerId == null || ownerId.trim().equals("")) {
			throw new RemoteStorageException(
					"Cannot list owned files because input owner id is invalid");
		}
		ListObjectsRequest listObjectsRequest = generateListObjectRequestForOwner(ownerId);
		List<String> ownedFiles = new ArrayList<String>();
		try {
			ObjectListing result = client.listObjects(listObjectsRequest);
			for (OSSObjectSummary obs : result.getObjectSummaries()) {
				ownedFiles.add(obs.getKey());
			}
		} catch (Exception e) {
			throw new RemoteStorageException("Cannot list owned files of : "
					+ ownerId, e);
		}
		return ownedFiles;
	}

	@Override
	public void downloadToFile(String fileName, String fullPath)
			throws RemoteStorageException {
		if (fileName == null || fileName.trim().equals("")) {
			throw new RemoteStorageException(
					"Cannot download file because input filename is invalid");
		}

		GetObjectRequest getObjectRequest = generateGetObjectRequest(fileName);
		try {
			client.getObject(getObjectRequest, new File(fullPath));
		} catch (Exception e) {
			throw new RemoteStorageException("Cannot download file : "
					+ fileName, e);
		}
	}

	@Override
	public InputStream downloadToMemory(String fileName)
			throws RemoteStorageException {
		if (fileName == null || fileName.trim().equals("")) {
			throw new RemoteStorageException(
					"Cannot download file because input filename is invalid");
		}

		GetObjectRequest getObjectRequest = generateGetObjectRequest(fileName);

		try {
			return client.getObject(getObjectRequest).getObjectContent();
		} catch (Exception e) {
			throw new RemoteStorageException("Cannot download file : "
					+ fileName, e);
		}

	}

	@Override
	public void upload(File file) throws RemoteStorageException {
		if (file == null || !file.exists()) {
			throw new RemoteStorageException(
					"Cannot upload File beacause target file is invalid");
		}

		InputStream content;
		try {
			content = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new RemoteStorageException("Cannot find file "
					+ file.getName(), e);
		}

		ObjectMetadata meta = generateObjectMetadata(file);

		try {
			client.putObject(bucket, file.getName(), content, meta);
		} catch (Exception e) {
			throw new RemoteStorageException("Cannot upload file : "
					+ file.getName(), e);
		}
	}

	@Override
	public void delete(String fileName) throws RemoteStorageException {
		if (fileName == null || fileName.trim().equals("")) {
			throw new RemoteStorageException(
					"Cannot delete file because input filename is invalid");
		}

		try {
			client.deleteObject(bucket, fileName);
		} catch (Exception e) {
			throw new RemoteStorageException(
					"Cannot delete file : " + fileName, e);
		}

	}

	private GetObjectRequest generateGetObjectRequest(String fileName) {
		GetObjectRequest getObjectRequest = new GetObjectRequest(bucket,
				fileName);

		return getObjectRequest;
	}

	private ListObjectsRequest generateListObjectRequestForOwner(String ownerId) {
		ListObjectsRequest listObjectRequest = new ListObjectsRequest();
		listObjectRequest.setBucketName(bucket);
		listObjectRequest.setPrefix(ownerId);
		listObjectRequest.setDelimiter("/");

		return listObjectRequest;
	}

	private ObjectMetadata generateObjectMetadata(File file) {
		ObjectMetadata meta = new ObjectMetadata();

		meta.setContentLength(file.length());
		Date expire = new Date(new Date().getTime() + 3600 * 1000);
		meta.setExpirationTime(expire);

		return meta;
	}

}
