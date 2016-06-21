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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;


/**Logger that writes log entries to a {@link List} so that we can give it out via REST
 * 
 * @author Hans-Joachim Ruscheweyh
 * 11:47:12 AM - Nov 2, 2014
 *
 */
public class StringLogger extends AppenderSkeleton{
	public static final List<String> logEntries = new ArrayList<String>();
	public static final SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

	@Override
	public void close() {
		logEntries.clear();

	}

	@Override
	public boolean requiresLayout() {
		return true;
	}

	@Override
	protected void append(LoggingEvent event) {
		StringBuilder logEvent = new StringBuilder(this.layout.format(event));
		String[] s = event.getThrowableStrRep();
		if (s != null) {
			logEvent.append(Layout.LINE_SEP);
			int len = s.length;
			for(int i = 0; i < len; i++) {
				logEvent.append(s[i]);
				logEvent.append(Layout.LINE_SEP);
			}
		}
		logEntries.add(logEvent.toString());
	}








}
