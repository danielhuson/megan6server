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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**Simple info implementation. requires a text file info.txt to be present in the classpath
 * 
 * @author Hans-Joachim Ruscheweyh
 * 2:16:27 PM - Jan 27, 2015
 *
 */
public class Info {

	private static String info;
	private static final Logger logger = LoggerFactory.getLogger(Info.class);

	public synchronized static String getInstance() {

		if(info == null){
			try{
				InputStream input = Info.class.getClassLoader().getResourceAsStream("info.txt");
				if(input == null){
					input = Info.class.getClassLoader().getResourceAsStream("/info.txt");
				}

                info = "";

                if(input!=null)
                {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    String aLine;
                    while ((aLine = reader.readLine()) != null) {
                        info += "\n" + aLine;
                    }
                    reader.close();
                }
			}catch(IOException e){
				logger.error("error loading info.txt", e);
			}
		}
		return info;
	}




}
