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
		this.version = "1.0.1";
		this.description = "A web resource for MEGAN datasets providing secure access to your sequencing data.";
		this.versionDate = "2015-06-12";
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
