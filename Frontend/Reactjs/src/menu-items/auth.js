// assets
import { LoginOutlined, ProfileOutlined, LogoutOutlined } from '@ant-design/icons';

// icons
const icons = {
  LoginOutlined,
  ProfileOutlined,
  LogoutOutlined
};
const isLoggedInEnabled = localStorage.getItem('token');

// ==============================|| MENU ITEMS - EXTRA PAGES ||============================== //

const caseLogin = [
  {
    id: 'logout1',
    title: 'Logout',
    type: 'item',
    url: '/logout',
    icon: icons.LogoutOutlined,
    target: true
  }
];
const caseLogout = [
  {
    id: 'Login',
    title: 'Login',
    type: 'item',
    url: '/login',
    icon: icons.LoginOutlined,
    target: true
  },
  {
    id: 'resgister1',
    title: 'Register',
    type: 'item',
    url: '/register',
    icon: icons.ProfileOutlined,
    target: true
  }
];

const auth = {
  id: 'authentication',
  title: 'Authentication',
  type: 'group',
  children: [
    isLoggedInEnabled && caseLogin[0], !isLoggedInEnabled && caseLogout[0], !isLoggedInEnabled && caseLogout[1]
].filter(Boolean)
};

export default auth;
