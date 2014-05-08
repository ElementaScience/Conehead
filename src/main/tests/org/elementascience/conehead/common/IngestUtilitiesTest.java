package org.elementascience.conehead.common;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IngestUtilitiesTest
{
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();


	@Test
	public void testThatSingleXMLFileIsOK() throws Exception
	{
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatSingleXMLFileWithRevisionIsOK() throws Exception
	{
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017_V10.xml");

		String successMsg =
				"Found article file: \"elementa.000017_V10.xml\".<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatWeAllowMultipleVersionsOfXMLFile() throws Exception
	{
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");
		filenames.add("elementa.000017_R1.xml");
		filenames.add("elementa.000017_R99.xml");
		filenames.add("elementa.000017_R732.xml");

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"Found article file: \"elementa.000017_R1.xml\".<br>" +
						"Found article file: \"elementa.000017_R99.xml\".<br>" +
						"Found article file: \"elementa.000017_R732.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatWeRequireAProperlyNamedXMLFile() throws Exception
	{
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("No article found.");

		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.17.xml");

		IngestUtilities.analyzeFilenames(filenames);
	}

	// The "non-matching" PDF file should be ignored and we'll report that no PDF file was included

	@Test
	public void testThatPrefixesOfDifferentTypeFilesAreTheSame() throws Exception
	{
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");
		filenames.add("elementa.000017.epub");
		filenames.add("elementa.000017.json");
		filenames.add("elementa.000017.mobi");
		filenames.add("elementa.000018.pdf");

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"Found \"epub\" file: \"elementa.000017.epub\".<br>" +
						"Found \"json\" file: \"elementa.000017.json\".<br>" +
						"Found \"mobi\" file: \"elementa.000017.mobi\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatWeDetectUnrecognizedFileTypes() throws Exception
	{
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");
		filenames.add("elementa.000017.dog");

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	// All the files can include a "revision number" following the article number of the form "_R<integer>"

	@Test
	public void testThatSingleXMLFileWithLegalRevisionIsOK() throws Exception
	{
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017_R5.xml");

		String successMsg =
				"Found article file: \"elementa.000017_R5.xml\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatWeAllowMultipleVersionsOfDifferentFiles() throws Exception
	{
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");
		filenames.add("elementa.000017_R1.xml");
		filenames.add("elementa.000017.pdf");
		filenames.add("elementa.000017_V1.pdf");

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
						"Found article file: \"elementa.000017_R1.xml\".<br>" +
						"Found \"pdf\" file: \"elementa.000017.pdf\".<br>" +
						"Found \"pdf\" file: \"elementa.000017_V1.pdf\".<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
						"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatWeRequireProperFormattingForMultipleVersionsOfXMLFile() throws Exception
	{
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");
		filenames.add("elementa.000017_A.xml");

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatSingleXMLFileWithIllegalRevisionIsOK() throws Exception
	{
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("No article found.");

		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017_R5a.xml");

		 IngestUtilities.analyzeFilenames(filenames);
	}

	@Test
	public void testThatWeAreSomewhatCaseSensitive() throws Exception
	{
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("No article found.");

		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.XML");

		IngestUtilities.analyzeFilenames(filenames);
	}

	@Test
	public void testThatWeRequireElementaFilename() throws Exception {
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("No article found.");

		List<String> filenames = new ArrayList<String>();
		filenames.add("notelementa.000017.xml");

		IngestUtilities.analyzeFilenames(filenames);
	}

	@Test
	public void testThatWeDetectIllegalXMLFilenames() throws Exception {
		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("No article found.");

		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.fred.norm.xml");

		IngestUtilities.analyzeFilenames(filenames);
	}

	// Real life directories often have these files. Just verify they don't cause a problem and are ignored like any
	// other unrecognized file

	@Test
	public void testThatWeIgnoreFilesThatStartWithADot() throws Exception {
		List<String> filenames = new ArrayList<String>();
		filenames.add(".Dogmeat");
		filenames.add("elementa.000017.xml");

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatFullPayloadIsOK() throws Exception {
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");
		filenames.add("elementa.000017.epub");
		filenames.add("elementa.000017.mobi");
		filenames.add("elementa.000017.pdf");
		filenames.add("elementa.000017.json");

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
				"Found \"epub\" file: \"elementa.000017.epub\".<br>" +
				"Found \"json\" file: \"elementa.000017.json\".<br>" +
				"Found \"mobi\" file: \"elementa.000017.mobi\".<br>" +
				"Found \"pdf\" file: \"elementa.000017.pdf\".";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatFullPayloadWithDifferentRevisionsIsOK() throws Exception {
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");
		filenames.add("elementa.000017_R7.xml");
		filenames.add("elementa.000017.epub");
		filenames.add("elementa.000017_R5.epub");
		filenames.add("elementa.000017.mobi");
		filenames.add("elementa.000017_R3.mobi");
		filenames.add("elementa.000017.pdf");
		filenames.add("elementa.000017_R1.pdf");
		filenames.add("elementa.000017_R2.pdf");
		filenames.add("elementa.000017.json");

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

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	// The directory can include TIFF files. They must have the case sensitive extension of "tif". They must include
	// an element ID of the form "[eft]<3 digit integer>" and are required.

	@Test
	public void testThatTIFFilesAreOK() throws Exception {
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");
		filenames.add("elementa.000017.e123.tif");
		filenames.add("elementa.000017.f123.tif");
		filenames.add("elementa.000017.t123.tif");

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.<br>" +
				"Found \"tif\" file: \"elementa.000017.e123.tif\".<br>" +
				"Found \"tif\" file: \"elementa.000017.f123.tif\".<br>" +
				"Found \"tif\" file: \"elementa.000017.t123.tif\".";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatTIFFilesMustBeWellFormedWrongNumberOfDigits() throws Exception {
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");
		filenames.add("elementa.000017.e1234.tif");

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatTIFFilesMustBeWellFormedBadPrefix() throws Exception {
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");
		filenames.add("elementa.000017.x123.tif");

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	// The directory can include "supplementary files" which have an element ID of the form "s<3 digit integer>".
	// Supplementary files can have any extension

	@Test
	public void testThatSupplementalFilesTIFFAreOK() throws Exception {
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");
		filenames.add("elementa.000017.s123.tif");

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.<br>" +
				"Found supplemental file: \"elementa.000017.s123.tif\".";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatSupplementalFilesAreOK() throws Exception {
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");
		filenames.add("elementa.000017.s123.dog");

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.<br>" +
				"Found supplemental file: \"elementa.000017.s123.dog\".";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatSupplementalFilesAreWellFormedTooManyDigits() throws Exception {
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");
		filenames.add("elementa.000017.s1234.dog");

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void testThatSupplementalFilesAreWellFormedBadPrefix() throws Exception {
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");
		filenames.add("elementa.000017.x123.dog");

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	//

	@Test
	public void supplementalFilesShouldHaveAnExtension() throws Exception {
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");
		filenames.add("elementa.000017.s123");

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}

	@Test
	public void supplementalFilesShouldHaveANonEmptyExtension() throws Exception {
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");
		filenames.add("elementa.000017.s123.");

		String successMsg =
				"Found article file: \"elementa.000017.xml\".<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"epub\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"json\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"mobi\" file found.<br>" +
				"<b><span style=\"color:#461B7E\">WARNING: </span></b> No \"pdf\" file found.";

		String statusMsg = IngestUtilities.analyzeFilenames(filenames);
		Assert.assertEquals(successMsg, statusMsg);
	}



	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	@Test
	public void testThatWeHarvestLastRevisionSimple() throws Exception
	{
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");
		filenames.add("elementa.000017_V1.xml");
		filenames.add(".Dogmeat");

		List<String> expectedFilenames = new ArrayList<String>();
		expectedFilenames.add("elementa.000017_V1.xml");


		List<String> filesToZip = IngestUtilities.getFilenamesToZip(filenames);
		Assert.assertTrue(SameLists(expectedFilenames, filesToZip));
	}

	@Test
	public void testThatWeHarvestLastRevisionComplex() throws Exception
	{
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017.xml");
		filenames.add("elementa.000017_V7.xml");
		filenames.add("elementa.000017.epub");
		filenames.add("elementa.000017_R5.epub");
		filenames.add("elementa.000017.mobi");
		filenames.add("elementa.000017_V3.mobi");
		filenames.add("elementa.000017.pdf");
		filenames.add("elementa.000017_R1.pdf");
		filenames.add("elementa.000017_V2.pdf");
		filenames.add("elementa.000017.json");

		List<String> expectedFilenames = new ArrayList<String>();
		expectedFilenames.add("elementa.000017_V7.xml");
		expectedFilenames.add("elementa.000017_R5.epub");
		expectedFilenames.add("elementa.000017_V3.mobi");
		expectedFilenames.add("elementa.000017_V2.pdf");
		expectedFilenames.add("elementa.000017.json");

		List<String> filesToZip = IngestUtilities.getFilenamesToZip(filenames);
		Assert.assertTrue(SameLists(expectedFilenames, filesToZip));
	}

	@Test
	public void testThatWeHarvestLastRevisionSupplementaryData() throws Exception
	{
		List<String> filenames = new ArrayList<String>();
		filenames.add("elementa.000017_R1.xml");

		filenames.add("elementa.000017_R7.pdf");
		filenames.add("elementa.000017_R6.pdf");

		filenames.add("elementa.000017_R130.mobi");
		filenames.add("elementa.000017_R66.mobi");

		filenames.add("elementa.000017_R27.json");
		filenames.add("elementa.000017_R166.json");
		filenames.add("elementa.000017_R73.json");
		filenames.add("elementa.000017_R34.json");

		filenames.add("elementa.000017_R1.s123.tif");
		filenames.add("elementa.000017_R7.s123.tif");
		filenames.add("elementa.000017_R3.s123.tif");

		filenames.add("elementa.000017_R8.s321.ppt");
		filenames.add("elementa.000017_R7.s321.ppt");
		filenames.add("elementa.000017_R3.s321.ppt");


		List<String> expectedFilenames = new ArrayList<String>();
		expectedFilenames.add("elementa.000017_R1.xml");
		expectedFilenames.add("elementa.000017_R7.pdf");
		expectedFilenames.add("elementa.000017_R130.mobi");
		expectedFilenames.add("elementa.000017_R166.json");
		expectedFilenames.add("elementa.000017_R7.s123.tif");
		expectedFilenames.add("elementa.000017_R8.s321.ppt");


		List<String> filenamesToZip = IngestUtilities.getFilenamesToZip(filenames);
		Assert.assertTrue(SameLists(expectedFilenames, filenamesToZip));
	}


	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	@Test
	public void testElementID() throws Exception
	{
		String filename = "elementa.000017.s123.ppt";
		Assert.assertEquals("s123", IngestUtilities.getElementID(filename));
	}

	@Test
	public void testElementIDWithRevision() throws Exception
	{
		String filename = "elementa.000017_V5.s123.ppt";
		Assert.assertEquals("s123", IngestUtilities.getElementID(filename));
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	@Test
	public void testGetPowerRankingWithoutPowerRanking() throws Exception
	{
		String filename = "elementa.000017.xml";
		Assert.assertEquals((Integer) 0, IngestUtilities.getPowerRanking(filename));
	}

	@Test
	public void testGetPowerRankingWithPowerRanking() throws Exception
	{
		String filename = "elementa.000017_R7.xml";
		Assert.assertEquals((Integer) 7, IngestUtilities.getPowerRanking(filename));
	}

	@Test
	public void testGetPowerRankingWithPowerRankingMultipleDigits() throws Exception
	{
		String filename = "elementa.000017_R704.xml";
		Assert.assertEquals((Integer) 704, IngestUtilities.getPowerRanking(filename));
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	@Test
	public void testGetElementID() throws Exception
	{
		String filename = "elementa.000017.s123.ppt";
		Assert.assertEquals("s123", IngestUtilities.getElementID(filename));
	}

	@Test
	public void testGetElementIDWithRevision() throws Exception
	{
		String filename = "elementa.000017_R7.t321.ppt";
		Assert.assertEquals("t321", IngestUtilities.getElementID(filename));
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	@Test
	public void testMakingZipFilename() throws Exception
	{
		String filename = "elementa.000017.xml";
		Assert.assertEquals("elementa.000017.xml", IngestUtilities.makeZipFilename(filename));

		filename = "elementa.000017_R5.xml";
		Assert.assertEquals("elementa.000017.xml", IngestUtilities.makeZipFilename(filename));

		filename = "elementa.000017_V7.mobi";
		Assert.assertEquals("elementa.000017.mobi", IngestUtilities.makeZipFilename(filename));

		filename = "elementa.000017_V7.e321.tif";
		Assert.assertEquals("elementa.000017.e321.tif", IngestUtilities.makeZipFilename(filename));

		filename = "elementa.000017_V7.s123.ppt";
		Assert.assertEquals("elementa.000017.s123.ppt", IngestUtilities.makeZipFilename(filename));
	}

	@Test
	public void testZippingBadFilename() throws Exception
	{
		String filename = "illegalPrefix.000017.xml";

		expectedEx.expect(RuntimeException.class);
		expectedEx.expectMessage("Illegal filename [" + filename + "] passed to \"makeZipFilename\"");

		IngestUtilities.makeZipFilename(filename);
	}

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

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