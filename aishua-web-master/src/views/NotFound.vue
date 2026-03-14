<template>
  <div class="not-found-container">
    <h1>404 - 页面未找到</h1>
    <p>抱歉，您访问的页面不存在。</p>
    <router-link to="/login" class="back-link">返回登录页</router-link>
  </div>
</template>

<script>
export default {
  name: 'NotFoundPage'
};
</script>

<style scoped>
.not-found-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.not-found-container h1 {
  color: #333;
  margin-bottom: 1rem;
}

.not-found-container p {
  color: #666;
  margin-bottom: 2rem;
}

.back-link {
  padding: 0.75rem 1.5rem;
  background-color: #409eff;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.back-link:hover {
  background-color: #337ecc;
}
</style>