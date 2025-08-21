<template>
  <v-container class="text-center py-12">
    <v-row justify="center">
      <v-col cols="12" md="8">
        <h1 class="text-h1 font-weight-bold mb-4">404</h1>
        <h2 class="text-h4 mb-6">頁面不存在</h2>
        <p class="text-body-1 mb-8">很抱歉，您要尋找的頁面不存在或已被移除。</p>
        
        <v-btn
          color="primary"
          size="large"
          to="/"
          prepend-icon="mdi-home"
        >
          返回首頁
        </v-btn>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
// 404 頁面不需要特殊邏輯
</script>

<style scoped>
/* 可以添加一些自訂樣式 */
</style>