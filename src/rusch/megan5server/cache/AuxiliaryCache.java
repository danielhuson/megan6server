package rusch.megan5server.cache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import megan.data.IConnector;
import rusch.megan5server.RMAFileHandler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


/**A caching class so that this simple command does not slow down important I/O
 * 
 * @author Hans-Joachim Ruscheweyh
 * 2:47:26 PM - Dec 23, 2014
 *
 */
public class AuxiliaryCache {
	
	private final Cache<Integer,Map<String, String>> auxCache;


	public AuxiliaryCache(){
		auxCache = CacheBuilder.newBuilder().maximumSize(10000).build();
	}
	/**Get Auxiliary Block. If possible, use the cache. Otherwise load from file but store it to the cache for later reference.
	 * 
	 * @param rma3FileHandler
	 * @param fileId
	 * @return
	 * @throws IOException
	 */
	public Map<String, String> getAuxBlock(RMAFileHandler rma3FileHandler, String fileId) throws IOException{
		Map<String, String> aux = auxCache.getIfPresent(rma3FileHandler.resolveFileIdentifierToId(fileId));
		if(aux == null){
			IConnector connector = rma3FileHandler.getIConnector(fileId);
			Map<String, byte[]> map = connector.getAuxiliaryData();
			Map<String, String> map2 = new HashMap<String, String>();
			for(Entry<String, byte[]> entry : map.entrySet()){
				map2.put(entry.getKey(), new String(entry.getValue()));
			}
			auxCache.put(rma3FileHandler.resolveFileIdentifierToId(fileId), map2);
			return map2;
		}else{
			return aux;
		}
	}

	public synchronized void clear() {
		auxCache.invalidateAll();
	}


}
