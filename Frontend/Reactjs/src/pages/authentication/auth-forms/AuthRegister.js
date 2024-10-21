import React, { useEffect, useState } from 'react'; // Import React và các hook useState, useEffect để quản lý state và tác động phụ
import { Button, TextField, Container } from '@mui/material'; // Import các component từ Material-UI để xây dựng giao diện người dùng (Button, TextField, Container)
import { fetchPostData } from 'client/client'; // Import hàm fetchPostData từ file client để gọi API đăng nhập
import { useNavigate } from 'react-router-dom'; // Import hook useNavigate từ react-router-dom để điều hướng trang sau khi đăng nhập thành công

const AuthRegister = () => {
  // Khai báo các state: email, password, và errors để lưu lỗi của từng field (email và password)
  const [email, setEmail] = useState(''); // State lưu giá trị email người dùng nhập
  const [password, setPassword] = useState(''); // State lưu giá trị password người dùng nhập
  const [errors, setErrors] = useState({ email: '', password: '' }); // State để lưu thông tin lỗi của email và password
  const [loginError, setLoginError] = useState(''); // State để lưu lỗi khi đăng nhập thất bại
  const navigate = useNavigate(); // Hook điều hướng để chuyển trang khi đăng nhập thành công

  useEffect(() => {
    const isLoggedIn = localStorage.getItem('token'); // Kiểm tra xem token đã lưu trong localStorage chưa (người dùng đã đăng nhập trước đó)
    if (isLoggedIn) {
      navigate('/'); // Nếu đã có token, điều hướng về trang chủ ("/")
      window.location.reload()

    }
  }, []); // useEffect chỉ chạy một lần khi component được render lần đầu tiên

  // Hàm validate email, sử dụng regex để kiểm tra định dạng email có hợp lệ hay không
  const validateEmail = () => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // Regular Expression kiểm tra định dạng email
    return emailRegex.test(email); // Trả về true nếu email hợp lệ, false nếu không hợp lệ
  };

  // Hàm validate password, kiểm tra độ dài password phải từ 6 đến 15 ký tự
  const validatePassword = () => {
    return password.length >= 6 && password.length <= 15; // Trả về true nếu password hợp lệ, false nếu không
  };

  // Hàm handleLogin xử lý khi người dùng nhấn nút Login
  const handleLogin = async () => {
    // Reset lại trạng thái lỗi trước khi kiểm tra
    setErrors({ email: '', password: '' });

    // Kiểm tra email, nếu không hợp lệ thì set thông báo lỗi cho email
    if (!validateEmail()) {
      setErrors((prevErrors) => ({ ...prevErrors, email: 'Invalid email format' })); // Cập nhật lỗi cho email
      return; // Dừng lại nếu email không hợp lệ
    }

    // Kiểm tra password, nếu không hợp lệ thì set thông báo lỗi cho password
    if (!validatePassword()) {
      setErrors((prevErrors) => ({ ...prevErrors, password: 'Password must be at least 6 characters' })); // Cập nhật lỗi cho password
      return; // Dừng lại nếu password không hợp lệ
    }

    // Gọi API đăng nhập với email và password
    fetchPostData('/auth/users/add', { email, password }) 
      .then(() => {
        setLoginError(''); // Xóa lỗi đăng nhập nếu có
        navigate('/login'); // Điều hướng về trang chủ sau khi đăng nhập thành công
        window.location.reload()

      })
      .catch((error) => {
        console.error('Login error:', error); // Ghi log lỗi nếu xảy ra vấn đề
        setLoginError('An error occured during login'); // Cập nhật thông báo lỗi nếu có vấn đề khi đăng nhập
      });
  };

  // JSX (giao diện) của form login
  return (
    <Container component="main" maxWidth="xs"> 
      {/* Tạo một container giới hạn độ rộng là xs (kích thước nhỏ nhất) */}
      <TextField
        variant="outlined" // Kiểu hiển thị của input là outlined
        margin="normal" // Khoảng cách margin giữa các phần tử
        fullWidth // Input chiếm toàn bộ chiều rộng container
        label="Email" // Nhãn của input
        value={email} // Giá trị hiện tại của email (lấy từ state)
        onChange={(e) => setEmail(e.target.value)} // Cập nhật state email khi người dùng nhập
        error={!!errors.email} // Hiển thị lỗi nếu có lỗi email
        helperText={errors.email} // Text phụ trợ hiển thị lỗi email
      />
      <TextField
        variant="outlined"
        margin="normal"
        fullWidth
        label="Password" // Nhãn của input
        type="password" // Input kiểu password (che dấu ký tự nhập vào)
        value={password} // Giá trị hiện tại của password (lấy từ state)
        onChange={(e) => setPassword(e.target.value)} // Cập nhật state password khi người dùng nhập
        error={!!errors.password} // Hiển thị lỗi nếu có lỗi password
        helperText={errors.password} // Text phụ trợ hiển thị lỗi password
      />
      <Button
        variant="contained" // Nút dạng chứa (nền được tô màu)
        color="primary" // Màu sắc của nút là màu chủ đạo (primary)
        fullWidth // Nút chiếm toàn bộ chiều rộng container
        onClick={handleLogin} // Khi nhấn nút, gọi hàm handleLogin để xử lý
      >
        Register {/* Nội dung hiển thị trên nút */}
      </Button>
      {loginError && <p style={{ color: 'red' }}>{loginError}</p>} 
      {/* Nếu có lỗi đăng nhập, hiển thị thông báo lỗi với màu chữ đỏ */}
    </Container>
  );
};

export default AuthRegister; // Xuất component AuthLogin để có thể import và sử dụng ở nơi khác
