<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'

onMounted(() => {
  document.documentElement.classList.add('dark')
})
</script>

<style>
/* 全局样式已在 assets/styles.css 中定义 */
</style>