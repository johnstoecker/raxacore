package org.raxa.module.raxacore.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.util.OpenmrsUtil;
import org.raxa.module.raxacore.Image;
import org.raxa.module.raxacore.ImageService;
import org.raxa.module.raxacore.db.ImageDAO;

public class ImageServiceImplTest extends BaseModuleContextSensitiveTest {
	
	private ImageService s = null;
	
	private static final String TEST_PATIENT_FILE = "mytest.jpg";
	
	private static final String TEST_UUID = "68547121-1b70-465c-99ee-c9dfd06e7e32";
	
	private static final byte[] TEST_DATA = { 0x12, 0x11, 0x10, 0x9 };
	
	private static final String TEST_DATA_PATH = "org/raxa/module/raxacore/include/";
	
	private static final String MODULE_TEST_DATA_XML = TEST_DATA_PATH + "moduleTestData.xml";
	
	/*
	 * Make a new test folder to put our temporary images
	 */
	@Before
	public void setUp() throws IOException, Exception {
		executeDataSet(MODULE_TEST_DATA_XML);
		s = Context.getService(ImageService.class);
		String filepath = OpenmrsUtil.getApplicationDataDirectory() + TEST_PATIENT_FILE;
		File testFile = new File(filepath);
		if (testFile.createNewFile())
			new FileOutputStream(testFile).write(TEST_DATA);
	}
	
	/**
	 * Test of saveImage method, of class ImageServiceImpl.
	 */
	@Test
	public void testSaveImage() {
		Image i = new Image();
		i.setFileName(TEST_PATIENT_FILE);
		i.setImageData(TEST_DATA);
		i.setCreator(Context.getUserContext().getAuthenticatedUser());
		i.setDateCreated(new java.util.Date());
		s.saveImage(i);
		File f = new File(s.getPath(i));
		try {
			assert (f.exists());
		}
		catch (Exception ex) {
			fail("Unable to access file system to test ImageService, error:" + ex.getMessage());
		}
	}
	
	/**
	 * Test of getImageByUuid method, of class ImageServiceImpl.
	 */
	@Test
	public void testGetImageByUuid() {
		Image i = s.getImageByUuid(TEST_UUID);
		Assert.assertArrayEquals(i.getImageData(), TEST_DATA);
		Assert.assertEquals(i.getTags(), "test image");
		File f = new File(s.getPath(i));
		try {
			FileInputStream fis = new FileInputStream(f);
			byte[] actualData = new byte[4];
			fis.read(actualData);
			Assert.assertArrayEquals(TEST_DATA, actualData);
		}
		catch (IOException ex) {
			fail("Unable to access file system to test PatientImageService, error:" + ex.getMessage());
		}
	}
	
	/**
	 * Test of getAllImages method, of class ImageServiceImpl.
	 */
	@Test
	public void testGetAllImages() {
		List<Image> result = s.getAllImages();
		assertEquals(1, result.size());
		assertEquals(TEST_PATIENT_FILE, result.get(0).getFileName());
	}
	
	/**
	 * Test of updateImage method, of class ImageServiceImpl.
	 */
	@Test
	public void testUpdateImage() {
		Image i = s.getImageByUuid(TEST_UUID);
		byte[] newData = { 0x1, 0x2, 0x3, 0x4, 0x5 };
		i.setImageData(newData);
		i.setTags("new tag");
		Image newImage = s.updateImage(i);
		Assert.assertArrayEquals(newData, newImage.getImageData());
		File f = new File(s.getPath(newImage));
		System.out.println(s.getPath(newImage));
		try {
			FileInputStream fis = new FileInputStream(f);
			byte[] actualData = new byte[5];
			fis.read(actualData);
			Assert.assertArrayEquals(newData, actualData);
		}
		catch (IOException ex) {
			fail("Unable to access file system to test PatientImageService, error:" + ex.getMessage());
		}
		Assert.assertEquals("new tag", newImage.getTags());
		//setting back to original test data in file system
		i.setImageData(TEST_DATA);
		s.updateImage(i);
	}
	
	/**
	 * Test of deleteImage method, of class ImageServiceImpl.
	 */
	@Test
	public void testDeleteImage() {
		Image i = s.getImageByUuid(TEST_UUID);
		s.deleteImage(i);
		Assert.assertEquals(0, s.getAllImages().size());
		String filepath = OpenmrsUtil.getApplicationDataDirectory() + TEST_PATIENT_FILE;
		File testFile = new File(filepath);
		try {
			Assert.assertTrue(!testFile.createNewFile());
		}
		catch (IOException ex) {
			Assert.fail("cannot check file system");
		}
	}
	
	/**
	 * Test of getImagesByProviderUuid method, of class ImageServiceImpl.
	 */
	@Test
	public void testGetImagesByProviderUuid() {
		List<Image> images = s.getImagesByProviderUuid("3effc802-12dd-4539-87f6-4065ca8e992b");
		Assert.assertEquals(1, images.size());
		Assert.assertEquals(images.get(0).getTags(), "test image");
	}
	
	/**
	 * Test of getImagesByPatientUuid method, of class ImageServiceImpl.
	 */
	@Test
	public void testGetImagesByPatientUuid() {
		List<Image> images = s.getImagesByPatientUuid("da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
		Assert.assertEquals(1, images.size());
		Assert.assertEquals(images.get(0).getTags(), "test image");
	}
	
	/**
	 * Test of getImagesByLocationUuid method, of class ImageServiceImpl.
	 */
	@Test
	public void testGetImagesByLocationUuid() {
		List<Image> images = s.getImagesByLocationUuid("dc5c1fcc-0459-4201-bf70-0b90535ba362");
		Assert.assertEquals(1, images.size());
		Assert.assertEquals(images.get(0).getTags(), "test image");
	}
	
	/**
	 * Test of getImagesByTag method, of class ImageServiceImpl.
	 */
	@Test
	public void testGetImagesByTag() {
		List<Image> images = s.getImagesByTag("est");
		Assert.assertEquals(1, images.size());
		Assert.assertEquals(images.get(0).getTags(), "test image");
	}
	
	/**
	 * Test of getLatestImageByTag method, of class ImageServiceImpl.
	 */
	@Test
	public void testGetLatestImageByTag() {
		Image i = s.getLatestImageByTag("est");
		Assert.assertArrayEquals(i.getImageData(), TEST_DATA);
		Assert.assertEquals(i.getTags(), "test image");
	}
	
	/**
	 * Test of getLatestImageByTag method, of class ImageServiceImpl.
	 */
	@Test
	public void testGetLatestImageByTagForPatient() {
		Image i = s.getLatestImageByTagForPatient("est", "da7f524f-27ce-4bb2-86d6-6d1d05312bd5");
		Assert.assertArrayEquals(i.getImageData(), TEST_DATA);
		Assert.assertEquals(i.getTags(), "test image");
	}
	
	/**
	 * Recursively deletes a file or directory.
	 * @param file
	 * @throws IOException
	 */
	private static void delete(File file) throws IOException {
		if (file.isDirectory()) {
			//directory is empty, then delete it
			if (file.list().length == 0) {
				file.delete();
			} else {
				//list all the directory contents
				String files[] = file.list();
				for (String temp : files) {
					//construct the file structure
					File fileDelete = new File(file, temp);
					
					//recursive delete
					delete(fileDelete);
				}
				
				//check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
				}
			}
		} else {
			//if file, then delete it
			file.delete();
		}
	}
	
}
