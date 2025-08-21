<template>
  <v-card
    class="post-card"
    elevation="2"
    :to="{ name: 'post-detail', params: { id: post.id }}"
  >
    <v-card-title class="text-h6">
      {{ post.title }}
      <v-spacer></v-spacer>
      <v-card-subtitle><span>{{ formatDate(post.createdAt) }}</span></v-card-subtitle>
    </v-card-title>

    <v-card-text>
      <div class="text-truncate-3">
        {{ stripHtml(deleteLinesMatching(post.content, /^\s*(\!|#|\[|-|>|\|)/)) }}
      </div>
      
      <div class="mt-3" v-if="post.category">
        <v-chip
          size="small"
          color="primary"
          :to="{ name: 'category', params: { id: post.category.id }}"
          class="mr-2"
        >
          {{ post.category.name }}
        </v-chip>
      </div>
      
      <div class="mt-2 d-flex flex-wrap">
        <v-chip
          v-for="tag in post.tags"
          :key="tag.id"
          size="small"
          variant="outlined"
          :color="tag.color || 'secondary'"
          :to="{ name: 'tag', params: { id: tag.id }}"
          class="mr-1 mb-1"
        >
          {{ tag.name }}
        </v-chip>
      </div>
    </v-card-text>

    <v-card-actions>
      <v-btn
        variant="text"
        color="primary"
        :to="{ name: 'post-detail', params: { id: post.id }}"
      >
        閱讀更多
      </v-btn>
      <v-spacer></v-spacer>
      <v-btn icon>
        <v-icon>mdi-comment-outline</v-icon>
      </v-btn>
      <span class="text-caption mr-2">{{ post.comments?.length || 0 }}</span>
    </v-card-actions>
  </v-card>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'

// IDE 會有未使用的警告，但刪除後會有問題
const props = defineProps({
  post: {
    type: Object,
    required: true
  }
})

function formatDate(dateString) {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-TW', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

function stripHtml(html : string) {
  if (!html) return ''
  // 簡單的HTML標籤移除函數
  const doc = new DOMParser().parseFromString(html, 'text/html')
  return doc.body.textContent || ''
}

/**
 * 刪除符合指定模式的行
 * @param text 原始文本
 * @param pattern 要匹配的正則表達式模式
 * @returns 刪除匹配行後的文本
 */
/**
 * 刪除符合指定模式的行
 * @param text 原始文本
 * @param pattern 要匹配的正則表達式模式
 * @returns 刪除匹配行後的文本
 */
function deleteLinesMatching(text: string, pattern: RegExp): string {
  const result : string = text
      .split('\n')
      .filter(line => !pattern.test(line))
      .slice(0, 5).join('\n');
  return result || ''
}

</script>

<style scoped>
.post-card {
  transition: transform 0.3s, box-shadow 0.3s;
  margin-bottom: 16px;
  width: 100%;
}

.post-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1) !important;
}

.text-truncate-3 {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  -webkit-line-clamp: 3;
  max-height: 4.8em;
}
</style>