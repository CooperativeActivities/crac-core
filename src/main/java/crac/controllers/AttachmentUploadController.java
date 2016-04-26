package crac.controllers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import crac.daos.AttachmentDAO;
import crac.models.Attachment;

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
		String filepath = "";

		if (myAtt.getId() != attachment_id) {
			return ResponseEntity.badRequest().body("{\"uploaded\":\"false\"}");
		}

		try {
			// Get the filename and build the local file path (be sure that the
			// application have write permissions on such directory)
			java.util.Date date= new java.util.Date();
			
			Random rn = new Random();
			int maximum = 100;
			int minimum = 1;
			int range = maximum - minimum + 1;
			int randomNum =  rn.nextInt(range) + minimum;
			
			String filename = new Timestamp(date.getTime()).hashCode()*randomNum+uploadfile.getOriginalFilename();
			String directory = "uploadedFiles";
			filepath = Paths.get(directory, filename).toString();

			// Save the file locally
			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(filepath)));
			stream.write(uploadfile.getBytes());
			stream.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return ResponseEntity.badRequest().body("{\"uploaded\":\"false\"}");
		}

		myAtt.setPath(filepath);
		attachmentDAO.save(myAtt);

		return ResponseEntity.ok().body("{\"uploaded\":\"true\", \"img_name\":\""+filepath+"\"}");
	} // method uploadFile

}
