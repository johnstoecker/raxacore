package org.raxa.module.raxacore.web.v1_0.controller;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.binary.Base64;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.annotation.WSDoc;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseCrudController;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.raxa.module.raxacore.ImageService;
import org.raxa.module.raxacore.Image;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for REST web service access to the Image resource.
 */
@Controller
@RequestMapping(value = "/rest/v1/raxacore/image")
public class ImageController {
	
	ImageService service;
	
	Gson gson = new GsonBuilder().serializeNulls().create();
	
	private static final String[] SUPPORTED_MIME_TYPES = { "png", "jpeg", "tiff", "gif" };
	
	private static final String DATAURI_PREFIX = "data:image/";
	
	private static final String DATAURI_ENCODING = ";base64";
	
	/**
	 * Before each function, initialize our service
	 */
	public void initImageController() {
		service = Context.getService(ImageService.class);
	}
	
	/**
	 * Create new Image
	 *
	 * @param post the body of the POST request
	 * @param request
	 * @param response
	 * @return 201 response status and PatientList object
	 * @throws ResponseException
	 */
	@RequestMapping(method = RequestMethod.POST)
	@WSDoc("Save New Image")
	@ResponseBody
	public Object createNewImage(@RequestBody SimpleObject post, HttpServletRequest request, HttpServletResponse response)
	        throws ResponseException {
		initImageController();
		Image created = createUsingPostFields(new Image(), post);
		SimpleObject obj = new SimpleObject();
		obj.add("uuid", created.getUuid());
		obj.add("fileName", created.getFileName());
		return RestUtil.created(response, obj);
	}
	
	/**
	 * Helper function to set fields from a POST call
	 *
	 * @param image our Image to change
	 * @param post our REST call
	 * @return the changed image
	 */
	private Image createUsingPostFields(Image image, SimpleObject post) {
		if (post.get("provider") != null) {
			Provider p = Context.getProviderService().getProviderByUuid(post.get("provider").toString());
			image.setProviderId(p.getId());
			image.setProvider(p);
		}
		if (post.get("location") != null) {
			Location location = Context.getLocationService().getLocationByUuid(post.get("location").toString());
			image.setLocationId(location.getId());
			image.setLocation(location);
		}
		if (post.get("patient") != null) {
			Patient patient = Context.getPatientService().getPatientByUuid(post.get("patient").toString());
			image.setPatientId(patient.getId());
			image.setPatient(patient);
		}
		if (post.get("dataURI") != null) {
			String[] dataURISplit = post.get("dataURI").toString().split(",");
			if (dataURISplit.length != 2) {
				throw new IllegalArgumentException("Malformed DataURI String");
			}
			Pattern IMAGE_TYPE_PATTERN = Pattern.compile("data:image/(\\w*)(?:;.*)*,(.*)");
			Matcher m = IMAGE_TYPE_PATTERN.matcher(post.get("dataURI").toString());
			String imageType = "", rawData = "";
			try {
				while (m.find()) {
					imageType = m.group(1);
					rawData = m.group(2);
				}
			}
			catch (Exception e) {
				throw new IllegalArgumentException("Malformed dataURI, must be data:image/<image type>,<image data>");
			}
			boolean foundType = false;
			for (int k = 0; k < SUPPORTED_MIME_TYPES.length; k++) {
				if (imageType.equals(SUPPORTED_MIME_TYPES[k])) {
					foundType = true;
					image.setFileName(imageType);
				}
			}
			if (!foundType) {
				throw new IllegalArgumentException("Image type in Data URI is not supported");
			}
			image.setImageData(Base64.decodeBase64(rawData));
		}
		if (post.get("fileName") != null) {
			String fileName = post.get("fileName").toString();
			String[] fileNameParts = fileName.split("\\.");
			if (fileNameParts.length > 0 && fileName.contains(".")) {
				image.setFileName(fileNameParts[fileNameParts.length - 1]);
			} else {
				throw new IllegalArgumentException("File name needs suffix (.jpg, .png, etc)");
			}
		}
		if (post.get("tags") != null) {
			image.setTags(post.get("tags").toString());
		}
		image = service.saveImage(image);
		return image;
	}
	
	/**
	 *
	 * @param image
	 * @return SimpleObject the representation of Image
	 */
	private SimpleObject getFieldsFromImage(Image image) {
		SimpleObject obj = new SimpleObject();
		obj.add("uuid", image.getUuid());
		obj.add("fileName", image.getFileName());
		obj.add("tags", image.getTags());
		String fileType = image.getFileName().split("\\.")[1];
		obj.add("dataURI", DATAURI_PREFIX + fileType + DATAURI_ENCODING + ","
		        + Base64.encodeBase64String(image.getImageData()));
		SimpleObject providerObj = new SimpleObject();
		Provider provider = image.getProvider();
		if (provider != null) {
			providerObj.add("uuid", provider.getUuid());
			providerObj.add("display", provider.getName());
		}
		obj.add("provider", providerObj);
		SimpleObject locationObj = new SimpleObject();
		Location location = image.getLocation();
		if (location != null) {
			locationObj.add("uuid", location.getUuid());
			locationObj.add("display", location.getName());
		}
		obj.add("location", locationObj);
		SimpleObject patientObj = new SimpleObject();
		Patient p = image.getPatient();
		if (p != null) {
			patientObj.add("uuid", p.getUuid());
			patientObj.add("display", p.getPersonName().getFullName());
		}
		obj.add("patient", patientObj);
		return obj;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	@WSDoc("Get All Unretired Images in the system")
	@ResponseBody()
	public String getAllImages(HttpServletRequest request, HttpServletResponse response) throws ResponseException {
		initImageController();
		List<Image> allImage = service.getAllImages();
		ArrayList results = new ArrayList();
		for (Image image : allImage) {
			results.add(getFieldsFromImage(image));
		}
		return gson.toJson(new SimpleObject().add("results", results));
	}
	
	/**
	 * Fetch images according to provider
	 *
	 * @param provider
	 * @param request
	 * @param response
	 * @return encounters for the given patient
	 * @throws ResponseException
	 */
	@RequestMapping(method = RequestMethod.GET, params = "provider")
	@WSDoc("Fetch all non-retired images according to provider")
	@ResponseBody()
	public String searchByProvider(@RequestParam("provider") String provider, HttpServletRequest request)
	        throws ResponseException {
		initImageController();
		return alertListToJson(service.getImagesByProviderUuid(provider));
	}
	
	/**
	 * Fetch images according to location
	 *
	 * @param location
	 * @param request
	 * @param response
	 * @return images for the given location
	 * @throws ResponseException
	 */
	@RequestMapping(method = RequestMethod.GET, params = "location")
	@WSDoc("Fetch all non-retired images according to location")
	@ResponseBody()
	public String searchByLocation(@RequestParam("location") String location, HttpServletRequest request)
	        throws ResponseException {
		initImageController();
		return alertListToJson(service.getImagesByLocationUuid(location));
	}
	
	/**
	 * Fetch images according to tags
	 *
	 * @param tag
	 * @param request
	 * @param response
	 * @return images for the given location
	 * @throws ResponseException
	 */
	@RequestMapping(method = RequestMethod.GET, params = "tag")
	@WSDoc("Fetch all non-retired images according to tag")
	@ResponseBody()
	public String searchByTags(@RequestParam("tag") String tags, HttpServletRequest request) throws ResponseException {
		initImageController();
		return alertListToJson(service.getImagesByTag(tags));
	}
	
	/**
	 * Fetch latest image for patient according to tag
	 *
	 * @param patient
	 * @param tag
	 * @param request
	 * @param response
	 * @return images for the given location
	 * @throws ResponseException
	 */
	@RequestMapping(method = RequestMethod.GET, params = { "patient", "tag" })
	@WSDoc("Fetch latest image according to patient and tag")
	@ResponseBody()
	public String searchLatestForPatientByTag(@RequestParam("patient") String patient, @RequestParam("tag") String tag,
	        HttpServletRequest request) throws ResponseException {
		initImageController();
		Image image = service.getLatestImageByTagForPatient(tag, patient);
		return gson.toJson(getFieldsFromImage(image));
	}
	
	/**
	 * Helper function that parses a list of Images, returns a JSon
	 */
	private String alertListToJson(List<Image> images) {
		ArrayList results = new ArrayList();
		for (Image image : images) {
			results.add(getFieldsFromImage(image));
		}
		return gson.toJson(new SimpleObject().add("results", results));
	}
	
	/**
	 * Updates the Image by making a POST call with uuid in URL
	 *
	 * @param uuid the uuid for the image resource
	 * @param post
	 * @param request
	 * @param response
	 * @return 200 response status
	 * @throws ResponseException
	 */
	@RequestMapping(value = "/{uuid}", method = RequestMethod.POST)
	@WSDoc("Updates an existing image")
	@ResponseBody
	public Object updateImage(@PathVariable("uuid") String uuid, @RequestBody SimpleObject post, HttpServletRequest request,
	        HttpServletResponse response) throws ResponseException {
		initImageController();
		Image image = service.getImageByUuid(uuid);
		image = createUsingPostFields(image, post);
		Image created = service.updateImage(image);
		SimpleObject obj = new SimpleObject();
		obj.add("uuid", created.getUuid());
		obj.add("fileName", created.getFileName());
		return RestUtil.noContent(response);
	}
	
	/**
	 * Get the Image by uuid
	 * @param uuid
	 * @param request
	 * @return response string
	 * @throws ResponseException 
	 */
	@RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
	@WSDoc("Gets Image for the given uuid")
	@ResponseBody()
	public String getImageByUuid(@PathVariable("uuid") String uuid, HttpServletRequest request) throws ResponseException {
		initImageController();
		Image image = service.getImageByUuid(uuid);
		return gson.toJson(getFieldsFromImage(image));
	}
	
}
