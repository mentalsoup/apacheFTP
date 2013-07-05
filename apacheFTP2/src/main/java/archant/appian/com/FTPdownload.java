package archant.appian.com;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
public class FTPdownload extends AppianSmartService {

	private static final Logger LOG = Logger.getLogger(FTPdownload.class);
	private final SmartServiceContext smartServiceCtx;
	private String username;
	private String password;
	private String host;
	// private String port;
	private int port = 21;
	private String fileName;
	private String remoteFilePath;
	private String localFilePath;
	private ContentService contentService;
	private boolean success;
	private boolean deleteTheFile;

	// public static void main(String[] args) {
	// String server = host;
	// int port = 21;
	// String user = username;
	// String pass = password;

	@Override
	public void run() throws SmartServiceException {
		try {
			sendFile(username, password, host, port, remoteFilePath, localFilePath, fileName, deleteTheFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendFile(String username, String password, String host, int port, String filePath1, String filePath2, String fileName, Boolean deleteTheFile)
			throws Exception {
			String fileDestination = (new StringBuilder(filePath2+"/"+fileName)).toString();
			//"C:/TAG/976231JJ.zip"
			String fileLocation = (new StringBuilder(filePath1+"/"+fileName)).toString();
			//"/Tag_Send/976231JJ.zip"
			
	        FTPClient ftpClient = new FTPClient();
	        try {
	 
	            ftpClient.connect(host, port);
	            ftpClient.login(username, password);
	            ftpClient.enterLocalPassiveMode();
	            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

	            String remoteFile2 = fileLocation;
	            File downloadFile2 = new File(fileDestination);
	            OutputStream outputStream2 = new BufferedOutputStream(new FileOutputStream(downloadFile2));
	            InputStream inputStream = ftpClient.retrieveFileStream(remoteFile2);
	            byte[] bytesArray = new byte[4096];
	            int bytesRead = -1;
	            while ((bytesRead = inputStream.read(bytesArray)) != -1) {
	                outputStream2.write(bytesArray, 0, bytesRead);
	            }
	 
	            success = ftpClient.completePendingCommand();
	            if (success) {
	                System.out.println("File "+fileName+" has been downloaded successfully.");
	            }
	            outputStream2.close();
	            inputStream.close();
	            
	            boolean enabledelete = deleteTheFile;
	            
	            if (enabledelete) {
	               
	            String fileToDelete = fileLocation;
	 
	            boolean deleted = ftpClient.deleteFile(fileToDelete);
	            if (deleted) {
	                System.out.println("The file was deleted successfully.");
	            } else {
	                System.out.println("Could not delete the  file, it may not exist.");
	            }
	            } else {
	                System.out.println("File deletion disabled.");
	            }
	        }	 
		        catch (IOException ex) {
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

	public FTPdownload(SmartServiceContext smartServiceCtx,
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
	@Name("localFilePath")
	public void setLocalFilePath(String val) {
		this.localFilePath = val;
	}
	
	@Input(required = Required.OPTIONAL)
	@Name("deleteTheFile")
	public void setDeleteTheFile(Boolean val) {
		this.deleteTheFile = val;
	}

}
