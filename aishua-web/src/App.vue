<template>
  <div id="app">
    <component :is="layoutComponent">
      <router-view />
    </component>
  </div>
</template>

<script>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import EmptyLayout from './layout/EmptyLayout.vue';
import DefaultLayout from './layout/DefaultLayout.vue';

export default {
  name: 'App',
  components: {
    EmptyLayout,
    DefaultLayout
  },
  setup() {
    const route = useRoute();
    
    const layoutComponent = computed(() => {
      // 根据路由meta信息选择布局
      const layout = route.meta.layout || 'default';
      return layout === 'empty' ? 'EmptyLayout' : 'DefaultLayout';
    });

    return {
      layoutComponent
    };
  }
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  margin: 0;
  padding: 0;
}
</style>
