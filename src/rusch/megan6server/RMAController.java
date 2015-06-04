package rusch.megan6server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import megan.data.DataSelection;
import megan.data.FindSelection;
import megan.data.IConnector;
import megan.rma2.IReadBlockGetter;
import megan.rma2.IReadBlockIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import rusch.megan5client.ClassificationBlockServer;
import rusch.megan5client.DataSelectionSerializer;
import rusch.megan5client.Metadata;
import rusch.megan5client.RMADataset;
import rusch.megan5client.ReadBlockServer;
import rusch.megan5client.connector.RMAControllerMappings;
import rusch.megan5client.connector.ReadBlockPage;
import rusch.megan6server.cache.AuxiliaryCache;
import rusch.megan6server.pagination.PageManager;

/**The main class of the Megan5Server. Contains all mapping information and allows one to contact RMA files.
 * 
 * 
 * 
 * @author Hans-Joachim Ruscheweyh
 * 3:03:19 PM - Oct 27, 2014
 *
 */
@Controller
public class RMAController {
	private static final Logger logger = LoggerFactory.getLogger(RMAController.class);
	private RMAFileHandler rma3FileHandler;
	private PageManager pageManager;
	private AuxiliaryCache cache;
	@Autowired
	public TextFileAuthentication textFileAuthentication;
	public RMAController(){
		this.rma3FileHandler = new RMAFileHandler();
		this.pageManager = new PageManager();
		this.cache = new AuxiliaryCache();
	}

	@RequestMapping(value = RMAControllerMappings.GET_UID_MAPPING, method = RequestMethod.GET)
	public @ResponseBody long getUid(@RequestParam(value="fileId", required=true) String fileId) throws FileNotFoundException{
		return (long) rma3FileHandler.resolveFileIdentifierToId(fileId);
	}

	@RequestMapping(value = "isReadOnly", method = RequestMethod.GET)
	public @ResponseBody boolean isReadOnly(@RequestParam(value="fileId", required=true) String fileId){
		return true;
	}
	@RequestMapping(value = "listDatasets", method = RequestMethod.GET)
	public @ResponseBody RMADataset[] getAllDatasets(@RequestParam(value="includeMetadata", required=false) Boolean includeMetadata) throws IOException{
		if(includeMetadata == null){
			includeMetadata = false;
		}

		RMADataset[] datasets = rma3FileHandler.getAllDatasets();
		for(RMADataset dataset : datasets){
            try {
                Map<String, String> name2value = Metadata.transformMetadataString(getAuxiliaryData(String.valueOf(dataset.getDatasetUid())).get("SAMPLE_ATTRIBUTES"));
                if (name2value.containsKey("Description")) {
                    dataset.setDescription(name2value.get("Description"));
                } else {
                    dataset.setDescription("No description provided");
                }
                if (includeMetadata) {
                    dataset.setMetadata(name2value);
                    dataset.getMetadata().put("@Source", dataset.getDatasetName());
                }
            }
            catch(Exception ex){}
		}



		return datasets;
	}
	@RequestMapping(value = "getAuxiliary", method = RequestMethod.GET)
	public @ResponseBody Map<String, String> getAuxiliaryData(@RequestParam(value="fileId", required=true) String fileId) throws IOException{
		return cache.getAuxBlock(rma3FileHandler, fileId);
	}
	@RequestMapping(value = "getAllClassificationNames", method = RequestMethod.GET)
	public @ResponseBody String[] getAllClassificationNames(@RequestParam(value="fileId", required=true) String fileId) throws IOException {		IConnector connector = rma3FileHandler.getIConnector(fileId);
	return connector.getAllClassificationNames();
	}
	@RequestMapping(value = "getClassificationBlock", method = RequestMethod.GET)
	public @ResponseBody ClassificationBlockServer getClassificationsBlock(@RequestParam(value="fileId", required=true) String fileId, @RequestParam(value="classification", required=true) String classification) throws IOException{
		IConnector connector = rma3FileHandler.getIConnector(fileId);
		return new ClassificationBlockServer(connector.getClassificationBlock(classification));
	}
	@RequestMapping(value = "getAllReadsIterator", method = RequestMethod.GET)
	public @ResponseBody ReadBlockPage getAllReadsIterator(@RequestParam(value="fileId", required=true) String fileId, @RequestParam(value="minScore", required=false) Float minScore, @RequestParam(value="maxExpected", required=false) Float maxExpected, @RequestParam(value="dataSelection", required=false) String[] dataSelection) throws IOException {
		DataSelection dataSel = null;
		if(dataSelection == null){
			dataSel= DataSelection.getSelectionForCreation(true, true, true, true,true);
		}else{
			dataSel = DataSelectionSerializer.deserializeDataSelection(dataSelection);
		}
		if(minScore == null){
			minScore = 0f;
		}
		if(maxExpected == null){
			maxExpected = 1000000f;
		}
		IConnector connector = rma3FileHandler.getIConnector(fileId);
		IReadBlockIterator it =  connector.getAllReadsIterator(minScore, maxExpected, dataSel);
		ReadBlockPage page =  retrieveReadBlockPage(pageManager.registerPaginator(it));
		page.setNextPageUrl(page.getNextPageUrl().replace("getAllReadsIterator", "loadPagedReads"));
		return page;
	}

	@RequestMapping(value = "getReadsIterator", method = RequestMethod.GET)
	public @ResponseBody ReadBlockPage getReadsIterator(@RequestParam(value="fileId", required=true) String fileId, @RequestParam(value="minScore", required=false) Float minScore, @RequestParam(value="maxExpected", required=false) Float maxExpected, @RequestParam(value="classification", required=true) String classification, @RequestParam(value="classId", required=true) int classId, @RequestParam(value="dataSelection", required=false) String[] dataSelection) throws IOException {
		DataSelection dataSel = null;
		if(dataSelection == null){
			dataSel= DataSelection.getSelectionForCreation(true, true, true, true,true);
		}else{
			dataSel = DataSelectionSerializer.deserializeDataSelection(dataSelection);
		}
		if(minScore == null){
			minScore = 0f;
		}
		if(maxExpected == null){
			maxExpected = 1000000f;
		}
		IConnector connector = rma3FileHandler.getIConnector(fileId);
		IReadBlockIterator it = connector.getReadsIterator(classification, classId, minScore, maxExpected, dataSel);
		ReadBlockPage page =  retrieveReadBlockPage(pageManager.registerPaginator(it));
		page.setNextPageUrl(page.getNextPageUrl().replace("getReadsIterator", "loadPagedReads"));
		return page;
	}




	@RequestMapping(value = "getReadsForMultipleClassIds", method = RequestMethod.GET)
	public @ResponseBody ReadBlockPage getReadsIteratorForListOfClassIds(@RequestParam(value="fileId", required=true) String fileId, @RequestParam(value="minScore", required=false) Float minScore, @RequestParam(value="maxExpected", required=false) Float maxExpected, @RequestParam(value="classification", required=true) String classification, @RequestParam(value="classIds", required=true) Integer[] classIds, @RequestParam(value="dataSelection", required=false) String[] dataSelection) throws IOException {
		DataSelection dataSel = null;
		if(dataSelection == null){
			dataSel= DataSelection.getSelectionForCreation(true, true, true, true,true);
		}else{
			dataSel = DataSelectionSerializer.deserializeDataSelection(dataSelection);
		}
		if(minScore == null){
			minScore = 0f;
		}
		if(maxExpected == null){
			maxExpected = 1000000f;
		}
		IConnector connector = rma3FileHandler.getIConnector(fileId);
		IReadBlockIterator it = connector.getReadsIteratorForListOfClassIds(classification, Arrays.asList(classIds), minScore, maxExpected, dataSel);
		ReadBlockPage page =  retrieveReadBlockPage(pageManager.registerPaginator(it));
		page.setNextPageUrl(page.getNextPageUrl().replace("getReadsForMultipleClassIds", "loadPagedReads"));
		return page;
	}


	@RequestMapping(value = "getClassificationSize", method = RequestMethod.GET)
	public  @ResponseBody int getClassificationSize(@RequestParam(value="fileId", required=true) String fileId, @RequestParam(value="classification", required=true) String classificationName) throws IOException{
		IConnector connector = rma3FileHandler.getIConnector(fileId);
		return connector.getClassificationSize(classificationName);
	}

	@RequestMapping(value = "getClassSize", method = RequestMethod.GET)
	public  @ResponseBody int getClassSize(@RequestParam(value="fileId", required=true) String fileId, @RequestParam(value="classification", required=true) String classificationName, @RequestParam(value="classId", required=true) int classId) throws IOException{
		IConnector connector = rma3FileHandler.getIConnector(fileId);
		return connector.getClassSize(classificationName, classId);
	}


	@RequestMapping(value = "getFindAllReadsIterator", method = RequestMethod.GET)
	public @ResponseBody ReadBlockPage getFindAllReadsIterator(@RequestParam(value="fileId", required=true) String fileId, @RequestParam(value="regEx", required=true) String regEx, @RequestParam(value="findSelection", required=false) String[] findSelection) throws IOException{
		FindSelection findSel = null;
		if(findSelection == null){
			findSel = new FindSelection();
			findSel.useMatchText = true;
			findSel.useReadHeader = true;
			findSel.useReadName = true;
			findSel.useReadSequence = true;
		}else{
			findSel = DataSelectionSerializer.deserializeFindSelection(findSelection);
		}
		IConnector connector = rma3FileHandler.getIConnector(fileId);
		IReadBlockIterator it = connector.getFindAllReadsIterator(regEx, findSel);
		ReadBlockPage page =  retrieveReadBlockPage(pageManager.registerPaginator(it));
		page.setNextPageUrl(page.getNextPageUrl().replace("getFindAllReadsIterator", "loadPagedReads"));
		return page;

	}
	@RequestMapping(value = "getNumberOfReads", method = RequestMethod.GET)
	public @ResponseBody int getNumberOfReads(@RequestParam(value="fileId", required=true) String fileId) throws IOException{
		IConnector connector = rma3FileHandler.getIConnector(fileId);
		return connector.getNumberOfReads();
	}

	@RequestMapping(value = "getNumberOfMatches", method = RequestMethod.GET)
	public @ResponseBody int getNumberOfMatches(@RequestParam(value="fileId", required=true) String fileId) throws IOException{
		IConnector connector = rma3FileHandler.getIConnector(fileId);
		return connector.getNumberOfMatches();
	}

	@RequestMapping(value={"", "/", "help"}, method = RequestMethod.GET)
	public @ResponseBody Map<String, Map<String, Object>> help() throws IOException{
		return RMAControllerMappings.REQUESTS;
	}

	@RequestMapping(value={"info"}, method = RequestMethod.GET)
	public @ResponseBody String info() throws IOException{
		return Info.getInstance();
	}

	@RequestMapping(value = "getRead", method = RequestMethod.GET)
	public @ResponseBody ReadBlockServer getReadsBlock(@RequestParam(value="fileId", required=true) String fileId, @RequestParam(value="readUid", required=true) long readUid, @RequestParam(value="minScore", required=false) Float minScore, @RequestParam(value="maxExpected", required=false) Float maxExpected, @RequestParam(value="dataSelection", required=false) String[] dataSelection) throws IOException {
		DataSelection dataSel = null;
		if(dataSelection == null){
			dataSel= DataSelection.getSelectionForCreation(true, true, true, true,true);
		}else{
			dataSel = DataSelectionSerializer.deserializeDataSelection(dataSelection);
		}
		if(minScore == null){
			minScore = 0f;
		}
		if(maxExpected == null){
			maxExpected = 1000000f;
		}
		IConnector connector = rma3FileHandler.getIConnector(fileId);
		IReadBlockGetter getter =  connector.getReadBlockGetter(minScore, maxExpected, dataSel);
		ReadBlockServer s =  new ReadBlockServer(getter.getReadBlock(readUid));
		getter.close();
		return s;
	}


	@RequestMapping(value = "loadPagedReads", method = RequestMethod.GET)
	public @ResponseBody ReadBlockPage retrieveReadBlockPage(@RequestParam(value="pageId", required=true)String pageId){
		ReadBlockPage page = pageManager.retrieveReadBlockPage(pageId);
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if(null != requestAttributes && requestAttributes instanceof ServletRequestAttributes) {
			HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
			String url = request.getRequestURL().toString();
			page.setNextPageUrl(url + "?pageId="+page.getNextPageToken());
		}
		return page;
	}
	@RequestMapping(value = "About", method = RequestMethod.GET)
	public @ResponseBody About getAbout(){
		return new About();
	}


	@Secured("ROLE_ADMIN")
	@RequestMapping(value = "admin/getLog", method = RequestMethod.GET)
	public @ResponseBody String[] getLog() throws IOException{
		return StringLogger.logEntries.toArray(new String[0]);
	}


	@Secured("ROLE_ADMIN")
	@RequestMapping(value = "admin/updateDatasets", method = RequestMethod.GET)
	public @ResponseBody String updateDatasets() throws IOException{
		long now = System.currentTimeMillis();
		rma3FileHandler.updateFilesystem();
		cache.clear();
		now = System.currentTimeMillis() - now;
		//populate cache
		getAllDatasets(false);
		return String.format("Updated Filesystem. %d datasets have been found. Updating required %d ms.", rma3FileHandler.getAllDatasets().length, now);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(value = "admin/addUser", method = RequestMethod.GET)
	public @ResponseBody String addUser(@RequestParam(value="userName", required=true)String userName, @RequestParam(value="password", required=true)String userPassword, @RequestParam(value="isAdmin", required=false)Boolean isAdmin){
		if(isAdmin == null){
			isAdmin = false;
		}
		textFileAuthentication.createUser(new User(userName, userPassword, textFileAuthentication.getAuthorities(true, isAdmin)));
		return "Created user: " + userName;
	}
	@Secured("ROLE_ADMIN")
	@RequestMapping(value = "admin/removeUser", method = RequestMethod.GET)
	public void removeUser(@RequestParam(value="userName", required=true)String userName){
		textFileAuthentication.deleteUser(userName);
	}
	@Secured("ROLE_ADMIN")
	@RequestMapping(value = "admin/listUsers", method = RequestMethod.GET)
	public  @ResponseBody  String[] listUsers(){
		return textFileAuthentication.getUserList();
	}



}
