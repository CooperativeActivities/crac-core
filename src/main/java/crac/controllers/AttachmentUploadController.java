package crac.controllers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import crac.models.db.daos.AttachmentDAO;
import crac.models.db.entities.Attachment;

@RestController
@RequestMapping("/attachmentUl")
public class AttachmentUploadController {

	@Autowired
	private AttachmentDAO attachmentDAO;

	@RequestMapping(value = "/addFile/{attachment_id}", headers = "content-type=multipart/*", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String> uploadFile(@PathVariable(value = "attachment_id") Long attachment_id,
			@RequestParam("uploadfile") MultipartFile uploadfile) {

		Attachment myAtt = attachmentDAO.findOne(attachment_id);
		String filename = "";

		if (myAtt == null) {
			return ResponseEntity.badRequest().body("{\"uploaded\":\"false\", \"exception\":\"no_attachment\"}");
		}

		if (!myAtt.getPath().equals("")) {
			return ResponseEntity.badRequest().body("{\"uploaded\":\"false\", \"exception\":\"attachment_has_file\"}");
		}

		try {
			// Get the filename and build the local file path (be sure that the
			// application have write permissions on such directory)
			java.util.Date date = new java.util.Date();

			filename = new Timestamp(date.getTime()).hashCode() + uploadfile.getOriginalFilename();
			String directory = "uploadedFiles";
			String filepath = Paths.get(directory, filename).toString();

			// Save the file locally
			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(filepath)));
			stream.write(uploadfile.getBytes());
			stream.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return ResponseEntity.badRequest().body("{\"uploaded\":\"false\"}");
		}

		myAtt.setPath(filename);
		attachmentDAO.save(myAtt);

		return ResponseEntity.ok().body("{\"uploaded\":\"true\", \"img_name\":\"" + filename + "\"}");
	} // method uploadFile

	@RequestMapping(value = "/getImage/{image_path}/{image_size}", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
	@ResponseBody
	public ResponseEntity<byte[]> getUserImage(@PathVariable(value = "image_path") String image_path,
			@PathVariable(value = "image_size") String image_size) {

		String path = "";
		/*
		 * 
		 * if(objImage.getUserId().getId() != myUser.getId()){ path =
		 * "uploadedFiles/bad_connection.jpg"; }else{ path =
		 * "uploadedFiles/"+objImage.getPath(); }
		 */
		System.out.println(image_path);
		path = "uploadedFiles/" + image_path;

		System.out.println();

		File image = new File(path);
		byte[] imageContent = null;
		try {
			imageContent = Files.readAllBytes(image.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);

		return ResponseEntity.ok().headers(headers).body(imageContent);
	} // method uploadFile

	@RequestMapping(value = "/changeFile/{attachment_id}", headers = "content-type=multipart/*", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<String> changeFile(@PathVariable(value = "attachment_id") Long attachment_id,
			@RequestParam("uploadfile") MultipartFile uploadfile) {

		Attachment myAtt = attachmentDAO.findOne(attachment_id);
		String filename = "";

		if (myAtt == null) {
			return ResponseEntity.badRequest().body("{\"uploaded\":\"false\", \"exception\":\"no_attachment\"}");
		}

		if (myAtt.getPath().equals("") || myAtt.getPath() == null) {
			return ResponseEntity.badRequest()
					.body("{\"uploaded\":\"false\", \"exception\":\"attachment_has_no_file\"}");
		}

		try {
			// Get the filename and build the local file path (be sure that the
			// application have write permissions on such directory)
			java.util.Date date = new java.util.Date();

			filename = new Timestamp(date.getTime()).hashCode() + uploadfile.getOriginalFilename();
			String directory = "uploadedFiles";
			String filepath = Paths.get(directory, filename).toString();

			// Save the file locally
			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(filepath)));
			stream.write(uploadfile.getBytes());
			stream.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return ResponseEntity.badRequest().body("{\"uploaded\":\"false\"}");
		}

		String oldFile = myAtt.getPath();

		try {

			File file = new File("uploadedFiles/" + oldFile);

			if (file.delete()) {
				System.out.println(file.getName() + " is deleted!");
			} else {
				System.out.println("Delete operation is failed.");
			}

		} catch (Exception e) {

			e.printStackTrace();
			return ResponseEntity.badRequest().body("{\"uploaded\":\"false\", \"exception\":\"error_deleting\"}");

		}

		myAtt.setPath(filename);
		attachmentDAO.save(myAtt);

		return ResponseEntity.ok().body("{\"uploaded\":\"true\", \"img_name\":\"" + filename + "\"}");
	} // method uploadFile

}
