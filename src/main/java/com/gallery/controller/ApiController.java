package com.gallery.controller;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gallery.domain.Admin;
import com.gallery.domain.Photo;
import com.gallery.domain.PhotoFailMessage;
import com.gallery.domain.PhotoSuccessMessage;
import com.gallery.domain.PhotoUploadMessage;
import com.gallery.model.ImageUploader;
import com.gallery.repositories.AdminRepository;
import com.gallery.repositories.CategoryRepository;
import com.gallery.repositories.PhotoRepository;
import com.gallery.utils.DateUtils;
import com.gallery.utils.SessionInfoUtils;
import com.gallery.utils.StringUtils;

@RestController
public class ApiController {
	
	private PhotoUploadMessage pum;

	@Autowired
	PhotoRepository photoRepo;
	
	@Autowired
	CategoryRepository categoryRepo;
	
	@Autowired
	AdminRepository adminRepo;

	@PostMapping("/api/upload")
	public PhotoUploadMessage uploadPhoto(@RequestParam MultipartFile upload, String title, String description, String category, HttpSession session) throws IOException {
		Admin loggedInAdmin = (Admin)session.getAttribute(SessionInfoUtils.SESSIONED_LOGIN_KEYWORD);
		String extension = StringUtils.getExtensionFromMultipart(upload);
		String contentType = upload.getContentType();
		ImageUploader iu = ImageUploader.getUploaderInstance(loggedInAdmin, extension, contentType);
		boolean photoUpload = iu.sendImageToS3(upload.getBytes());
		
		if (photoUpload) {
			Photo photoInfoOnDB = new Photo();
			photoInfoOnDB.setDate(DateUtils.getDaysTimeStamp());
			photoInfoOnDB.setPath(iu.getUploadedFileName());
			photoInfoOnDB.setDescription(description);
			
			photoRepo.save(photoInfoOnDB);
			pum = new PhotoSuccessMessage(photoInfoOnDB.getPath(), photoUpload);
			pum.setHtmlForm();
			return pum;
		}
		pum = new PhotoFailMessage("", photoUpload);
		pum.setHtmlForm();
		return pum;
	}

}