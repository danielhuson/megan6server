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
