package org.raxa.module.raxacore.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.raxa.module.raxacore.Image;
import org.raxa.module.raxacore.ImageService;
import org.raxa.module.raxacore.db.ImageDAO;

/**
 * Copyright 2012, Raxa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

public class ImageServiceImpl implements ImageService {
	
	private ImageDAO dao;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private static final String IMGDIR = "patientimages";
	
	private static final String DEFAULT_FILE_TYPE = "png";
	
	/**
	 * @see org.raxa.module.raxacore.RaxaAlertService#setRaxaAlertDAO
	 */
	@Override
	public void setImageDAO(ImageDAO dao) {
		this.dao = dao;
	}
	
	@Override
	public Image saveImage(Image image) {
		Image i = dao.saveImage(image);
		saveImageOnFileSystem(image);
		return i;
	}
	
	private void saveImageOnFileSystem(Image image) {
		File imgDir = new File(OpenmrsUtil.getApplicationDataDirectory() + IMGDIR);
		File img = new File(getPath(image));
		try {
			if (!imgDir.exists()) {
				FileUtils.forceMkdir(imgDir);
			}
		}
		catch (IOException e) {
			System.out.println(e);
			log.error(e);
		}
		FileOutputStream fos = null;
		try {
			if (image.getFileName() == null || image.getFileName().equals("")) {
				image.setFileName(DEFAULT_FILE_TYPE);
			}
			String[] fileNameSplitter = image.getFileName().split("\\.");
			image.setFileName(image.getUuid() + "." + fileNameSplitter[fileNameSplitter.length - 1]);
			fos = new FileOutputStream(imgDir + System.getProperty("file.separator") + image.getFileName());
			fos.write(image.getImageData());
			fos.close();
		}
		catch (Exception e) {
			System.out.println(e);
			log.error(e);
		}
		finally {
			IOUtils.closeQuietly(fos);
		}
	}
	
	@Override
	public Image getImageByUuid(String uuid) {
		Image image = dao.getImageByUuid(uuid);
		File f = new File(getPath(image));
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			byte[] imageData = IOUtils.toByteArray(fis);
			fis.read(imageData);
			image.setImageData(imageData);
			fis.close();
			return image;
		}
		catch (IOException ex) {
			log.error("Reading image directory failed with: " + ex.getMessage());
		}
		finally {
			IOUtils.closeQuietly(fis);
		}
		return new Image();
	}
	
	@Override
	public List<Image> getAllImages() {
		return dao.getAllImages();
	}
	
	@Override
	public Image updateImage(Image image) {
		saveImageOnFileSystem(image);
		return dao.updateImage(image);
	}
	
	@Override
	public void deleteImage(Image image) {
		dao.deleteImage(image);
	}
	
	@Override
	public List<Image> getImagesByProviderUuid(String providerUuid) throws APIException {
		if (Context.getProviderService().getProviderByUuid(providerUuid) == null) {
			throw new IllegalArgumentException("Provider uuid is invalid");
		}
		return dao.getImagesByProviderId(Context.getProviderService().getProviderByUuid(providerUuid).getId());
	}
	
	@Override
	public List<Image> getImagesByPatientUuid(String patientUuid) throws APIException {
		if (Context.getPatientService().getPatientByUuid(patientUuid) == null) {
			throw new IllegalArgumentException("Patient uuid is invalid");
		}
		return dao.getImagesByPatientId(Context.getPatientService().getPatientByUuid(patientUuid).getId());
	}
	
	@Override
	public List<Image> getImagesByLocationUuid(String locationUuid) {
		if (Context.getLocationService().getLocationByUuid(locationUuid) == null) {
			throw new IllegalArgumentException("Location uuid is invalid");
		}
		return dao.getImagesByLocationId(Context.getLocationService().getLocationByUuid(locationUuid).getId());
	}
	
	@Override
	public List<Image> getImagesByTag(String tag) {
		return dao.getImagesByTag(tag);
	}
	
	@Override
	public Image getLatestImageByTag(String tag) throws APIException {
		Image image = dao.getLatestImageByTag(tag);
		if (image == null) {
			throw new IllegalArgumentException("No images found with given tag");
		}
		File f = new File(getPath(image));
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			byte[] imageData = IOUtils.toByteArray(fis);
			fis.read(imageData);
			image.setImageData(imageData);
			fis.close();
			return image;
		}
		catch (IOException ex) {
			log.error("Reading image directory failed with: " + ex.getMessage());
		}
		finally {
			IOUtils.closeQuietly(fis);
		}
		return image;
	}
	
	@Override
	public Image getLatestImageByTagForPatient(String tag, String patientUuid) {
		if (Context.getPatientService().getPatientByUuid(patientUuid) == null) {
			throw new IllegalArgumentException("Patient uuid is invalid");
		}
		Image image = dao.getLatestImageByTagForPatient(tag, Context.getPatientService().getPatientByUuid(patientUuid)
		        .getId());
		if (image == null) {
			throw new IllegalArgumentException("No images found with given tag");
		}
		File f = new File(getPath(image));
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			byte[] imageData = IOUtils.toByteArray(fis);
			fis.read(imageData);
			image.setImageData(imageData);
			return image;
		}
		catch (IOException ex) {
			log.error("Reading image directory failed with: " + ex.getMessage());
		}
		finally {
			IOUtils.closeQuietly(fis);
		}
		return image;
	}
	
	@Override
	public void onStartup() {
	}
	
	@Override
	public void onShutdown() {
	}
	
	@Override
	public String getPath(Image p) {
		return OpenmrsUtil.getApplicationDataDirectory() + IMGDIR + System.getProperty("file.separator") + p.getFileName();
	}
	
	@Override
	public String getImageDirectory() {
		return OpenmrsUtil.getApplicationDataDirectory() + IMGDIR;
	}
	
}
