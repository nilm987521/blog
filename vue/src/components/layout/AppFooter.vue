<template>
  <v-footer
    app
    padless
    color="primary"
    dark
  >
    <v-container>
      <v-row>
        <v-col cols="12">
          <h3 class="text-h6 mb-4">免責聲明</h3>
          <p>本網站所分享之所有技術文章、教學內容與程式碼，均為個人學習與經驗紀錄，僅供參考。雖力求準確與完整，惟資訊隨技術演進可能有所變動，本站不對任何因內容使用所造成之直接或間接損失負責。</p>
          <p>使用者在實作與應用前，請務必依實際需求自行驗證與測試。如有侵權或不當引用之處，歡迎來信指正，本站將儘速處理。</p>
        </v-col>
      </v-row>

    </v-container>
  </v-footer>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import TagService from '@/services/tag.service'

const popularTags = ref([])

onMounted(async () => {
  try {
    const response = await TagService.getAllTags()
    // 假設我們有某種方式來確定最受歡迎的標籤
    // 在真實情況下，可能需要一個專門的 API 端點來獲取最受歡迎的標籤
    popularTags.value = response.slice(0, 10) // 僅使用前 10 個標籤作為示例
  } catch (error) {
    console.error('獲取標籤失敗:', error)
  }
})
</script>

<style scoped>
.text-h6 {
  font-size: 1rem;
  font-weight: 500;
  margin-bottom: 15px;
}

.v-chip {
  margin: 0.25rem;
}
</style>