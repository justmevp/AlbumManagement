import React, { useState } from 'react'; // Import React và useState từ thư viện React
import TextField from '@mui/material/TextField'; // Import TextField từ Material UI để sử dụng trường nhập liệu
import Button from '@mui/material/Button'; // Import Button từ Material UI để sử dụng nút bấm
import { useEffect } from 'react'; // Import useEffect từ React để sử dụng hiệu ứng khi component mount hoặc cập nhật
import { fetchPostDataWithAuth } from 'client/client'; // Import hàm fetchPostDataWithAuth từ file client để gửi dữ liệu kèm theo xác thực
import { useNavigate } from 'react-router-dom'; // Import useNavigate từ react-router-dom để điều hướng người dùng

const AddAlbumForm = () => {
    const navigate = useNavigate(); // Khởi tạo hook useNavigate để điều hướng người dùng trong ứng dụng
    useEffect(() => {
        const isLoggedIn = localStorage.getItem('token'); // Kiểm tra xem người dùng đã đăng nhập hay chưa qua token
        if (!isLoggedIn) { // Nếu chưa đăng nhập (token không tồn tại)
          navigate('/login'); // Điều hướng người dùng đến trang đăng nhập
          window.location.reload(); // Tải lại trang sau khi điều hướng
        } 
      }, []); // useEffect chỉ chạy một lần khi component được mount (mảng phụ thuộc rỗng)

    const [formData, setFormData] = useState({ // Khởi tạo state formData để lưu dữ liệu từ form
    name: '', // Giá trị mặc định cho trường "name" là chuỗi rỗng
    description: '', // Giá trị mặc định cho trường "description" là chuỗi rỗng
    });

  const [errors, setErrors] = useState({ // Khởi tạo state errors để lưu các thông báo lỗi của form
    name: '', // Mặc định không có lỗi cho trường "name"
    description: '', // Mặc định không có lỗi cho trường "description"
  });

  const handleInputChange = (e) => { // Hàm xử lý khi người dùng thay đổi dữ liệu trong form
    const { name, value } = e.target; // Lấy tên và giá trị của trường nhập liệu
    setFormData((prevData) => ({ // Cập nhật lại formData với giá trị mới của trường nhập liệu
      ...prevData, // Giữ lại các giá trị cũ
      [name]: value, // Cập nhật giá trị cho trường đang được thay đổi
    }));
  };

  const handleSubmit = async (e) => { // Hàm xử lý khi người dùng submit form
    e.preventDefault(); // Ngăn hành vi mặc định của form (không tải lại trang)

    // Validation - Kiểm tra dữ liệu hợp lệ
    let isValid = true; // Biến cờ để theo dõi form có hợp lệ hay không
    const newErrors = { name: '', description: '' }; // Khởi tạo đối tượng lưu lỗi mới

    if (!formData.name.trim()) { // Nếu trường "name" trống hoặc chỉ chứa khoảng trắng
      newErrors.name = 'Name is required'; // Đặt lỗi cho trường "name"
      isValid = false; // Form không hợp lệ
    }

    if (!formData.description.trim()) { // Nếu trường "description" trống hoặc chỉ chứa khoảng trắng
      newErrors.description = 'Description is required'; // Đặt lỗi cho trường "description"
      isValid = false; // Form không hợp lệ
    }

    setErrors(newErrors); // Cập nhật state errors với các lỗi mới

    // Nếu form hợp lệ, tiếp tục thực hiện các hành động khác
    if (isValid) {
        const payload = { // Tạo payload chứa dữ liệu cần gửi lên server
            name: formData.name, // Trường "name" với giá trị từ formData
            description: formData.description, // Trường "description" với giá trị từ formData
          };

      fetchPostDataWithAuth("/albums/add", payload) // Gọi API gửi dữ liệu lên server để thêm album
      .then((response) => { // Xử lý khi có phản hồi từ server
        console.log(response); // In ra phản hồi từ server để kiểm tra
      })
      .catch((error) => { // Xử lý lỗi khi API gặp sự cố
          console.error('Login error:', error); // In lỗi ra console
    });

      console.log('Form submitted:'); // In thông báo khi form được submit
      navigate('/'); // Điều hướng người dùng về trang chủ sau khi thêm album thành công
    }
  };

  return (
    <form onSubmit={handleSubmit}> {/* Khi form được submit, gọi hàm handleSubmit */}
      <TextField
        fullWidth // Trường nhập liệu chiếm toàn bộ chiều rộng
        label="Name" // Nhãn của trường nhập liệu
        variant="outlined" // Kiểu dáng của trường nhập liệu
        name="name" // Tên của trường nhập liệu
        value={formData.name} // Giá trị của trường nhập liệu lấy từ formData
        onChange={handleInputChange} // Gọi hàm handleInputChange khi giá trị thay đổi
        error={!!errors.name} // Hiển thị lỗi nếu có
        helperText={errors.name} // Hiển thị thông báo lỗi cho người dùng
        margin="normal" // Cách lề mặc định của Material UI
      />

      <TextField
        fullWidth // Trường nhập liệu chiếm toàn bộ chiều rộng
        label="Description" // Nhãn của trường nhập liệu
        variant="outlined" // Kiểu dáng của trường nhập liệu
        name="description" // Tên của trường nhập liệu
        value={formData.description} // Giá trị của trường nhập liệu lấy từ formData
        onChange={handleInputChange} // Gọi hàm handleInputChange khi giá trị thay đổi
        error={!!errors.description} // Hiển thị lỗi nếu có
        helperText={errors.description} // Hiển thị thông báo lỗi cho người dùng
        multiline // Cho phép nhập nhiều dòng văn bản
        rows={4} // Số dòng hiển thị trong trường nhập liệu
        margin="normal" // Cách lề mặc định của Material UI
      />

      <Button type="submit" variant="contained" color="primary"> {/* Nút bấm submit form */}
        Add Album {/* Nội dung hiển thị trên nút */}
      </Button>
    </form>
  );
};

export default AddAlbumForm; // Xuất component AddAlbumForm để sử dụng ở nơi khác
