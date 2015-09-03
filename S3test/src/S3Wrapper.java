import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.UUID;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;

public class S3Wrapper {
	public static void upload(String direc, String virtualDirec,
			String bucketName) throws IOException {
		AWSCredentials credentials = null;
		File file = new File(direc);
		if (!file.exists() || !file.isDirectory()) {
			System.out.println("directory not valid");
			return;
		}

		try {
			credentials = new ProfileCredentialsProvider("default")
					.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. "
							+ "Please make sure that your credentials file is at the correct "
							+ "location (/home/honolulu413/.aws/credentials), and is in valid format.",
					e);
		}
		TransferManager tx = new TransferManager(credentials);
		
		int i = 0;
		for (File textFile : file.listFiles()) {
			Upload myUpload = tx.upload(bucketName, virtualDirec + "/" + textFile.getName(), textFile);

			try {
				myUpload.waitForCompletion();
				i++;
				System.out.println(i);
			} catch (AmazonServiceException e) {
				// e.printStackTrace();
			} catch (AmazonClientException e) {
				// e.printStackTrace();
			} catch (InterruptedException e) {
				// e.printStackTrace();
			}
		}
		tx.shutdownNow();
		System.out.println("upload finished");
	}

	public static void download(String direc, String virtualDirec,
			String bucketName) throws IOException {
		AWSCredentials credentials = null;
		File file = new File(direc);
		if (!file.exists() || !file.isDirectory()) {
			System.out.println("directory not valid");
			return;
		}

		try {
			credentials = new ProfileCredentialsProvider("default")
					.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. "
							+ "Please make sure that your credentials file is at the correct "
							+ "location (/home/honolulu413/.aws/credentials), and is in valid format.",
					e);
		}
		TransferManager tx = new TransferManager(credentials);
		MultipleFileDownload myDownload = tx.downloadDirectory(bucketName,
				virtualDirec, file);
		try {
			myDownload.waitForCompletion();
		} catch (AmazonServiceException e) {
			e.printStackTrace();
		} catch (AmazonClientException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		tx.shutdownNow();
		System.out.println("download finished");
	}

	public static void main(String[] args) throws IOException {
		// download("/home/honolulu413/upload", "cde", "cis555");
		if (args.length == 4) {
			if (args[0].equals("upload")) {
				upload(args[1], args[2], args[3]);
			}

			if (args[0].equals("download")) {
				download(args[1], args[2], args[3]);
			}
		}
	}

}
