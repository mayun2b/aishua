<template>
  <div class="empty-layout">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'EmptyLayout'
};
</script>

<style scoped>
.empty-layout {
  width: 100%;
  height: 100%;
}
</style>