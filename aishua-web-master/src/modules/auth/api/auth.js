import request from '@/utils/request';

// 登录接口
export const login = (loginData) => {
  return request.post('/user/login', loginData);
};

// 注册接口
export const register = (registerData) => {
  return request.post('/user/register', registerData);
};

// 管理员查询用户列表接口
export const listUsers = (queryData) => {
  return request.post('/user/list', queryData);
};

// 管理员禁用用户接口（将status设为0）
export const disableUser = (userId) => {
  return request.post('/user/disable', { userId });
};

// 获取用户信息
export const getUserInfo = () => {
  return request.get('/user/info');
};

// 退出登录
export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('userId');
  localStorage.removeItem('username');
  localStorage.removeItem('isAdmin');
};