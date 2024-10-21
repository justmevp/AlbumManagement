package org.studyeasy.SpringRestDemo.util.AppUtils;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;

public class AppUtil {
    public static String PATH = "src\\main\\resources\\static\\uploads\\" ;

    // Phương thức này trả về đường dẫn lưu trữ hình ảnh dựa trên tên file, tên thư
    // mục và ID album
    // - fileName: tên file của ảnh.
    // - folder_name: tên thư mục con mà ảnh sẽ được lưu vào (có thể là "thumbnail",
    // "original", v.v.)
    // - album_id: ID của album để tạo thư mục lưu trữ tương ứng.
    public static String get_photo_upload_path(String fileName, String folder_name, long album_id) throws IOException {
        // Tạo đường dẫn tới thư mục lưu trữ file ảnh theo cấu trúc:
        // "src/main/resources/static/uploads/album_id/folder_name"
        String path = PATH + album_id + "\\" + folder_name;

        // Tạo thư mục nếu chưa tồn tại
        Files.createDirectories(Paths.get(path));

        // Trả về đường dẫn tuyệt đối của file ảnh bao gồm tên file
        return new File(path).getAbsolutePath() + "\\" + fileName;
    }

    public static boolean delete_photo_from_path(String filename, String folder_name, long album_id){
        try{
            File f = new File(PATH + album_id + "\\" + folder_name + "\\" + filename);
            if (f.delete()) {
                return true;
            }else{
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    // Phương thức này trả về hình ảnh thumbnail từ file gốc với chiều rộng (width)
    // được chỉ định.
    // - orginalFile: file ảnh gốc được tải lên dưới dạng MultipartFile.
    // - width: chiều rộng mong muốn của ảnh thumbnail.
    public static BufferedImage getThumbnail(MultipartFile orginalFile, Integer width) throws IOException {
        BufferedImage thumbImg = null; // Biến lưu trữ hình ảnh thumbnail
        BufferedImage img = ImageIO.read(orginalFile.getInputStream()); // Đọc ảnh gốc từ file input dưới dạng
                                                                        // BufferedImage

        // Sử dụng thư viện Scalr để thay đổi kích thước ảnh (resize) theo chiều rộng
        // mong muốn.
        // - Scalr.Method.AUTOMATIC: Phương pháp resize tự động, tối ưu hóa cho hiệu
        // suất.
        // - Scalr.Mode.AUTOMATIC: Chế độ resize tự động (không bắt buộc theo tỷ lệ cụ
        // thể).
        // - width: Chiều rộng mà ảnh thumbnail sẽ có, chiều cao sẽ được tính theo tỷ
        // lệ.
        // - Scalr.OP_ANTIALIAS: Thêm hiệu ứng làm mịn để giảm răng cưa trên ảnh thu
        // nhỏ.
        thumbImg = Scalr.resize(img, Scalr.Method.AUTOMATIC, Scalr.Mode.AUTOMATIC, width, Scalr.OP_ANTIALIAS);

        // Trả về ảnh thumbnail đã được resize
        return thumbImg;
    }

   // Phương thức trả về một file dưới dạng tài nguyên (Resource), dựa trên ID album, tên thư mục và tên file
// Nếu file không tồn tại, trả về null
public static Resource getFileAsResource(long album_id, String folder_name, String file_name) throws IOException {

    // Xây dựng đường dẫn tới file bằng cách kết hợp album_id, folder_name và file_name
    String location = "src\\main\\resources\\static\\uploads\\" + album_id + "\\" + folder_name + "\\" + file_name;

    // Tạo đối tượng File dựa trên đường dẫn đã xây dựng
    File file = new File(location);

    // Kiểm tra nếu file tồn tại
    if (file.exists()) {
        // Lấy đường dẫn tuyệt đối của file và chuyển đổi nó thành đối tượng Path
        Path path = Paths.get(file.getAbsolutePath());
        
        // Trả về đối tượng UrlResource, đại diện cho tài nguyên file, từ URI của đường dẫn
        return new UrlResource(path.toUri());
    } else {
        // Nếu file không tồn tại, trả về null
        return null;
    }
}

}
