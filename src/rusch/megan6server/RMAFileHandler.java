/** 
 * Copyright (C) 2015 Hans-Joachim Ruscheweyh
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

import megan.daa.connector.DAAConnector;
import megan.daa.io.DAAParser;
import megan.data.IConnector;
import megan.rma2.RMA2Connector;
import megan.rma2.RMA2File;
import megan.rma3.RMA3Connector;
import megan.rma6.RMA6Connector;

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
	public static enum FILETYPE {RMA2_FILE, RMA3_FILE, RMA6_FILE, DAA_FILE};
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
		InputStream input = this.getClass().getClassLoader().getResourceAsStream("meganserver.properties");
		if(input == null){
			input = this.getClass().getClassLoader().getResourceAsStream("/meganserver.properties");
		}

		if(input==null)
			throw new RuntimeException("File not found in path: meganserver.properties");

		prop.load(input);
		String rootFolder = prop.getProperty("rma.rootFolder").trim();
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

		id2FileName.clear();
		id2FileType.clear();
		while(!folders.isEmpty()){
			final List<String> rmafiles = new ArrayList<String>();
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
				} else if (rmafile.toLowerCase().endsWith(".daa")) {
					fileType = FILETYPE.DAA_FILE;
					if(!DAAParser.isMeganizedDAAFile(rmafile, true)){
						logger.warn("File " + rmafile + " is a daa file that has not been meganized. Will ignore it.");
						fileType = null;
					}
				}
				if (fileType == null) {
					//logger.warn("File " + rmafile + " is not a rma/meganized daa file.");
				} else {
					id2FileType.put(id, fileType);
					id2FileName.put(id, rmafile);
				}
			}
			folders.remove(0);
		}
		logger.info(String.format("Done updating filesystem. Found %s RMA/DAA files", id2FileName.size()));
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
		IConnector connector = null;
		switch(type){
		case RMA2_FILE:
			connector =  new RMA2Connector(rmafile);
			break;
		case RMA3_FILE:
			connector = new RMA3Connector(rmafile);
			break;
		case RMA6_FILE:
			connector = new RMA6Connector(rmafile);
			break;
		case DAA_FILE:
			connector = new DAAConnector(rmafile);
			break;
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
