package org.elementascience.conehead.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class ArticleDirectoryTest
{
	private Path directory;
	private TestPublisher testPublisher;

	class TestPublisher implements Publisher
	{
		private String publishedMessage = "";

		@Override
		public void publishMessage(String message)
		{
			publishedMessage += message;
		}

		String getPublishedMessage()
		{
			return publishedMessage;
		}
	}

	@Before
	public void setUp() throws Exception
	{
		directory = Files.createTempDirectory("ArticleDirectoryTest");
		directory.toFile().deleteOnExit();

		testPublisher = new TestPublisher();
	}

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Constructor
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	@Test
	public void testThatMultipleArticlesThrows() throws IOException
	{
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("Multiple articles found: [elementa.000017.xml, elementa.000018.xml]");

		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000018.xml");
		new ArticleDirectory(directory.toFile());
	}

	@Test
	public void testThatWeRequireAProperlyNamedXMLFile() throws Exception
	{
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("No article found.");

		CreateFile("elementa.17.xml");
		new ArticleDirectory(directory.toFile());
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Static methods
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	@Test
	public void testGetPrefix() throws IOException
	{
		CreateFile("elementa.000042.xml");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		Assert.assertEquals("elementa.000042", articleDir.getPrefix());
	}

	@Test
	public void testGetPrefixWithRevision() throws IOException
	{
		CreateFile("elementa.000042_R5.xml");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		Assert.assertEquals("elementa.000042", articleDir.getPrefix());
	}


	@Test
	public void testGetArticleID() throws IOException
	{
		CreateFile("elementa.000042.xml");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		Assert.assertEquals("000042", articleDir.getArticleID());
	}

	@Test
	public void testGetArticleIDWithRevision() throws IOException
	{
		CreateFile("elementa.000043_R9.xml");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		Assert.assertEquals("000043", articleDir.getArticleID());

		String articleNumber = String.valueOf(Integer.parseInt(articleDir.getArticleID()));
		Assert.assertEquals("43", articleNumber);
	}




	@Test
	public void testElementID() throws Exception
	{
		String filename = "elementa.000017.s123.ppt";
		Assert.assertEquals("s123", ArticleDirectory.getElementID(filename));
	}

	@Test
	public void testElementIDWithRevision() throws Exception
	{
		String filename = "elementa.000017_V5.s123.ppt";
		Assert.assertEquals("s123", ArticleDirectory.getElementID(filename));
	}


	@Test
	public void testGetPowerRankingWithoutPowerRanking() throws Exception
	{
		String filename = "elementa.000017.xml";
		Assert.assertEquals((Integer) 0, ArticleDirectory.getPowerRanking(filename));
	}

	@Test
	public void testGetPowerRankingWithPowerRanking() throws Exception
	{
		String filename = "elementa.000017_R7.xml";
		Assert.assertEquals((Integer) 7, ArticleDirectory.getPowerRanking(filename));
	}

	@Test
	public void testGetPowerRankingWithPowerRankingMultipleDigits() throws Exception
	{
		String filename = "elementa.000017_R704.xml";
		Assert.assertEquals((Integer) 704, ArticleDirectory.getPowerRanking(filename));
	}


	@Test
	public void testGettingArticleIDWithRevision() throws Exception
	{
		String filename = "elementa.000017_R5.xml";
		Assert.assertEquals("000017", ArticleDirectory.getArticleID(filename));
	}

	@Test
	public void testGettingArticleIDNonArticleFilename() throws Exception
	{
		String filename = "elementa.000017_R5.s123.ppt";
		Assert.assertEquals("000017", ArticleDirectory.getArticleID(filename));
	}


	@Test
	public void testGetElementID() throws Exception
	{
		String filename = "elementa.000017.s123.ppt";
		Assert.assertEquals("s123", ArticleDirectory.getElementID(filename));
	}

	@Test
	public void testGetElementIDWithRevision() throws Exception
	{
		String filename = "elementa.000017_R7.t321.ppt";
		Assert.assertEquals("t321", ArticleDirectory.getElementID(filename));
	}


	@Test
	public void testMakingZipFilename() throws Exception
	{
		String filename = "elementa.000017.xml";
		Assert.assertEquals("elementa.000017.xml", ArticleDirectory.makeZipFilename(filename));

		filename = "elementa.000017_R5.xml";
		Assert.assertEquals("elementa.000017.xml", ArticleDirectory.makeZipFilename(filename));

		filename = "elementa.000017_V7.mobi";
		Assert.assertEquals("elementa.000017.mobi", ArticleDirectory.makeZipFilename(filename));

		filename = "elementa.000017_V7.e321.tif";
		Assert.assertEquals("elementa.000017.e321.tif", ArticleDirectory.makeZipFilename(filename));

		filename = "elementa.000017_V7.s123.ppt";
		Assert.assertEquals("elementa.000017.s123.ppt", ArticleDirectory.makeZipFilename(filename));
	}

	@Test
	public void testZippingBadFilename() throws Exception
	{
		String filename = "illegalPrefix.000017.xml";

		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("Illegal filename [" + filename + "] passed to \"makeZipFilename\"");

		ArticleDirectory.makeZipFilename(filename);
	}


	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// BuildZip
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	@Test
	public void testThatWeHarvestLastRevisionSimple() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017_V1.xml");
		CreateFile(".Dogmeat");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile(), testPublisher);

		articleDir.BuildZipFile();
		String publishedMessage = testPublisher.getPublishedMessage();

		Assert.assertTrue(publishedMessage.contains("Adding file: \"elementa.000017_V1.xml\"."));

		Assert.assertTrue(publishedMessage.contains("Ignoring file: \"elementa.000017.xml\"."));
		Assert.assertTrue(publishedMessage.contains("Ignoring file: \".Dogmeat\"."));
		Assert.assertTrue(publishedMessage.contains("No epub file found."));
		Assert.assertTrue(publishedMessage.contains("No pdf file found."));
		Assert.assertTrue(publishedMessage.contains("No mobi file found."));
		Assert.assertTrue(publishedMessage.contains("No json file found."));
	}

	@Test
	public void weShouldIgnoreMalformedNames() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017_V1.xml");
		CreateFile("elementa.000017_V1a.xml");              // bad revision
		CreateFile("elementa.000017_V2.XML");               // we're fussy about casing

		CreateFile("elementa.000017.s1234.ppt");            // too many digits
		CreateFile("elementa.000017.x123.tif");             // "x" isn't valid there
		CreateFile("elementa.000017.s123.tiffffff.tif");    // uh... tifffffff?

		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile(), testPublisher);

		articleDir.BuildZipFile();
		String publishedMessage = testPublisher.getPublishedMessage();

		Assert.assertTrue(publishedMessage.contains("Adding file: \"elementa.000017_V1.xml\"."));

		Assert.assertTrue(publishedMessage.contains("Ignoring file: \"elementa.000017.xml\"."));
		Assert.assertTrue(publishedMessage.contains("Ignoring file: \"elementa.000017.xml\"."));
		Assert.assertTrue(publishedMessage.contains("Ignoring file: \"elementa.000017_V1a.xml\"."));
		Assert.assertTrue(publishedMessage.contains("Ignoring file: \"elementa.000017_V2.XML\"."));

		Assert.assertTrue(publishedMessage.contains("Ignoring file: \"elementa.000017.s1234.ppt\"."));
		Assert.assertTrue(publishedMessage.contains("Ignoring file: \"elementa.000017.x123.tif\"."));
		Assert.assertTrue(publishedMessage.contains("Ignoring file: \"elementa.000017.s123.tiffffff.tif\"."));
		Assert.assertTrue(publishedMessage.contains("No epub file found."));
		Assert.assertTrue(publishedMessage.contains("No pdf file found."));
		Assert.assertTrue(publishedMessage.contains("No mobi file found."));
		Assert.assertTrue(publishedMessage.contains("No json file found."));
	}

	@Test
	public void testThatWeHarvestLastRevisionComplex() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017_V7.xml");
		CreateFile("elementa.000017.epub");
		CreateFile("elementa.000017_R5.epub");
		CreateFile("elementa.000017.mobi");
		CreateFile("elementa.000017_V3.mobi");
		CreateFile("elementa.000017.pdf");
		CreateFile("elementa.000017_R1.pdf");
		CreateFile("elementa.000017_V2.pdf");
		CreateFile("elementa.000017.json");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile(), testPublisher);

		articleDir.BuildZipFile();
		String publishedMessage = testPublisher.getPublishedMessage();

		Assert.assertTrue(publishedMessage.contains("Adding file: \"elementa.000017_V7.xml\"."));
		Assert.assertTrue(publishedMessage.contains("Adding file: \"elementa.000017_R5.epub\"."));
		Assert.assertTrue(publishedMessage.contains("Adding file: \"elementa.000017_V3.mobi\"."));
		Assert.assertTrue(publishedMessage.contains("Adding file: \"elementa.000017_V2.pdf\"."));
		Assert.assertTrue(publishedMessage.contains("Adding file: \"elementa.000017.json\"."));
	}


	@Test
	public void testThatWeHarvestLastRevisionSupplementaryData() throws Exception
	{
		CreateFile("elementa.000017_R1.xml");

		CreateFile("elementa.000017_R7.pdf");
		CreateFile("elementa.000017_R6.pdf");
		CreateFile("elementa.000018_R6.pdf");       // Wrong prefix

		CreateFile("elementa.000017_R130.mobi");
		CreateFile("elementa.000017_R66.mobi");

		CreateFile("elementa.000017_R27.json");
		CreateFile("elementa.000017_R166.json");
		CreateFile("elementa.000017_R73.json");
		CreateFile("elementa.000017_R34.json");

		CreateFile("elementa.000017_R1.s123.tif");
		CreateFile("elementa.000017_R7.s123.tif");
		CreateFile("elementa.000017_R3.s123.tif");

		CreateFile("elementa.000017_R8.s321.ppt");
		CreateFile("elementa.000017_R7.s321.ppt");
		CreateFile("elementa.000017_R3.s321.ppt");

		CreateFile("elementa.000017.s666.xlsx");        // 4 character extension

		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile(), testPublisher);

		articleDir.BuildZipFile();
		String publishedMessage = testPublisher.getPublishedMessage();

		Assert.assertTrue(publishedMessage.contains("Adding file: \"elementa.000017_R1.xml\"."));
		Assert.assertTrue(publishedMessage.contains("Adding file: \"elementa.000017_R130.mobi\"."));
		Assert.assertTrue(publishedMessage.contains("Adding file: \"elementa.000017_R7.pdf\"."));
		Assert.assertTrue(publishedMessage.contains("Adding file: \"elementa.000017_R166.json\"."));
		Assert.assertTrue(publishedMessage.contains("Adding file: \"elementa.000017_R7.s123.tif\"."));
		Assert.assertTrue(publishedMessage.contains("Adding file: \"elementa.000017_R8.s321.ppt\"."));

		Assert.assertTrue(publishedMessage.contains("No epub file found."));

		Assert.assertTrue(publishedMessage.contains("Ignoring file: \"elementa.000017_R6.pdf\"."));
		Assert.assertTrue(publishedMessage.contains("Ignoring file: \"elementa.000018_R6.pdf\"."));
		Assert.assertTrue(publishedMessage.contains("Ignoring file: \"elementa.000017_R66.mobi\"."));
		Assert.assertTrue(publishedMessage.contains("Ignoring file: \"elementa.000017_R27.json\"."));
		Assert.assertTrue(publishedMessage.contains("Ignoring file: \"elementa.000017_R73.json\"."));
		Assert.assertTrue(publishedMessage.contains("Ignoring file: \"elementa.000017_R34.json\"."));
		Assert.assertTrue(publishedMessage.contains("Ignoring file: \"elementa.000017_R1.s123.tif\"."));
		Assert.assertTrue(publishedMessage.contains("Ignoring file: \"elementa.000017_R3.s123.tif\"."));

		Assert.assertTrue(publishedMessage.contains("Ignoring file: \"elementa.000017_R3.s321.ppt\"."));
		Assert.assertTrue(publishedMessage.contains("Ignoring file: \"elementa.000017_R7.s321.ppt\"."));
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++






	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// Utilities
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	private void CreateFile(String filename) throws IOException
	{
		Path path = Files.createFile(directory.resolve(filename));
		path.toFile().deleteOnExit();
	}

	private boolean SameLists(List<String> list1, List<String> list2)
	{
		System.out.println("++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("Expected List.");
		for (String s1 : list1)
		{
			System.out.println("\t" + s1);
		}

		System.out.println("Actual List.");
		for (String s2 : list2)
		{
			System.out.println("\t" + s2);
		}

		Collections.sort(list1);
		Collections.sort(list2);

		return list1.equals(list2);
	}


}