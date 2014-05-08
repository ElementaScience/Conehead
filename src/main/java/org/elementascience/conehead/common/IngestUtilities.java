package org.elementascience.conehead.common;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tylerbrown on 4/30/14.
 */
public class IngestUtilities
{
	static String prefixRegex	 	= "elementa\\.\\d\\d\\d\\d\\d\\d";

	static String revisionRegex 	= "(_[A-Z]{1}\\d+)*";
	static String powerRankingRegex = "(_[A-Z]{1}\\d+)";		// power rankings are the "d" in the regex thus they *have* to be present

	static String tifRegex 			= "[eft]\\d\\d\\d\\.tif";
	static String supplementalRegex = "[s]\\d\\d\\d\\..+";
	static String elementIDRegex	= "[sfet]\\d\\d\\d";

	static String dotRegex 			= "\\.";

	static List<String> optionalExtensions = new ArrayList<String>();
	static
	{
		optionalExtensions.add("epub");
		optionalExtensions.add("json");
		optionalExtensions.add("mobi");
		optionalExtensions.add("pdf");
	}

	static String analyzeFilenames(List<String> filenames)
	{
		String prefix = getPrefix(filenames);
		if (prefix.length() == 0)
		{
			throw new RuntimeException("No article found.");
		}

		String statusMsg = "";

		statusMsg += lookForArticleFile(filenames, prefix);
		statusMsg += lookForOptionalFiles(filenames, prefix);
		statusMsg += lookForTifFiles(filenames, prefix);
		statusMsg += lookForSupplementalFiles(filenames, prefix);

		return statusMsg;
	}

	static public String getPrefix(List<String> filenames)
	{
		String prefix = "";
		for (String filename : filenames)
		{
			if (filename.matches(prefixRegex + revisionRegex + dotRegex + "xml"))
			{
				prefix = filename.substring(0, 15);
			}
		}
		return prefix;
	}

	private static String lookForOptionalFiles(List<String> filenames, String prefix)
	{
		String statusMsg = "";
		List<String> missingExtensions = new ArrayList<String>(optionalExtensions);
		for (String extension : optionalExtensions)
		{
			for (String filename : filenames)
			{
				String regex = prefix + revisionRegex + dotRegex + extension;
				if (filename.matches(regex))
				{
					missingExtensions.remove(extension);
					statusMsg += "<br>" + "Found \"" + extension + "\" file: \"" + filename + "\".";
				}
			}
		}

		for (String extension : missingExtensions)
		{
			statusMsg += "<br><b><span style=\"color:#461B7E\">WARNING: </span></b> No \"" + extension + "\" file found.";
		}
		return statusMsg;
	}

	private static String lookForTifFiles(List<String> filenames, String prefix)
	{
		String statusMsg = "";
		for (String filename : filenames)
		{
			String regex = prefix + revisionRegex + dotRegex + tifRegex;
			if (filename.matches(regex))
			{
				statusMsg += "<br>" + "Found \"tif\" file: \"" + filename + "\".";
			}
		}
		return statusMsg;
	}

	private static String lookForArticleFile(List<String> filenames, String prefix)
	{
		String statusMsg = "";
		for (String filename : filenames)
		{
			String regex = prefix + revisionRegex + dotRegex + "xml";
			if (filename.matches(regex))
			{
				if (statusMsg.length() != 0)
				{
					statusMsg += "<br>";
				}
				statusMsg += "Found article file: \"" + filename + "\".";
			}
		}
		return statusMsg;
	}

	private static String lookForSupplementalFiles(List<String> filenames, String prefix)
	{
		String statusMsg = "";
		for (String filename : filenames)
		{
			String temp = prefix + revisionRegex + dotRegex + supplementalRegex;
			if (filename.matches(temp))
			{
				statusMsg += "<br>" + "Found supplemental file: \"" + filename + "\".";
			}
		}
		return statusMsg;
	}

	public static List<String> getFilenamesToZip(List<String> filenames)
	{
		String prefix = getPrefix(filenames);

		List<String> result = getStandardFilenames(filenames, prefix);
		result.addAll(addImageAndSupplementalFilenames(filenames, prefix));

		return result;
	}


	// Get filenames for filenames without the element ID (e.g. the "s123" part preceding the extension

	private static List<String> getStandardFilenames(List<String> filenames, String prefix)
	{
		List<String> result = new ArrayList<String>();
		for (String extension : new String[] {"xml", "pdf", "json", "epub", "mobi"})
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

	private static List<String> addImageAndSupplementalFilenames(List<String> filenames, String prefix)
	{
		List<String> result = new ArrayList<String>();

		Set<String> elementIDs = getElementIDs(filenames);
		for (String elementID : elementIDs)
		{
			TreeMap<Integer, String> maps = new TreeMap<Integer, String>();
			for (String filename : filenames)
			{
				String regex = prefix + revisionRegex + dotRegex + elementID + ".*";
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
			return matchedString.substring(matchedString.length()-4);
		}
		else
		{
			return null;
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

	public static String getExtension(String filename)
	{
		return filename.substring(filename.lastIndexOf("."));
	}
}
