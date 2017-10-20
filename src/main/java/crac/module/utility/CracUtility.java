package crac.module.utility;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;

import org.springframework.web.multipart.MultipartFile;

import crac.enums.ErrorCode;
import crac.exception.InvalidActionException;

/**
 * This class contains generally helpfull static methods for the framework
 * @author David Hondl
 *
 */
public class CracUtility {

	/**
	 * Static method that returns a random string with given length
	 * @param length
	 * @return String
	 */
	public static String randomString(final int length) {
		Random r = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int c = r.nextInt(9);
			sb.append((c < 0) ? c * -1 : c);
		}
		return sb.toString();
	}

	/**
	 * Static method that processes an uploaded multipart-file
	 * @param file
	 * @param allowedMIME
	 * @return
	 * @throws IOException
	 * @throws InvalidActionException
	 */
	public static String processUpload(MultipartFile file, String... allowedMIME)
			throws IOException, InvalidActionException {

		String type = file.getContentType();
		boolean allowed = false;

		for (String mime : allowedMIME) {
			if (type.equals(mime)) {
				allowed = true;
			}
		}

		if (!allowed) {
			throw new InvalidActionException(ErrorCode.MIME_TYPE_NOT_ALLOWED);
		}

		String filename = new Timestamp(new Date().getTime()).hashCode() + file.getOriginalFilename();
		String directory = "uploadedFiles";
		String filepath = Paths.get(directory, filename).toString();

		// Save the file locally
		BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(filepath)));
		stream.write(file.getBytes());
		stream.close();

		return filename;
	}

	/**
	 * Static method, that removes target file
	 * @param path
	 * @throws InvalidActionException
	 */
	public static void removeFile(String path) throws InvalidActionException {
		if (!new File("uploadedFiles/" + path).delete()) {
			throw new InvalidActionException(ErrorCode.ACTION_NOT_VALID);
		}
	}

	/**
	 * Static method that loads and returns target file as byte-array
	 * @param path
	 * @return byte[]
	 * @throws IOException
	 */
	public static byte[] getFile(String path) throws IOException {
		File image = new File("uploadedFiles/" + path);
		byte[] imageContent = null;
		imageContent = Files.readAllBytes(image.toPath());
		return imageContent;
	}

}
