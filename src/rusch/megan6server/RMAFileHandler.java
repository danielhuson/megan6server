package rusch.megan6server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import megan.data.IConnector;
import megan.rma2.RMA2Connector;
import megan.rma2.RMA2File;
import megan.rma3.RMA3Connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import rusch.megan5client.RMADataset;

/**
 * 
 * @author Hans-Joachim Ruscheweyh
 * 3:03:07 PM - Oct 27, 2014
 *
 * Simple File Handler that takes a root directory and looks for all rma files in all subdirs.
 * Then provides this to the executing instances as path or id
 *
 */
public class RMAFileHandler {
	private File rootDirectory;
	private final Map<Integer, String> id2FileName;
	private final Map<Integer, FILETYPE> id2FileType;
	public static enum FILETYPE {RMA2_FILE, RMA3_FILE, RMA6_FILE};
	private static final Logger logger = LoggerFactory.getLogger(RMAFileHandler.class);

	/**Constructor
	 * 
	 */
	public RMAFileHandler(){
		id2FileName = new HashMap<>();
		id2FileType = new HashMap<>();
		try {
			updateFilesystem();
		} catch (IOException e) {
			logger.error("Exception init filesystem: ", e);
		}
	}


	/**
	 * Update the files which are there.
	 * 
	 * TODO this should probably happen either on trigger command, or should watch if there are changes, or every once in a while
	 * @throws IOException 
	 * 
	 * 
	 */
	protected synchronized void updateFilesystem() throws IOException{
		Properties prop = new Properties();
		InputStream input = this.getClass().getClassLoader().getResourceAsStream("megan5server.properties");
		if(input == null){
			input = this.getClass().getClassLoader().getResourceAsStream("/megan5server.properties");
		}

		if(input==null)
			throw new RuntimeException("File not found in path: megan5server.properties");

		prop.load(input);
		String rootFolder = prop.getProperty("rma.rootFolder");
		if(rootFolder.equals("")){
			rootFolder = System.getProperty("user.home");
		}
		input.close();
		Assert.notNull(rootFolder);
		File rootDirectory = new File(rootFolder);
		rootDirectory = rootDirectory.getAbsoluteFile();
		if (!rootDirectory.isDirectory())
			throw new IOException("Not a directory: " + rootDirectory);
		if (!rootDirectory.exists())
			throw new IOException("Directory not found: " + rootDirectory);
		if (!rootDirectory.canRead())
			throw new IOException("Cannot read: " + rootDirectory);
		logger.info("Set root directory to: " + rootDirectory);
		this.rootDirectory = rootDirectory;
		logger.info("Updating filesystem.");
		final List<File> folders = new ArrayList<File>();
		folders.add(rootDirectory);
		final List<String> rmafiles = new ArrayList<String>();
		id2FileName.clear();
		id2FileType.clear();
		while(!folders.isEmpty()){
			for(String file : folders.get(0).list()){
				String file2 = folders.get(0) + File.separator + file;
				//				if(RMAFileFilter.getInstance().accept(new File(file2)) && !file.endsWith(".rmaz")){ The new RMAFileFilter doesnt support this.. so lets skip
				rmafiles.add(file2); 
				//				}
				File f = new File(file2);
				if(f.isDirectory()){
					if(f.canRead()){
						folders.add(f);
					}
				}
			}
			for (String rmafile : rmafiles) {
				int id = Math.abs(rmafile.hashCode());
				FILETYPE fileType = null;
				if (rmafile.toLowerCase().endsWith(".rma2")) {
					fileType = FILETYPE.RMA2_FILE;
				} else if (rmafile.toLowerCase().endsWith(".rma3")) {
					fileType = FILETYPE.RMA3_FILE;
				} else if (rmafile.toLowerCase().endsWith(".rma6")) {
					fileType = FILETYPE.RMA6_FILE;
				} else if (rmafile.toLowerCase().endsWith(".rma")) {
					int version = RMA2File.getRMAVersion(new File(rmafile));
					if (version == 2)
						fileType = FILETYPE.RMA2_FILE;
					else if (version == 3)
						fileType = FILETYPE.RMA3_FILE;
					else if (version == 6)
						fileType = FILETYPE.RMA6_FILE;
				}
				

				if (fileType == null) {
					logger.warn("File " + rmafile + " is not a rma file. Why did it pass the filter?");
				} else {
					id2FileType.put(id, fileType);
					id2FileName.put(id, rmafile);
				}
			}
			folders.remove(0);
		}
		logger.info(String.format("Done updating filesystem. Found %s RMA files", id2FileName.size()));
	}


	/**The getter
	 * 
	 * @param fileId
	 * @return
	 * @throws IOException
	 */
	public String getRMAFileAbsolutePath(int fileId) throws IOException{
		String rmafile = id2FileName.get(fileId);
		Assert.notNull(rmafile);
		return rmafile;
	}

	/**
	 * resolve the identifier to the Integer Id
	 * 
	 * @param file
	 * @return id
	 * @throws FileNotFoundException
	 */
	public int resolveFileIdentifierToId(String file) throws FileNotFoundException{
		int fileId = -1;
		try{
			fileId = Integer.parseInt(file);
		}catch(NumberFormatException e){
			// well then its a string and the path has to be tested
			for(Entry<Integer, String>  entry: id2FileName.entrySet()){
				if(entry.getValue().endsWith(file)){
					fileId = entry.getKey();
					break;
				}
			}
		}
		if(fileId == -1){
			throw new FileNotFoundException("File with identifier "+file+" not found in current database. Maybe need to update the filesystem using the admin functions?");
		}
		return fileId;
	}


	/**Resolve the identifier to the file path
	 * 
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	public String resolveFileIdentifierToPath(String file) throws FileNotFoundException{
		int fileId = -1;
		try{
			fileId = Integer.parseInt(file);
			return id2FileName.get(fileId);
		}catch(NumberFormatException e){
			return file;
		}
	}

	/**
	 * get the connector
	 * 
	 * @param file
	 * @return connector
	 * @throws IOException
	 */
	public IConnector getIConnector(String file) throws IOException{
		final int fileId = resolveFileIdentifierToId(file);
		final String rmafile = getRMAFileAbsolutePath(fileId);

		final FILETYPE type =  id2FileType.get(fileId);
		Assert.notNull(type);
		IConnector connector;
		if(type.equals(FILETYPE.RMA2_FILE)){
			connector =  new RMA2Connector(rmafile);
		}else{
			connector = new RMA3Connector(rmafile);
		}
		Assert.notNull(connector);
		return connector;
	}


	/**The all getter
	 * 
	 * @return
	 */
	public RMADataset[] getAllDatasets() {
		RMADataset[] map = new RMADataset[id2FileName.size()];
		int pos = 0;
		for(Entry<Integer, String> entry : id2FileName.entrySet()){
			map[pos] = new RMADataset(entry.getKey(), entry.getValue().replace(rootDirectory.getAbsolutePath() + File.separator, ""));
			pos++;
		}
		return map;
	}
}
