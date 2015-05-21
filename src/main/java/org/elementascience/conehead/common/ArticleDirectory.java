package org.elementascience.conehead.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by tylerbrown on 5/12/14.
 */
public class ArticleDirectory
{
	static final String prefixRegex = "elementa\\.\\d\\d\\d\\d\\d\\d";
	static final String revisionRegex = "(_[A-Z]{1}\\d+)*";
	static final String dotRegex = "\\.";
	static final String powerRankingRegex = "(_[A-Z]{1}\\d+)";        // power rankings are the "d" in the regex thus they *have* to be present
	static final String elementIDRegex = "[sfetb]\\d\\d\\d";
	static final String validExtension = dotRegex + "[a-z]{3,4}";

	static final String warningColor = "#461B7E";


	static List<String> optionalExtensions = new ArrayList<String>();
	static
	{
		optionalExtensions.add("epub");
		optionalExtensions.add("json");
		optionalExtensions.add("mobi");
		optionalExtensions.add("pdf");
	}

	private File directory;
	private List<String> filenames;
	private String prefix;
	private Publisher publisher;

	public ArticleDirectory(File directory)
	{
		this.publisher = new Publisher() {public void publishMessage(String message) {System.out.println(message);}};
		BuildArticleDirectory(directory);
	}

	public ArticleDirectory(File directory, Publisher publisher)
	{
		this.publisher = publisher;
		BuildArticleDirectory(directory);
	}

	private void BuildArticleDirectory(File directory)
	{
		this.directory = directory;
		this.filenames = Arrays.asList(directory.list());
		this.prefix = findPrefix();
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	public String getPrefix()
	{
		return prefix;
	}

	private String findPrefix()
	{
		String regex = prefixRegex + revisionRegex + dotRegex + "xml";

		List<String> articleFilenames = new ArrayList<String>();
		Set<String> articleIDs = new HashSet<String>();
		for (String filename : filenames)
		{
			if (filename.matches(regex))
			{
				articleIDs.add(getArticleID(filename));
				articleFilenames.add(filename);
			}
		}

		if (articleIDs.size() == 0)
		{
			throw new RuntimeException("No article found.");
		}
		else if (articleIDs.size() > 1)
		{
			String listOfFilenames = "";
			for (int i = 0; i < articleFilenames.size(); i++)
			{
				if (i > 0)
				{
					listOfFilenames += ", ";
				}
				listOfFilenames += articleFilenames.get(i);

			}
			throw new RuntimeException("Multiple articles found: [" + listOfFilenames + "]");
		}
		else
		{
			return articleFilenames.get(0).substring(0, 15);
		}
	}


	public List<String> getFilenamesToZip()
	{
		List<String> result;

		result = getStandardFilenames();
		result.addAll(addImageAndSupplementalFilenames());

		return result;
	}

	// Get filenames for filenames without the element ID (e.g. the "s123" part preceding the extension

	private List<String> getStandardFilenames()
	{
		List<String> result = new ArrayList<String>();
		for (String extension : new String[]{"xml", "pdf", "json", "epub", "mobi"})
		{
			TreeMap<Integer, String> maps = new TreeMap<Integer, String>();
			for (String filename : filenames)
			{
				String regex = prefix + revisionRegex + dotRegex + extension;
				if (filename.matches(regex))
				{
					Integer powerRanking = getPowerRanking(filename);
					maps.put(powerRanking, filename);
				}
			}

			if (maps.keySet().size() > 0)
			{
				Integer biggest = maps.lastKey();
				String filename = maps.get(biggest);
				result.add(filename);
			}
		}
		return result;
	}

	private List<String> addImageAndSupplementalFilenames()
	{
		List<String> result = new ArrayList<String>();

		Set<String> elementIDs = getElementIDs(filenames);
		for (String elementID : elementIDs)
		{
			TreeMap<Integer, String> maps = new TreeMap<Integer, String>();
			for (String filename : filenames)
			{
				String regex = prefix + revisionRegex + dotRegex + elementID + validExtension;
				if (filename.matches(regex))
				{
					Integer powerRanking = getPowerRanking(filename);
					maps.put(powerRanking, filename);
				}
			}

			if (maps.keySet().size() > 0)
			{
				result.add(maps.get(maps.lastKey()));
			}
		}

		return result;
	}

	private static Set<String> getElementIDs(List<String> filenames)
	{
		Set<String> elementIDs = new HashSet<String>();
		for (String filename : filenames)
		{
			String elementID = getElementID(filename);
			if (elementID != null)
			{
				elementIDs.add(elementID);
			}
		}
		return elementIDs;
	}

	static String getElementID(String filename)
	{
		Pattern pattern = Pattern.compile(prefixRegex + revisionRegex + dotRegex + elementIDRegex);
		Matcher matcher = pattern.matcher(filename);
		if (matcher.find())
		{
			String matchedString = matcher.group();
			return matchedString.substring(matchedString.length() - 4);
		}
		else
		{
			return null;
		}
	}

	public static String makeZipFilename(String filename)
	{
		String zipFilename;

		Pattern pattern = Pattern.compile(prefixRegex);
		Matcher matcher = pattern.matcher(filename);
		if (matcher.find())
		{
			zipFilename = matcher.group();
			String elementID = getElementID(filename);
			if (elementID != null)
			{
				zipFilename += "." + elementID;
			}

			return zipFilename + getExtension(filename);
		}
		else
		{
			throw new RuntimeException("Illegal filename [" + filename + "] passed to \"makeZipFilename\"");
		}
	}

	public File BuildZipFile()
	{
		File result = null;
		try
		{
			File temp = File.createTempFile("AmbraUploader", ".zip");
			temp.deleteOnExit();

			FileOutputStream fos = new FileOutputStream(temp);
			ZipOutputStream zos = new ZipOutputStream(fos);
			zos.setLevel(9);

			addDirToArchive(zos);

			// close the ZipOutputStream
			zos.close();
			result = temp;
		} catch (IOException ioe)
		{
			publisher.publishMessage("Error creating zip file: " + ioe);
		}
		return result;
	}

	private void addDirToArchive(ZipOutputStream zos)
	{
		publisher.publishMessage("Zipping directory: \"" + directory.getName() + "\"");
		publisher.publishMessage("<br>");

		List<String> filenamesToZip = getFilenamesToZip();

		Set<String> ignoredFilenames = new HashSet<String>();
		for (String filename : directory.list())
		{
			if (filenamesToZip.contains(filename))
			{
				ZipFile(zos, filename);
			}
			else
			{
				ignoredFilenames.add(filename);
			}
		}

		publishWarningsAboutIgnoredFiles(ignoredFilenames);
		publishWarningsAboutMissingOptionalFiles(filenamesToZip);
	}

	private void publishWarningsAboutIgnoredFiles(Set<String> ignoredFilenames)
	{
		publisher.publishMessage("<br>");
		for (String filename : ignoredFilenames)
		{
			publisher.publishMessage("<b><span style=\"color:" + warningColor + "\">WARNING: </span></b>Ignoring file: \"" + filename + "\".");
		}
	}

	private void publishWarningsAboutMissingOptionalFiles(List<String> filenamesToZip)
	{
		for (String extension : optionalExtensions)
		{
			boolean found = false;
			String regex = ".*" + extension;
			for (String filename : filenamesToZip)
			{
				if (filename.matches(regex))
				{
					found = true;
				}
			}

			if (!found)
			{
				publisher.publishMessage("<b><span style=\"color:" + warningColor + "\">WARNING: </span></b>No " + extension + " file found.");
			}

		}
	}

	private void ZipFile(ZipOutputStream zos, String filenameToZip)
	{
		try
		{
			String standardizedFilename = makeZipFilename(filenameToZip);

			publisher.publishMessage("Adding file: \"" + filenameToZip + "\".");

			String fullPath = directory.getAbsolutePath() + File.separator + filenameToZip;
			File file = new File(fullPath);

			byte[] buffer = new byte[2048];
			FileInputStream fis = new FileInputStream(file);
			zos.putNextEntry(new ZipEntry(standardizedFilename));

			int length;
			while ((length = fis.read(buffer)) > 0)
			{
				zos.write(buffer, 0, length);
			}

			zos.closeEntry();
			fis.close();

		} catch (IOException ioe)
		{
			publisher.publishMessage("IOException :" + ioe);
		}
	}



	static public String getExtension(String filename)
	{
		return filename.substring(filename.lastIndexOf("."));
	}

	public String getArticleID()
	{
		return getArticleID(prefix);
	}

	public static String getArticleID(String filename)
	{
		Pattern pattern = Pattern.compile(prefixRegex);
		Matcher matcher = pattern.matcher(filename);
		if (matcher.find())
		{
			return matcher.group().substring(prefixRegex.indexOf("."));
		}
		else
		{
			throw new RuntimeException("Unable to extract article ID from \"" + filename + "\"");
		}
	}

	public static Integer getPowerRanking(String filename)
	{
		Pattern pattern = Pattern.compile(prefixRegex + powerRankingRegex);
		Matcher matcher = pattern.matcher(filename);
		if (matcher.find())
		{
			String powerRanking = matcher.group();
			return new Integer(powerRanking.substring(17));
		}
		else
		{
			return 0;
		}
	}

}
