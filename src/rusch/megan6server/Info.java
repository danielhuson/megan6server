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
