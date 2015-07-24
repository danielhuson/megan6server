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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**A Servletfilter that logs all activity in the server. We log user, url and the time the request took to perform.
 * 
 * @author Hans-Joachim Ruscheweyh
 * 3:54:58 PM - Nov 1, 2014
 *
 */
public class LoggingFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		long time = System.currentTimeMillis();
		String queryString = ((HttpServletRequest)request).getQueryString();
		String requestString = ((HttpServletRequest)request).getRequestURL().toString() +( queryString == null ? ("") : ("?"+queryString));
		String user = ((HttpServletRequest)request).getRemoteUser();
		try{
			chain.doFilter(request, response);
		}catch(Throwable t){
			logger.error("Error requesting: " + requestString, t);
			throw new IOException(t);
		}finally{
			long now = System.currentTimeMillis();
			long took = now-time;
			logger.info(String.format("User '%s' requested url '%s'. The request took '%s' milliseconds", user, requestString, took));
		}

	}

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

}
