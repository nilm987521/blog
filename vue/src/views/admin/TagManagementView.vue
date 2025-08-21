<template>
  <div>
    <div class="d-flex justify-space-between align-center mb-4">
      <h2 class="text-h5">標籤管理</h2>
      <v-btn 
        color="primary" 
        @click="openTagDialog()" 
        prepend-icon="mdi-plus"
      >
        新增標籤
      </v-btn>
    </div>
    
    <v-data-table
      :headers="headers"
      :items="tags"
      :loading="loading"
      class="elevation-1"
    >
      <template v-slot:item.color="{ item }">
        <div class="d-flex align-center">
          <v-chip
            :color="item.color || 'primary'"
            variant="outlined"
            size="small"
            class="mr-2"
          >
            {{ item.name }}
          </v-chip>
          <span>{{ item.color || 'primary' }}</span>
        </div>
      </template>
      
      <template v-slot:item.actions="{ item }">
        <v-btn
          icon
          size="small"
          color="primary"
          @click="openTagDialog(item)"
        >
          <v-icon>mdi-pencil</v-icon>
        </v-btn>
        <v-btn
          icon
          size="small"
          color="error"
          @click="confirmDeleteTag(item)"
          class="ml-2"
        >
          <v-icon>mdi-delete</v-icon>
        </v-btn>
      </template>
    </v-data-table>
    
    <!-- 標籤編輯對話框 -->
    <v-dialog v-model="tagDialog" max-width="500px">
      <v-card>
        <v-card-title>
          <span>{{ editedId ? '編輯標籤' : '新增標籤' }}</span>
        </v-card-title>
        
        <v-card-text>
          <v-form ref="tagForm" @submit.prevent="saveTag">
            <v-text-field
              v-model="editedTag.name"
              label="標籤名稱"
              required
              :rules="[v => !!v || '請輸入標籤名稱']"
            ></v-text-field>
            
            <v-label class="text-subtitle-1 mb-2 d-block">標籤顏色</v-label>
            
            <!-- 顏色選擇器 -->
            <div class="custom-color-picker mb-4">
              <div class="d-flex flex-wrap gap-1 mb-2">
                <div 
                  v-for="color in defaultColors" 
                  :key="color"
                  class="color-swatch"
                  :style="{ backgroundColor: color, outline: editedTag.color === color ? '2px solid black' : 'none' }"
                  @click="editedTag.color = color"
                ></div>
              </div>
              
              <v-text-field
                v-model="editedTag.color"
                label="自定義色碼 (HEX)"
                hint="例如: #1976D2"
                persistent-hint
                prepend-inner-icon="mdi-palette"
                :rules="[v => /^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$/.test(v) || '請輸入有效的十六進制顏色代碼']"
              ></v-text-field>
            </div>
            
            <v-chip
              :color="editedTag.color || 'primary'"
              variant="outlined"
              size="small"
              class="mb-4"
            >
              {{ editedTag.name || '標籤預覽' }}
            </v-chip>
          </v-form>
        </v-card-text>
        
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="secondary" text @click="tagDialog = false">取消</v-btn>
          <v-btn color="primary" @click="saveTag">保存</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    
    <!-- 刪除確認對話框 -->
    <v-dialog v-model="deleteDialog" max-width="400px">
      <v-card>
        <v-card-title>確認刪除</v-card-title>
        <v-card-text>
          您確定要刪除標籤 <strong>{{ editedTag.name }}</strong> 嗎？此操作無法撤銷。
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="secondary" text @click="deleteDialog = false">取消</v-btn>
          <v-btn color="error" @click="deleteTag">確認刪除</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    
    <!-- 提示訊息 -->
    <v-snackbar
      v-model="snackbar.show"
      :color="snackbar.color"
      :timeout="3000"
    >
      {{ snackbar.text }}
      <template v-slot:actions>
        <v-btn
          variant="text"
          @click="snackbar.show = false"
        >
          關閉
        </v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import TagService from '@/services/tag.service'

// 表格標頭
const headers = [
  { title: 'ID', key: 'id', width: '80px' },
  { title: '標籤名稱', key: 'name' },
  { title: '顏色', key: 'color' },
  { title: '操作', key: 'actions', sortable: false, width: '150px' }
]

// 預設顏色選項
const defaultColors = [
  '#1976D2', // primary
  '#E91E63', // pink
  '#9C27B0', // purple 
  '#673AB7', // deep-purple
  '#3F51B5', // indigo
  '#2196F3', // blue
  '#03A9F4', // light-blue
  '#00BCD4', // cyan
  '#009688', // teal
  '#4CAF50', // green
  '#8BC34A', // light-green
  '#CDDC39', // lime
  '#FFEB3B', // yellow
  '#FFC107', // amber
  '#FF9800', // orange
  '#FF5722', // deep-orange
  '#795548', // brown
  '#607D8B', // blue-grey
]

// 數據和狀態
const tags = ref([])
const loading = ref(false)
const tagDialog = ref(false)
const deleteDialog = ref(false)
const editedId = ref(null)
const editedTag = ref({
  name: '',
  color: '#1976D2' // 預設藍色
})
const snackbar = ref({
  show: false,
  text: '',
  color: 'success'
})
const tagForm = ref(null)

// 生命週期鉤子
onMounted(async () => {
  await loadTags()
})

// 方法
async function loadTags() {
  loading.value = true
  try {
    tags.value = await TagService.getAllTags()
  } catch (error) {
    showSnackbar('載入標籤失敗', 'error')
    console.error('載入標籤失敗:', error)
  } finally {
    loading.value = false
  }
}

function openTagDialog(tag = null) {
  if (tag) {
    editedId.value = tag.id
    editedTag.value = { ...tag }
  } else {
    editedId.value = null
    editedTag.value = {
      name: '',
      color: '#1976D2'
    }
  }
  tagDialog.value = true
}

async function saveTag() {
  // 表單驗證
  const form = tagForm.value
  if (form && !form.validate()) return

  loading.value = true
  try {
    if (editedId.value) {
      // 更新現有標籤
      await TagService.updateTag(editedId.value, editedTag.value)
      showSnackbar('標籤更新成功')
    } else {
      // 創建新標籤
      await TagService.createTag(editedTag.value)
      showSnackbar('標籤建立成功')
    }
    tagDialog.value = false
    await loadTags()
  } catch (error) {
    showSnackbar('操作失敗: ' + error.message, 'error')
    console.error('保存標籤失敗:', error)
  } finally {
    loading.value = false
  }
}

function confirmDeleteTag(tag) {
  editedId.value = tag.id
  editedTag.value = { ...tag }
  deleteDialog.value = true
}

async function deleteTag() {
  loading.value = true
  try {
    await TagService.deleteTag(editedId.value)
    deleteDialog.value = false
    showSnackbar('標籤刪除成功')
    await loadTags()
  } catch (error) {
    showSnackbar('刪除失敗: ' + error.message, 'error')
    console.error('刪除標籤失敗:', error)
  } finally {
    loading.value = false
  }
}

function showSnackbar(text, color = 'success') {
  snackbar.value = {
    show: true,
    text,
    color
  }
}
</script>

<style scoped>
.custom-color-picker {
  margin-bottom: 16px;
}

.color-swatch {
  display: inline-block;
  width: 30px;
  height: 30px;
  margin: 3px;
  cursor: pointer;
  border-radius: 4px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.color-swatch:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.gap-1 {
  gap: 8px;
}
</style>