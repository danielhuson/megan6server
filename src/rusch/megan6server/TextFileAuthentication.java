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

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;



/**An implementation to deal with user details. Conform with the needs of the Spring framework
 * 
 * @author Hans-Joachim Ruscheweyh
 * 3:34:00 PM - Nov 7, 2014
 *
 */
public class TextFileAuthentication implements UserDetailsManager{

	private final File credentialsFile;
	private final Map<String, UserDetails> name2tokens;
	public static Logger logger = Logger.getLogger(TextFileAuthentication.class);

	public static final String ROLE_READER = "ROLE_USER";
	public static final String ROLE_ADMIN = "ROLE_ADMIN";

	public TextFileAuthentication() throws IOException{
		URL url= this.getClass().getResource("/credentials.txt");
		if(url==null)
			this.getClass().getResource("megan6server/credentials.txt");

        if(url==null)
			throw new RuntimeException("File not found in path: credentials.txt");

		this.credentialsFile = new File(url.getPath());
		this.name2tokens = new HashMap<String, UserDetails>();
		try {
			load();
		} catch (IOException e) {
			logger.error("Error init textauthentication manager", e);
		}
	}

	/**Load credentials from file
	 * 
	 * @throws IOException
	 */
	private synchronized void load() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(credentialsFile));
		String aLine;
		while((aLine = reader.readLine()) != null){
			if(aLine.startsWith("#")){
				continue;
			}
			if(aLine.length() == 0){
				continue;
			}
			String[] splits = aLine.split("\\t");
			UserDetails user = new User(splits[0], splits[1], getAuthorities(Boolean.valueOf(splits[2]), Boolean.valueOf(splits[3])));
			name2tokens.put(splits[0], user);
		}
		reader.close();
	}

	/**Get Authorities for a set of parameters
	 * 
	 * @param canRead
	 * @param isAdmin
	 * @return
	 */
	public Collection<GrantedAuthority> getAuthorities(boolean canRead, boolean isAdmin){
		Set<GrantedAuthority> list = new HashSet<GrantedAuthority>();
		if(canRead){
			list.add(new SimpleGrantedAuthority(ROLE_READER));
		}

		if(isAdmin){
			list.add(new SimpleGrantedAuthority(ROLE_READER));
			list.add(new SimpleGrantedAuthority(ROLE_ADMIN));
		}
		return list;
	}

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		UserDetails ud = name2tokens.get(username);

		if(ud == null){
			throw new UsernameNotFoundException("No user with name: " + username);
		}
		return new User(ud.getUsername(), ud.getPassword(), ud.getAuthorities());

	}

	@Override
	public void createUser(UserDetails user) {
		updateUser(user);
	}
	public static String md5(String s) throws NoSuchAlgorithmException{
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		messageDigest.reset();
		messageDigest.update(s.getBytes(Charset.forName("UTF8")));
		byte[] resultByte = messageDigest.digest();
		return new String(Hex.encodeHex(resultByte));
	}

	@Override
	public void updateUser(UserDetails user) {
		synchronized (name2tokens) {
			try {
				name2tokens.put(user.getUsername(), new User(user.getUsername(), md5(user.getPassword()), user.getAuthorities()));
			} catch (NoSuchAlgorithmException e) {
				logger.warn("MD5 algorithm not found. Can not happen...");
			}
			persist();
		}

	}


	/**Write credentials file
	 * 
	 */
	private synchronized void persist() {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(credentialsFile));

			writer.write("#UserName\tPassword\tisReader\tisWriter\tisAdmin\n");
			for(UserDetails userDetails : name2tokens.values()){
				writer.write(userDetails.getUsername()+"\t" + userDetails.getPassword() + "\t" + canRead(userDetails.getAuthorities()) + "\t" + isAdmin(userDetails.getAuthorities()) + "\n");
			}
			writer.close();
			load();
		} catch (IOException e) {
			logger.error("persist error", e);
		}

	}
	@Override
	public void deleteUser(String username) {
		synchronized (name2tokens) {
			name2tokens.remove(username);
			persist();
		}
	}

	@Override
	public void changePassword(String oldPassword, String newPassword) {
		throw new AccessDeniedException("Changing password not supported");
	}


	@Override
	public boolean userExists(String username) {
		if(name2tokens.containsKey(username)){
			return true;
		}else{
			return false;
		}
	}

	/**Check auth contains read permission
	 * 
	 * @param authorities
	 * @return
	 */
	public boolean canRead(Collection<? extends GrantedAuthority> authorities) {
		for(GrantedAuthority auth : authorities){
			if(auth.getAuthority().equals(ROLE_READER)){
				return true;
			}
		}
		return false;
	}


	/**Check if auth contains the right to modify users
	 * 
	 * @param authorities
	 * @return
	 */
	public boolean isAdmin(
			Collection<? extends GrantedAuthority> authorities) {
		for(GrantedAuthority auth : authorities){
			if(auth.getAuthority().equals(ROLE_ADMIN)){
				return true;
			}
		}
		return false;
	}

	public String[] getUserList() {
		return name2tokens.keySet().toArray(new String[0]);
	}



}
