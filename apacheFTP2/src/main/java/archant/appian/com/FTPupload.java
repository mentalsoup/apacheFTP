package archant.appian.com;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import org.apache.log4j.Logger;

import com.appiancorp.suiteapi.common.Name;
import com.appiancorp.suiteapi.content.ContentConstants;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.knowledge.Document;
import com.appiancorp.suiteapi.knowledge.DocumentDataType;
import com.appiancorp.suiteapi.process.exceptions.SmartServiceException;
import com.appiancorp.suiteapi.process.framework.AppianSmartService;
import com.appiancorp.suiteapi.process.framework.Input;
import com.appiancorp.suiteapi.process.framework.MessageContainer;
import com.appiancorp.suiteapi.process.framework.Required;
import com.appiancorp.suiteapi.process.framework.SmartServiceContext;
import com.appiancorp.suiteapi.process.palette.PaletteInfo;

@PaletteInfo(paletteCategory = "Integration Services", palette = "Connectivity Services")
public class FTPupload extends AppianSmartService {

	private static final Logger LOG = Logger.getLogger(FTPupload.class);
	private final SmartServiceContext smartServiceCtx;
	private Long document;
	private String username;
	private String password;
	private String host;
	// private String port;
	private int port = 21;
	private String fileName;
	private String remoteFilePath;
	private String documentName;
	private ContentService contentService;

	// public static void main(String[] args) {
	// String server = host;
	// int port = 21;
	// String user = username;
	// String pass = password;

	@Override
	public void run() throws SmartServiceException {
		try {
			sendFile(username, password, host, port, remoteFilePath, fileName,document);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendFile(String username, String password, String host, int port, String filePath, String fileName, Long documentID)
			throws Exception {
		Document d[] = (Document[]) null;
		try {
			d = contentService.download(documentID,
					ContentConstants.VERSION_CURRENT, Boolean.valueOf(false));
		} catch (Exception e) {
			throw e;
		}
		Document ftpDoc = d[0];

		String fileLocation = ftpDoc.getInternalFilename();
		// Read the file
		File file = new File(fileLocation);
		InputStream is = new FileInputStream(file);
		long len = file.length();
		if (len > Integer.MAX_VALUE)
			LOG.error("File too large");
		byte[] bt = new byte[(int) len];
		int offset = 0;
		int numRead = 0;
		while (offset < bt.length
				&& (numRead = is.read(bt, offset, bt.length - offset)) >= 0) {
			offset += numRead;
		}
		// Ensure all the bytes have been read in
		if (offset < bt.length) {
			// throw new
			// IOException("Could not completely read file "+file.getName());
			LOG.error("Count not complete reading file");
		}
		// Close the input stream and return bytes
		is.close();

		FTPClient ftpClient = new FTPClient();
		try {

			ftpClient.connect(host, port);
			ftpClient.login(username, password);
			ftpClient.enterLocalPassiveMode();

			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			
			// FTP upload speed resolved using http://stackoverflow.com/questions/14000341/why-is-ftp-upload-slow-in-java-7
			// without setting a 2.1Mb file tool 8mins20sec. With the setting in place for the same file, 5secs.
			ftpClient.setBufferSize(0);

			// APPROACH #1: uploads first file using an InputStream
			File firstLocalFile = new File(fileLocation);

			String firstRemoteFile = (new StringBuilder(filePath+"/"+(String.valueOf(d[0]
					.getName())))).append(".").append(d[0].getExtension())
					.toString();
			InputStream inputStream = new FileInputStream(firstLocalFile);

			System.out.println("Start uploading first file");
			boolean done = ftpClient.storeFile(firstRemoteFile, inputStream);
			inputStream.close();
			
			if (done) {
                System.out.println("File is uploaded successfully.");
        }

		} catch (IOException ex) {
			System.out.println("Error: " + ex.getMessage());
			ex.printStackTrace();
		} finally {
			try {
				if (ftpClient.isConnected()) {
					ftpClient.logout();
					ftpClient.disconnect();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public FTPupload(SmartServiceContext smartServiceCtx,
			ContentService cs) {
		super();
		this.smartServiceCtx = smartServiceCtx;
		this.contentService = cs;
	}

	public void onSave(MessageContainer messages) {
	}

	public void validate(MessageContainer messages) {
	}

	@Input(required = Required.OPTIONAL)
	@Name("document")
	@DocumentDataType
	public void setDocument(Long val) {
		this.document = val;
	}

	@Input(required = Required.OPTIONAL)
	@Name("username")
	public void setUsername(String val) {
		this.username = val;
	}

	@Input(required = Required.OPTIONAL)
	@Name("password")
	public void setPassword(String val) {
		this.password = val;
	}

	@Input(required = Required.OPTIONAL)
	@Name("host")
	public void setHost(String val) {
		this.host = val;
	}

	// @Input(required = Required.OPTIONAL)
	// @Name("port")
	// public void setPort(String val) {
	// this.port = val;
	// }

	@Input(required = Required.OPTIONAL)
	@Name("fileName")
	public void setFileName(String val) {
		this.fileName = val;
	}

	@Input(required = Required.OPTIONAL)
	@Name("remoteFilePath")
	public void setRemoteFilePath(String val) {
		this.remoteFilePath = val;
	}

	@Input(required = Required.OPTIONAL)
	@Name("documentName")
	public void setDocumentName(String val) {
		this.documentName = val;
	}
}
