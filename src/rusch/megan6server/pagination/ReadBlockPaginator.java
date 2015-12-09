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
package rusch.megan6server.pagination;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import megan.data.IReadBlockIterator;
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
	private IReadBlockIterator iterator;
	private long lastAccessed;
	private long timeout;
	private int blockSize;
	private boolean isClosed = false;
	private long totalNumberOfReads = 0;
	private long previouslySeenReads = 0;
	private String[] classnames;

	public ReadBlockPaginator(IReadBlockIterator it, long timeout, int blockSize, String[] classnames){
		iterator = it;
		lastAccessed = System.currentTimeMillis();
		this.timeout = timeout;
		this.blockSize = blockSize;
		this.totalNumberOfReads = it.getMaximumProgress();
		this.classnames = classnames;
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
		iterator = null;
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
			String code = String.valueOf((String.valueOf(Math.abs((rbs.hashCode() + System.currentTimeMillis() + "MySaltIsMyNameMEGANServer").hashCode()))));
			int pos = 0;
			while(iterator.hasNext()){
				pos++;
				rbs.add(new ReadBlockServer(iterator.next(), this.classnames));
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
