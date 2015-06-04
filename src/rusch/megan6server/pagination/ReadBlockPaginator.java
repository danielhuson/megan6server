package rusch.megan6server.pagination;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import megan.rma2.IReadBlockIterator;
import rusch.megan5client.ReadBlockServer;
import rusch.megan5client.connector.ReadBlockPage;


/**A paginator that retrieves pages from an readblockiterator
 * 
 * 
 * @author Hans-Joachim Ruscheweyh
 * 10:20:41 AM - Oct 29, 2014
 *
 */
public class ReadBlockPaginator {
	private final IReadBlockIterator iterator;
	private long lastAccessed;
	private long timeout;
	private int blockSize;
	private boolean isClosed = false;
	private long totalNumberOfReads = 0;
	private long previouslySeenReads = 0;

	public ReadBlockPaginator(IReadBlockIterator it, long timeout, int blockSize){
		iterator = it;
		lastAccessed = System.currentTimeMillis();
		this.timeout = timeout;
		this.blockSize = blockSize;
		this.totalNumberOfReads = it.getMaximumProgress();
	}



	/**Check if data has been retrieve in the last n seconds. n is the specified as timeout in the constructor.
	 * 
	 * @return
	 */
	public boolean isActive(){
		return (System.currentTimeMillis() - lastAccessed) > timeout ? false : true;
	}

	public void close() throws IOException{
		iterator.close();
		isClosed = true;
	}


	/**Retrieve next page
	 * 
	 * @return
	 */
	public ReadBlockPage getNextPage(){
		if(isClosed){
			return new ReadBlockPage();
		}else{
			List<ReadBlockServer> rbs = new ArrayList<ReadBlockServer>();
			String code = String.valueOf((String.valueOf(Math.abs((rbs.hashCode() + System.currentTimeMillis() + "MySaltIsMyNameMEGAN5Server").hashCode()))));
			int pos = 0;
			while(iterator.hasNext()){
				pos++;
				rbs.add(new ReadBlockServer(iterator.next()));
				if(rbs.size() == blockSize){
					break;
				}
			}
			if(rbs.size() == 0 || rbs.size() < blockSize){
				code = null;
			}
			previouslySeenReads = previouslySeenReads + pos;
			lastAccessed = System.currentTimeMillis();
			return new ReadBlockPage(code, null, rbs.toArray(new ReadBlockServer[0]),totalNumberOfReads, (previouslySeenReads - pos));	
		}
	}


	/**Retrieve initial PageId
	 * 
	 * @return
	 */
	public String retrieveInitialPageId() {
		return String.valueOf(iterator.hashCode());
	}


}
