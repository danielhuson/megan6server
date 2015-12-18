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


/**Simple about class
 * 
 * @author Hans-Joachim Ruscheweyh
 * 5:33:27 PM - Nov 1, 2014
 *
 */
public class About {
	
	private final String applicationName;
	private final String author;
	private final String contact;
	private final String version;
	private final String versionDate;
	private final String description;
	public About() {
		this.applicationName = "MeganServer";
		this.author = "Hans-Joachim Ruscheweyh";
		this.contact = "hans-joachim.ruscheweyh@id.ethz.ch";
		this.version = "1.0.0";
		this.description = "A web resource for MEGAN datasets providing secure access to your sequencing data.";
		this.versionDate = "2015-12-18";
	}

	public String getVersionDate() {
		return versionDate;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public String getAuthor() {
		return author;
	}

	public String getContact() {
		return contact;
	}

	public String getVersion() {
		return version;
	}

	public String getDescription() {
		return description;
	}

	
	
	
}
