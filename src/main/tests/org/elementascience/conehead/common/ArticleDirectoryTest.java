package org.elementascience.conehead.common;

import junit.framework.TestCase;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArticleDirectoryTest
{
	private Path directory;

	@Before
	public void setUp() throws Exception
	{
		directory = Files.createTempDirectory("ArticleDirectoryTest");
		directory.toFile().deleteOnExit();

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
	// analyzeFilenames
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	@Test
	public void testSingleArticleFile() throws IOException
	{
		CreateFile("elementa.000017.xml");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatSingleXMLFileWithRevisionIsOK() throws Exception
	{
		CreateFile("elementa.000017_V10.xml");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		String successMsg =
				"Found article file: \"elementa.000017_V10.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatWeAllowMultipleVersionsOfXMLFile() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017_R1.xml");
		CreateFile("elementa.000017_R99.xml");
		CreateFile("elementa.000017_R732.xml");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());


		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"Found article file: \"elementa.000017_R1.xml\".<br>" +
						"Found article file: \"elementa.000017_R732.xml\".<br>" +
						"Found article file: \"elementa.000017_R99.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	// The "non-matching" PDF file should be ignored and we'll report that no PDF file was included

	@Test
	public void testThatPrefixesOfDifferentTypeFilesAreTheSame() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017.epub");
		CreateFile("elementa.000017.json");
		CreateFile("elementa.000017.mobi");
		CreateFile("elementa.000018.pdf");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());


		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"Found \"epub\" file: \"elementa.000017.epub\".<br>" +
						"Found \"json\" file: \"elementa.000017.json\".<br>" +
						"Found \"mobi\" file: \"elementa.000017.mobi\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatWeDetectUnrecognizedFileTypes() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017.dog");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	// All the files can include a "revision number" following the article number of the form "_[A-Z]<integer>"

	@Test
	public void testThatSingleXMLFileWithLegalRevisionIsOK() throws Exception
	{
		CreateFile("elementa.000017_R5.xml");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		String successMsg =
				"Found article file: \"elementa.000017_R5.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatWeAllowMultipleVersionsOfDifferentFiles() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017_R1.xml");
		CreateFile("elementa.000017.pdf");
		CreateFile("elementa.000017_V1.pdf");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"Found article file: \"elementa.000017_R1.xml\".<br>" +
						"Found \"pdf\" file: \"elementa.000017.pdf\".<br>" +
						"Found \"pdf\" file: \"elementa.000017_V1.pdf\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatWeRequireProperFormattingForMultipleVersionsOfXMLFile() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017_A.xml");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatSingleXMLFileWithIllegalRevisionIsOK() throws Exception
	{
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("No article found.");

		CreateFile("elementa.000017_R5a.xml");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		articleDir.analyzeFilenames();
	}

	@Test
	public void testThatWeAreSomewhatCaseSensitive() throws Exception
	{
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("No article found.");

		CreateFile("elementa.000017.XML");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		articleDir.analyzeFilenames();
	}

	@Test
	public void testThatWeRequireElementaFilename() throws Exception
	{
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("No article found.");

		CreateFile("notelementa.000017.xml");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		articleDir.analyzeFilenames();
	}

	@Test
	public void testThatWeDetectIllegalXMLFilenames() throws Exception
	{
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("No article found.");

		CreateFile("elementa.000017.xml.xml");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		articleDir.analyzeFilenames();
	}

	// Real life directories often have these files. Just verify they don't cause a problem and are ignored like any
	// other unrecognized file

	@Test
	public void testThatWeIgnoreFilesThatStartWithADot() throws Exception
	{
		CreateFile(".Dogmeat");
		CreateFile("elementa.000017.xml");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());


		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatFullPayloadIsOK() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017.epub");
		CreateFile("elementa.000017.mobi");
		CreateFile("elementa.000017.pdf");
		CreateFile("elementa.000017.json");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());


		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"Found \"epub\" file: \"elementa.000017.epub\".<br>" +
						"Found \"json\" file: \"elementa.000017.json\".<br>" +
						"Found \"mobi\" file: \"elementa.000017.mobi\".<br>" +
						"Found \"pdf\" file: \"elementa.000017.pdf\".";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatFullPayloadWithDifferentRevisionsIsOK() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017_R7.xml");
		CreateFile("elementa.000017.epub");
		CreateFile("elementa.000017_R5.epub");
		CreateFile("elementa.000017.mobi");
		CreateFile("elementa.000017_R3.mobi");
		CreateFile("elementa.000017.pdf");
		CreateFile("elementa.000017_R1.pdf");
		CreateFile("elementa.000017_R2.pdf");
		CreateFile("elementa.000017.json");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"Found article file: \"elementa.000017_R7.xml\".<br>" +
						"Found \"epub\" file: \"elementa.000017.epub\".<br>" +
						"Found \"epub\" file: \"elementa.000017_R5.epub\".<br>" +
						"Found \"json\" file: \"elementa.000017.json\".<br>" +
						"Found \"mobi\" file: \"elementa.000017.mobi\".<br>" +
						"Found \"mobi\" file: \"elementa.000017_R3.mobi\".<br>" +
						"Found \"pdf\" file: \"elementa.000017.pdf\".<br>" +
						"Found \"pdf\" file: \"elementa.000017_R1.pdf\".<br>" +
						"Found \"pdf\" file: \"elementa.000017_R2.pdf\".";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	// The directory can include TIFF files. They must have the case sensitive extension of "tif". They must include
	// an element ID of the form "[eft]<3 digit integer>" and are required.

	@Test
	public void testThatTIFFilesAreOK() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017.e123.tif");
		CreateFile("elementa.000017.f123.tif");
		CreateFile("elementa.000017.t123.tif");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());


		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.<br>" +
						"Found \"tif\" file: \"elementa.000017.e123.tif\".<br>" +
						"Found \"tif\" file: \"elementa.000017.f123.tif\".<br>" +
						"Found \"tif\" file: \"elementa.000017.t123.tif\".";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatTIFFilesMustBeWellFormedWrongNumberOfDigits() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017.e1234.tif");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatTIFFilesMustBeWellFormed() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017.e123.tiffffff.tif");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatTIFFilesMustBeWellFormedBadPrefix() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017.x123.tif");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	// The directory can include "supplementary files" which have an element ID of the form "s<3 digit integer>".
	// Supplementary files can have any extension

	@Test
	public void testThatSupplementalFilesTIFFAreOK() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017.s123.tif");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.<br>" +
						"Found supplemental file: \"elementa.000017.s123.tif\".";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatSupplementalFilesAreOK() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017.s123.dog");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.<br>" +
						"Found supplemental file: \"elementa.000017.s123.dog\".";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatSupplementalFilesAreWellFormedTooManyDigits() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017.s1234.dog");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatSupplementalFilesAreWellFormedBadPrefix() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017.x123.dog");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void supplementalFilesShouldHaveAnExtension() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017.s123");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void supplementalFilesShouldHaveANonEmptyExtension() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017.s123.");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = articleDir.analyzeFilenames();
		Assert.assertEquals(successMsg, statusMsg);
	}

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	// getFilenamesToZip
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	@Test
	public void testThatWeHarvestLastRevisionSimple() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017_V1.xml");
		CreateFile(".Dogmeat");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		List<String> expectedFilenames = new ArrayList<String>();
		expectedFilenames.add("elementa.000017_V1.xml");

		List<String> filesToZip = articleDir.getFilenamesToZip();
		Assert.assertTrue(SameLists(expectedFilenames, filesToZip));
	}

	@Test
	public void weShouldIgnoreMalformedNames() throws Exception
	{
		CreateFile("elementa.000017.xml");
		CreateFile("elementa.000017_V1.xml");
		CreateFile("elementa.000017.s123.tiffffff.tif");
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		List<String> expectedFilenames = new ArrayList<String>();
		expectedFilenames.add("elementa.000017_V1.xml");

		List<String> filesToZip = articleDir.getFilenamesToZip();
		Assert.assertTrue(SameLists(expectedFilenames, filesToZip));
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
		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		List<String> expectedFilenames = new ArrayList<String>();
		expectedFilenames.add("elementa.000017_V7.xml");
		expectedFilenames.add("elementa.000017_R5.epub");
		expectedFilenames.add("elementa.000017_V3.mobi");
		expectedFilenames.add("elementa.000017_V2.pdf");
		expectedFilenames.add("elementa.000017.json");

		List<String> filesToZip = articleDir.getFilenamesToZip();
		Assert.assertTrue(SameLists(expectedFilenames, filesToZip));
	}


	@Test
	public void testThatWeHarvestLastRevisionSupplementaryData() throws Exception
	{
		CreateFile("elementa.000017_R1.xml");

		CreateFile("elementa.000017_R7.pdf");
		CreateFile("elementa.000017_R6.pdf");

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

		ArticleDirectory articleDir = new ArticleDirectory(directory.toFile());

		List<String> expectedFilenames = new ArrayList<String>();
		expectedFilenames.add("elementa.000017_R1.xml");
		expectedFilenames.add("elementa.000017_R7.pdf");
		expectedFilenames.add("elementa.000017_R130.mobi");
		expectedFilenames.add("elementa.000017_R166.json");
		expectedFilenames.add("elementa.000017_R7.s123.tif");
		expectedFilenames.add("elementa.000017_R8.s321.ppt");

		List<String> filesToZip = articleDir.getFilenamesToZip();
		Assert.assertTrue(SameLists(expectedFilenames, filesToZip));
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