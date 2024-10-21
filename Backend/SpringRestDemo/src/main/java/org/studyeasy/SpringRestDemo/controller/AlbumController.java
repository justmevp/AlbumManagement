package org.studyeasy.SpringRestDemo.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.studyeasy.SpringRestDemo.model.Account;
import org.studyeasy.SpringRestDemo.model.Album;
import org.studyeasy.SpringRestDemo.model.Photo;
import org.studyeasy.SpringRestDemo.payload.auth.album.AlbumPayloadDTO;
import org.studyeasy.SpringRestDemo.payload.auth.album.AlbumViewDTO;
import org.studyeasy.SpringRestDemo.payload.auth.album.PhotoDTO;
import org.studyeasy.SpringRestDemo.payload.auth.album.PhotoPayloadDTO;
import org.studyeasy.SpringRestDemo.payload.auth.album.PhotoViewDTO;
import org.studyeasy.SpringRestDemo.service.AccountService;
import org.studyeasy.SpringRestDemo.service.AlbumService;
import org.studyeasy.SpringRestDemo.service.PhotoService;
import org.studyeasy.SpringRestDemo.util.AppUtils.AppUtil;
import org.studyeasy.SpringRestDemo.util.constants.AlbumError;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@Tag(name = "Album Controller", description = "Controller for Album and Photo management")
@Slf4j
public class AlbumController {

    static final String PHOTOS_FOLDER_NAME = "photos";
    static final String THUMBNAIL_FOLDER_NAME = "thumbnails";
    static final int THUMBNAIL_WIDTH = 300;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AlbumService albumService;

    @Autowired
    private PhotoService photoService;

    @PostMapping(value = "/albums/add", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "")
    @ApiResponse(responseCode = "200", description = "Album added")
    @Operation(summary = "Add a new Album")
    @SecurityRequirement(name = "studyeasy-demo-api")
    public ResponseEntity<AlbumViewDTO> addAlbum(@Valid @RequestBody AlbumPayloadDTO albumPayloadDTO,
            Authentication authentication) {
        try {
            Album album = new Album();
            album.setName(albumPayloadDTO.getName());
            album.setDescription(albumPayloadDTO.getDescription());
            String email = authentication.getName();
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();
            album.setAccount(account);
            album = albumService.save(album);
            AlbumViewDTO albumViewDTO = new AlbumViewDTO(album.getId(), album.getName(), album.getDescription(), null);
            return ResponseEntity.ok(albumViewDTO);
        } catch (Exception e) {
            log.debug(AlbumError.ADD_ALBUM_ERROR.toString() + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

    }

    @GetMapping(value = "/albums", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "List of albums")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token error")
    @SecurityRequirement(name = "studyeasy-demo-api")
    @Operation(summary = "List album api")
    public List<AlbumViewDTO> albums(Authentication authentication) {

        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        Account account = optionalAccount.get();
        List<AlbumViewDTO> albums = new ArrayList<>();
        for (Album album : albumService.findByAccountId(account.getId())) {

            List<PhotoDTO> photos = new ArrayList<>();
            for (Photo photo : photoService.findByAlbumId(album.getId())) {
                String link = "/albums/" + album.getId() + "/photos/" + photo.getId() + "/download-photo";
                photos.add(new PhotoDTO(photo.getId(), photo.getName(), photo.getDescription(), photo.getFileName(),
                        link));
            }
            albums.add(new AlbumViewDTO(album.getId(), album.getName(), album.getDescription(), photos));

        }
        return albums;

    }

    @GetMapping(value = "/albums/{album_id}", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "List of albums")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token error")
    @SecurityRequirement(name = "studyeasy-demo-api")
    @Operation(summary = "List album api")
    public ResponseEntity<AlbumViewDTO> albums_by_id(@PathVariable long album_id, Authentication authentication) {
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        Account account = optionalAccount.get();
        Optional<Album> optionalAlbum = albumService.findById(album_id);
        Album album;
        if (optionalAlbum.isPresent()) {
            album = optionalAlbum.get();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        if (account.getId() != album.getAccount().getId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);

        }
        List<PhotoDTO> photos = new ArrayList<>();
        for (Photo photo : photoService.findByAlbumId(album.getId())) {
            String link = "/albums/" + album.getId() + "/photos/" + photo.getId() + "/download-photo";
            photos.add(new PhotoDTO(photo.getId(), photo.getName(), photo.getDescription(), photo.getFileName(),
                    link));
        }
        AlbumViewDTO albumViewDTO = new AlbumViewDTO(album.getId(), album.getName(), album.getDescription(), photos);
        return ResponseEntity.ok(albumViewDTO);

    }

    @PostMapping(value = "albums/{album_id}/upload-photos", consumes = { "multipart/form-data" })
    @Operation(summary = "Upload photo into album") // Chú thích OpenAPI, tóm tắt về chức năng upload ảnh vào album
    @ApiResponse(responseCode = "400", description = "Please check the payload or token") // Trả về mã lỗi 400 nếu có
                                                                                          // lỗi với payload hoặc token
    @SecurityRequirement(name = "studyeasy-demo-api") // Yêu cầu bảo mật cho API

    public ResponseEntity<List<HashMap<String, List<?>>>> photos(
            @RequestPart(required = true) MultipartFile[] files, // Nhận mảng file ảnh từ request
            @PathVariable long album_id, // Nhận ID của album từ URL
            Authentication authentication) { // Nhận thông tin xác thực (authentication) từ người dùng
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        Account account = optionalAccount.get();
        Optional<Album> optionaAlbum = albumService.findById(album_id);
        Album album;

        if (optionaAlbum.isPresent()) {
            album = optionaAlbum.get();
            if (account.getId() != album.getAccount().getId()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        List<PhotoViewDTO> fileNamesWithSuccess = new ArrayList<>();
        List<String> fileNamesWithError = new ArrayList<>();

        // Lặp qua các file được gửi lên trong mảng files
        Arrays.asList(files).stream().forEach(file -> {
            String contentType = file.getContentType(); // Lấy loại MIME của file

            // Kiểm tra xem file có phải là định dạng ảnh hợp lệ (PNG, JPG, JPEG) hay không
            if (contentType.equals("image/png")
                    || contentType.equals("image/jpg")
                    || contentType.equals("image/jpeg")) {
                // Tạo một chuỗi ngẫu nhiên để đặt tên file (tránh trùng lặp)
                int length = 10;
                boolean useLetters = true;
                boolean useNumbers = true;

                try {
                    String fileName = file.getOriginalFilename(); // Lấy tên gốc của file
                    String generatedString = RandomStringUtils.random(length, useLetters, useNumbers); // Tạo chuỗi ngẫu
                                                                                                       // nhiên
                    String final_photo_name = generatedString + fileName; // Tạo tên file cuối cùng bằng cách ghép chuỗi
                                                                          // ngẫu nhiên với tên gốc
                    String absolute_fileLocation = AppUtil.get_photo_upload_path(final_photo_name, PHOTOS_FOLDER_NAME,
                            album_id); // Tạo đường dẫn tuyệt đối để lưu file
                    Path path = Paths.get(absolute_fileLocation); // Tạo đối tượng Path từ đường dẫn
                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING); // Lưu file vào đường
                                                                                                  // dẫn chỉ định

                    // Tạo đối tượng Photo và lưu thông tin file vào cơ sở dữ liệu
                    Photo photo = new Photo();
                    photo.setName(fileName); // Lưu tên gốc của file
                    photo.setFileName(final_photo_name); // Lưu tên file sau khi thêm chuỗi ngẫu nhiên
                    photo.setOriginalFileName(fileName); // Lưu tên file gốc
                    photo.setAlbum(album); // Liên kết ảnh với album
                    photoService.save(photo); // Lưu ảnh vào database

                    // Tạo ảnh thumbnail (ảnh thu nhỏ) từ file gốc
                    PhotoViewDTO photoViewDTO = new PhotoViewDTO(photo.getId(), photo.getName(),
                            photo.getDescription());
                    fileNamesWithSuccess.add(photoViewDTO);
                    BufferedImage thumbImg = AppUtil.getThumbnail(file, THUMBNAIL_WIDTH);
                    File thumbnail_location = new File(
                            AppUtil.get_photo_upload_path(final_photo_name, THUMBNAIL_FOLDER_NAME, album_id)); // Tạo
                                                                                                               // đường
                                                                                                               // dẫn
                                                                                                               // cho
                                                                                                               // file
                                                                                                               // thumbnail
                    ImageIO.write(thumbImg, file.getContentType().split("/")[1], thumbnail_location); // Lưu ảnh
                                                                                                      // thumbnail vào
                                                                                                      // file

                } catch (Exception e) {
                    // Nếu xảy ra lỗi khi upload file, ghi log lỗi và thêm tên file vào danh sách
                    // thất bại
                    log.debug(AlbumError.PHOTO_UPLOAD_ERROR.toString() + ": " + e.getMessage());
                    fileNamesWithError.add(file.getOriginalFilename());
                }

            } else {
                // Nếu file không phải là định dạng ảnh hợp lệ, thêm tên file vào danh sách thất
                // bại
                fileNamesWithError.add(file.getOriginalFilename());
            }
        });

        // Tạo kết quả trả về bao gồm danh sách các file upload thành công và thất bại
        HashMap<String, List<?>> result = new HashMap<>();
        result.put("SUCCESS", fileNamesWithSuccess); // Danh sách các file upload thành công
        result.put("ERRORS", fileNamesWithError); // Danh sách các file upload thất bại

        // Đưa kết quả vào một danh sách HashMap và trả về trong ResponseEntity
        List<HashMap<String, List<?>>> response = new ArrayList<>();
        response.add(result);

        return ResponseEntity.ok(response); // Trả về kết quả với mã trạng thái HTTP 200 (OK)
    }

    @GetMapping("albums/{album_id}/photos/{photo_id}/download-photo")
    @SecurityRequirement(name = "studyeasy-demo-api") // Yêu cầu bảo mật cho API
    public ResponseEntity<?> downloadPhoto(@PathVariable("album_id") long album_id,
            @PathVariable("photo_id") long photo_id, Authentication authentication) {
        return downloadFile(album_id, photo_id, PHOTOS_FOLDER_NAME, authentication);
    }

    @GetMapping("albums/{album_id}/photos/{photo_id}/download-thumbnail")
    @SecurityRequirement(name = "studyeasy-demo-api") // Yêu cầu bảo mật cho API
    public ResponseEntity<?> downloadThumbnail(@PathVariable("album_id") long album_id,
            @PathVariable("photo_id") long photo_id, Authentication authentication) {
        return downloadFile(album_id, photo_id, THUMBNAIL_FOLDER_NAME, authentication);
    }

    @PutMapping(value = "/albums/{album_id}/update", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "Please add a valid name for description")
    @ApiResponse(responseCode = "204", description = "Album added")
    @Operation(summary = "Update a Album")
    @SecurityRequirement(name = "studyeasy-demo-api")
    public ResponseEntity<AlbumViewDTO> updateAlbum(@Valid @PathVariable long album_id,
            @RequestBody AlbumPayloadDTO albumPayloadDTO,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();
            Optional<Album> optionaAlbum = albumService.findById(album_id);
            Album album;

            if (optionaAlbum.isPresent()) {
                album = optionaAlbum.get();
                if (account.getId() != album.getAccount().getId()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            album.setName(albumPayloadDTO.getName());
            album.setDescription(albumPayloadDTO.getDescription());
            album = albumService.save(album);
            List<PhotoDTO> photos = new ArrayList<>();
            for (Photo photo : photoService.findByAlbumId(album.getId())) {
                String link = "/albums/" + album.getId() + "/photos/" + photo.getId() + "/download-photo";
                photos.add(new PhotoDTO(photo.getId(), photo.getName(), photo.getDescription(), photo.getFileName(),
                        link));
            }
            AlbumViewDTO albumViewDTO = new AlbumViewDTO(album.getId(), album.getName(), album.getDescription(),
                    photos);
            return ResponseEntity.ok(albumViewDTO);
        } catch (Exception e) {
            log.debug(AlbumError.ADD_ALBUM_ERROR.toString() + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PutMapping(value = "/albums/{album_id}/photos/{photo_id}/update", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "Please add a valid name for description")
    @ApiResponse(responseCode = "204", description = "Photo added")
    @Operation(summary = "Update a photo")
    @SecurityRequirement(name = "studyeasy-demo-api")
    public ResponseEntity<PhotoViewDTO> update_photo(@Valid @RequestBody PhotoPayloadDTO photoPayloadDTO,
            @PathVariable long album_id,
            @PathVariable long photo_id, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();
            Optional<Album> optionaAlbum = albumService.findById(album_id);
            Album album;

            if (optionaAlbum.isPresent()) {
                album = optionaAlbum.get();
                if (account.getId() != album.getAccount().getId()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            Optional<Photo> optionalPhoto = photoService.findById(photo_id);
            if (optionalPhoto.isPresent()) {
                Photo photo = optionalPhoto.get();
                if (photo.getAlbum().getId() != album_id) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
                photo.setName(photoPayloadDTO.getName());
                photo.setDescription(photoPayloadDTO.getDescription());
                photoService.save(photo);
                PhotoViewDTO photoViewDTO = new PhotoViewDTO(photo.getId(), photo.getName(), photo.getDescription());
                return ResponseEntity.ok(photoViewDTO);

            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping(value = "/albums/{album_id}/photos/{photo_id}/delete")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "202", description = "Photo Delete")
    @Operation(summary = "Delete a photo")
    @SecurityRequirement(name = "studyeasy-demo-api")
    public ResponseEntity<String> delete_photo(@PathVariable long album_id, @PathVariable long photo_id,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();
            Optional<Album> optionalAlbum = albumService.findById(album_id);
            Album album;

            if (optionalAlbum.isPresent()) {
                album = optionalAlbum.get();
                if (account.getId() != album.getAccount().getId()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            Optional<Photo> optionalPhoto = photoService.findById(photo_id);
            if (optionalPhoto.isPresent()) {
                Photo photo = optionalPhoto.get();
                if (photo.getAlbum().getId() != album_id) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
                AppUtil.delete_photo_from_path(photo.getFileName(), PHOTOS_FOLDER_NAME, album_id);
                AppUtil.delete_photo_from_path(photo.getFileName(), THUMBNAIL_FOLDER_NAME, album_id);
                photoService.delete(photo);
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);

            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

    }

    @DeleteMapping(value = "/albums/{album_id}/delete")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "202", description = "Album Delete")
    @Operation(summary = "Delete a album")
    @SecurityRequirement(name = "studyeasy-demo-api")
    public ResponseEntity<String> delete_album(@PathVariable long album_id, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();
            Optional<Album> optionalAlbum = albumService.findById(album_id);
            Album album;

            if (optionalAlbum.isPresent()) {
                album = optionalAlbum.get();
                if (account.getId() != album.getAccount().getId()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            for (Photo photo : photoService.findByAlbumId(album.getId())) {
                AppUtil.delete_photo_from_path(photo.getFileName(), PHOTOS_FOLDER_NAME, album_id);
                AppUtil.delete_photo_from_path(photo.getFileName(), THUMBNAIL_FOLDER_NAME, album_id);
                photoService.delete(photo);
            }
            albumService.delete(album);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    public ResponseEntity<?> downloadFile(long album_id,
            long photo_id, String folder_name, Authentication authentication) {
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        Account account = optionalAccount.get();
        Optional<Album> optionaAlbum = albumService.findById(album_id);
        Album album;

        if (optionaAlbum.isPresent()) {
            album = optionaAlbum.get();
            if (account.getId() != album.getAccount().getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        Optional<Photo> optionalPhoto = photoService.findById(photo_id);
        if (optionalPhoto.isPresent()) {
            Photo photo = optionalPhoto.get();
            if (photo.getAlbum().getId() != album_id) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            Resource resource = null;
            try {
                resource = AppUtil.getFileAsResource(album_id, folder_name, photo.getFileName());
            } catch (IOException e) {
                return ResponseEntity.internalServerError().build();
            }
            if (resource == null) {
                return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
            }
            String contentType = "application/octet-stream";
            String headerValue = "attachment; filename=\"" + photo.getOriginalFileName() + "\"";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .body(resource);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

}
