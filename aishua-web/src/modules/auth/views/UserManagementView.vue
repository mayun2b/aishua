<template>
  <div class="user-management-container">
    <h2>用户管理</h2>
    
    <!-- 搜索表单 -->
    <div class="search-form">
      <van-cell-group inset>
        <van-field 
          v-model="searchForm.phone" 
          label="手机号" 
          placeholder="请输入手机号" 
          clearable
        />
        <van-field 
          v-model="searchForm.nickname" 
          label="昵称" 
          placeholder="请输入昵称" 
          clearable
        />
        <van-field label="状态" readonly clickable @click="showStatusPicker = true">
          <template #input>
            <span>{{ statusMap[searchForm.status] || '全部' }}</span>
          </template>
        </van-field>
      </van-cell-group>
      
      <div class="form-actions">
        <van-button 
          type="primary" 
          @click="handleSearch" 
          :loading="loading"
          block
        >
          搜索
        </van-button>
        <van-button 
          plain 
          @click="handleReset" 
          :disabled="loading"
          block
        >
          重置
        </van-button>
      </div>
    </div>
    
    <!-- 用户列表 -->
    <div class="user-list">
      <van-cell-group inset>
        <van-cell title="用户ID" />
        <van-cell title="手机号" />
        <van-cell title="昵称" />
        <van-cell title="状态" />
        <van-cell title="角色" />
        <van-cell title="创建时间" />
        <van-cell title="操作" />
        
        <van-cell 
          v-for="user in users" 
          :key="user.id"
          :title="user.id.toString()"
        >
          <template #default>
            <div class="user-item-content">
              <span>{{ user.phone }}</span>
              <span>{{ user.nickname }}</span>
              <span>{{ user.status === 1 ? '启用' : '禁用' }}</span>
              <span>{{ user.isAdmin ? '管理员' : '普通用户' }}</span>
              <span>{{ formatDate(user.createTime) }}</span>
              <span class="actions">
                <van-button 
                  v-if="!user.isAdmin && user.status === 1" 
                  size="small"
                  type="danger"
                  @click="handleDisableUser(user.id)" 
                  :loading="disablingUserId === user.id"
                >
                  {{ disablingUserId === user.id ? '禁用中...' : '禁用' }}
                </van-button>
                <van-button 
                  v-else-if="!user.isAdmin && user.status === 0"
                  size="small"
                  type="success"
                  @click="handleEnableUser"
                  :loading="enablingUserId === user.id"
                >
                  {{ enablingUserId === user.id ? '启用中...' : '启用' }}
                </van-button>
                <span v-else class="admin-tag">管理员</span>
              </span>
            </div>
          </template>
        </van-cell>
      </van-cell-group>
      
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="users.length === 0" class="empty">暂无用户数据</div>
    </div>
    
    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <van-pagination
        v-model="currentPage"
        :page-count="totalPages"
        :force-events="true"
        @change="handlePageChange"
      />
    </div>
    
    <!-- 状态选择器 -->
    <van-popup v-model="showStatusPicker" position="bottom">
      <van-picker
        :columns="statusOptions"
        @confirm="onStatusConfirm"
        @cancel="showStatusPicker = false"
      />
    </van-popup>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { listUsers, disableUser } from '../api/auth';

export default {
  name: 'UserManagementView',
  setup() {
    // 搜索表单
    const searchForm = ref({
      phone: '',
      nickname: '',
      status: '',
      page: 1,
      size: 10
    });
    
    // 状态选择器
    const showStatusPicker = ref(false);
    const statusOptions = [
      { text: '全部', value: '' },
      { text: '启用', value: '1' },
      { text: '禁用', value: '0' }
    ];
    const statusMap = {
      '': '全部',
      '1': '启用',
      '0': '禁用'
    };
    
    // 用户列表数据
    const users = ref([]);
    const total = ref(0);
    const currentPage = ref(1);
    const totalPages = ref(0);
    const loading = ref(false);
    const disablingUserId = ref(null);
    const enablingUserId = ref(null);
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN');
    };
    
    // 获取用户列表
    const fetchUsers = async () => {
      loading.value = true;
      try {
        const response = await listUsers({
          phone: searchForm.value.phone || undefined,
          nickname: searchForm.value.nickname || undefined,
          status: searchForm.value.status !== '' ? parseInt(searchForm.value.status) : undefined,
          page: searchForm.value.page,
          size: searchForm.value.size
        });
        
        if (response.code === 200) {
          users.value = response.data.records || [];
          total.value = response.data.total || 0;
          currentPage.value = response.data.current || 1;
          totalPages.value = response.data.pages || 1;
        } else {
          console.error('获取用户列表失败:', response.message);
          users.value = [];
          total.value = 0;
          totalPages.value = 0;
          if (window.$toast) {
            window.$toast.fail('获取用户列表失败: ' + response.message);
          } else {
            alert('获取用户列表失败: ' + response.message);
          }
        }
      } catch (error) {
        console.error('获取用户列表异常:', error);
        users.value = [];
        total.value = 0;
        totalPages.value = 0;
        if (window.$toast) {
          window.$toast.fail('网络错误，请稍后重试');
        } else {
          alert('网络错误，请稍后重试');
        }
      } finally {
        loading.value = false;
      }
    };
    
    // 搜索
    const handleSearch = () => {
      searchForm.value.page = 1;
      fetchUsers();
    };
    
    // 重置
    const handleReset = () => {
      searchForm.value = {
        phone: '',
        nickname: '',
        status: '',
        page: 1,
        size: 10
      };
      fetchUsers();
    };
    
    // 状态选择确认
    const onStatusConfirm = (value) => {
      searchForm.value.status = value.value;
      showStatusPicker.value = false;
    };
    
    // 禁用用户
    const handleDisableUser = async (userId) => {
      if (!confirm('确定要禁用该用户吗？禁用后用户将无法登录系统！')) {
        return;
      }
      
      disablingUserId.value = userId;
      try {
        const response = await disableUser(userId);
        if (response.code === 200 && response.data === true) {
          if (window.$toast) {
            window.$toast.success('用户禁用成功！');
          } else {
            alert('用户禁用成功！');
          }
          fetchUsers();
        } else {
          if (window.$toast) {
            window.$toast.fail('禁用失败：' + (response.message || '未知错误'));
          } else {
            alert('禁用失败：' + (response.message || '未知错误'));
          }
        }
      } catch (error) {
        console.error('禁用用户异常:', error);
        if (window.$toast) {
          window.$toast.fail('禁用失败：网络错误');
        } else {
          alert('禁用失败：网络错误');
        }
      } finally {
        disablingUserId.value = null;
      }
    };
    
    // 启用用户（如果需要的话，可以后续实现）
    const handleEnableUser = async () => {
      if (window.$toast) {
        window.$toast.info('启用功能暂未实现，请联系管理员');
      } else {
        alert('启用功能暂未实现，请联系管理员');
      }
    };
    
    // 分页切换
    const handlePageChange = (page) => {
      if (page < 1 || page > totalPages.value) return;
      searchForm.value.page = page;
      fetchUsers();
    };
    
    // 初始化加载
    onMounted(() => {
      fetchUsers();
    });
    
    return {
      searchForm,
      users,
      total,
      currentPage,
      totalPages,
      loading,
      disablingUserId,
      enablingUserId,
      showStatusPicker,
      statusOptions,
      statusMap,
      formatDate,
      handleSearch,
      handleReset,
      onStatusConfirm,
      handleDisableUser,
      handleEnableUser,
      handlePageChange
    };
  }
};
</script>

<style scoped>
.user-management-container {
  padding: 20px;
}

.user-management-container h2 {
  margin-bottom: 20px;
  color: #333;
}

.search-form {
  margin-bottom: 20px;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.form-actions .van-button {
  flex: 1;
}

.user-list {
  margin-bottom: 20px;
}

.user-item-content {
  display: grid;
  grid-template-columns: 120px 100px 60px 80px 150px 100px;
  gap: 10px;
  align-items: center;
}

.loading,
.empty {
  text-align: center;
  padding: 20px;
  color: #666;
}

.actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.admin-tag {
  color: #409eff;
  font-weight: bold;
}

.pagination {
  display: flex;
  justify-content: center;
}
</style>