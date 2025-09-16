<template>
  <v-container style="padding-top: 100px;margin-bottom: 200px">
    <h1 class="text-h3 mb-6">管理面板</h1>

    <v-tabs
        v-model="tab"
        bg-color="primary"
    >
      <v-tab value="users">用戶管理</v-tab>
      <v-tab value="posts">文章管理</v-tab>
      <v-tab value="categories">分類管理</v-tab>
      <v-tab value="tags">標籤管理</v-tab>
      <v-tab value="comments">評論管理</v-tab>
    </v-tabs>

    <v-window v-model="tab" class="mt-6">
      <!-- 用戶管理 -->
      <v-window-item value="users">
        <h2 class="text-h5 mb-4">用戶管理</h2>
        <v-data-table
            :headers="userHeaders"
            :items="users"
            :loading="userLoading"
            class="elevation-1"
        >
          <template #[`item.roles`]="{ item }">
            <v-chip
                v-for="role in item.roles"
                :key="role"
                :color="role === 'ROLE_ADMIN' ? 'error' : 'primary'"
                class="mr-1"
                size="small"
            >
              {{ formatRole(role) }}
            </v-chip>
          </template>

          <template #[`item.actions`]="{ item }">
            <v-btn
                icon
                size="small"
                color="primary"
                @click="editUser(item)"
            >
              <v-icon>mdi-pencil</v-icon>
            </v-btn>
            <v-btn
                icon
                size="small"
                color="error"
                class="ml-2"
                @click="confirmDeleteUser(item)"
            >
              <v-icon>mdi-delete</v-icon>
            </v-btn>
          </template>
        </v-data-table>
      </v-window-item>

      <!-- 文章管理 -->
      <v-window-item value="posts">
        <div class="d-flex justify-space-between align-center mb-4">
          <h2 class="text-h5">文章管理</h2>
          <v-row align="center" class="ml-auto">
            <v-col cols="auto">
              <v-select
                v-model="postFilter"
                :items="postFilterOptions"
                label="篩選狀態"
                variant="outlined"
                density="compact"
                style="min-width: 150px;"
                @update:model-value="filterPosts"
              />
            </v-col>
          </v-row>
        </div>
        <v-data-table
            :headers="postHeaders"
            :items="filteredPosts"
            :loading="postLoading"
            class="elevation-1"
        >
          <template #[`item.published`]="{ item }">
            <v-chip
                :color="item.published ? 'success' : 'grey'"
                size="small"
            >
              {{ item.published ? '已發布' : '草稿' }}
            </v-chip>
          </template>

          <template #[`item.createdAt`]="{ item }">
            {{ formatDate(item.createdAt) }}
          </template>

          <template #[`item.actions`]="{ item }">
            <v-btn
                icon
                size="small"
                color="info"
                :to="{ name: 'post-detail', params: { id: item.id } }"
            >
              <v-icon>mdi-eye</v-icon>
            </v-btn>
            <v-btn
                icon
                size="small"
                color="primary"
                class="ml-2"
                :to="{ name: 'post-edit', params: { id: item.id } }"
            >
              <v-icon>mdi-pencil</v-icon>
            </v-btn>
            <v-btn
                icon
                size="small"
                color="error"
                class="ml-2"
                @click="confirmDeletePost(item)"
            >
              <v-icon>mdi-delete</v-icon>
            </v-btn>
          </template>
        </v-data-table>
      </v-window-item>

      <!-- 分類管理 -->
      <v-window-item value="categories">
        <div class="d-flex justify-space-between align-center mb-4">
          <h2 class="text-h5">分類管理</h2>
          <v-btn
              color="primary"
              prepend-icon="mdi-plus"
              @click="openCategoryDialog()"
          >
            新增分類
          </v-btn>
        </div>

        <v-data-table
            :headers="categoryHeaders"
            :items="categories"
            :loading="categoryLoading"
            class="elevation-1"
        >
          <template #[`item.actions`]="{ item }">
            <v-btn
                icon
                size="small"
                color="primary"
                @click="openCategoryDialog(item)"
            >
              <v-icon>mdi-pencil</v-icon>
            </v-btn>
            <v-btn
                icon
                size="small"
                color="error"
                class="ml-2"
                @click="confirmDeleteCategory(item)"
            >
              <v-icon>mdi-delete</v-icon>
            </v-btn>
          </template>
        </v-data-table>
      </v-window-item>

      <!-- 標籤管理 -->
      <v-window-item value="tags">
        <TagManagementView />
      </v-window-item>

      <!-- 評論管理 -->
      <v-window-item value="comments">
        <h2 class="text-h5 mb-4">評論管理</h2>
        <v-data-table
            v-if="comments && commentHeaders.length > 0"
            :headers="commentHeaders"
            :items="comments"
            :loading="commentLoading"
            class="elevation-1"
        >
          <template #[`item.content`]="{ item }">
            <div class="text-truncate" style="max-width: 300px;">
              {{ item.content }}
            </div>
          </template>

          <template #[`item.post`]="{ item }">
            <router-link
                :to="{ name: 'post-detail', params: { id: item.post.id } }"
                class="text-decoration-none"
            >
              {{ item.post.title }}
            </router-link>
          </template>

          <template #[`item.createdAt`]="{ item }">
            {{ formatDate(item.createdAt) }}
          </template>

          <template #[`item.actions`]="{ item }">
            <v-btn
                icon
                size="small"
                color="error"
                @click="confirmDeleteComment(item)"
            >
              <v-icon>mdi-delete</v-icon>
            </v-btn>
          </template>
        </v-data-table>
      </v-window-item>
    </v-window>

    <!-- 分類對話框 -->
    <v-dialog v-model="categoryDialog.show" max-width="500px">
      <v-card>
        <v-card-title>
          {{ categoryDialog.isEdit ? '編輯分類' : '新增分類' }}
        </v-card-title>
        <v-card-text>
          <v-form ref="categoryForm">
            <v-text-field
                v-model="categoryDialog.form.name"
                label="分類名稱"
                :rules="[v => !!v || '分類名稱不能為空']"
                required
            ></v-text-field>
            <v-textarea
                v-model="categoryDialog.form.description"
                label="分類描述"
                rows="3"
            ></v-textarea>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
              color="secondary"
              text
              @click="categoryDialog.show = false"
          >
            取消
          </v-btn>
          <v-btn
              color="primary"
              @click="saveCategory"
              :loading="categoryDialog.loading"
          >
            保存
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>



    <!-- 確認刪除對話框 -->
    <v-dialog v-model="deleteDialog.show" max-width="400px">
      <v-card>
        <v-card-title>確認刪除</v-card-title>
        <v-card-text>
          {{ deleteDialog.message }}
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="secondary" text @click="deleteDialog.show = false">
            取消
          </v-btn>
          <v-btn
              color="error"
              @click="confirmDelete"
              :loading="deleteDialog.loading"
          >
            確認刪除
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import CategoryService from '../../services/category.service'
import PostService from '../../services/post.service'
import CommentService from '../../services/comment.service'
import UserService from '../../services/user.service'
import TagManagementView from './TagManagementView.vue'

// 標籤頁
const tab = ref('users')

// 用戶管理
const users = ref([])
const userLoading = ref(false)
const userHeaders = [
  {title: 'ID', key: 'id', sortable: true},
  {title: '用戶名', key: 'username', sortable: true},
  {title: '姓名', key: 'fullName', sortable: true},
  {title: '電子郵件', key: 'email', sortable: true},
  {title: '角色', key: 'roles', sortable: false},
  {title: '操作', key: 'actions', sortable: false}
]

// 文章管理
const posts = ref([])
const filteredPosts = ref([])
const postLoading = ref(false)
const postFilter = ref('all')
const postFilterOptions = [
  { title: '全部文章', value: 'all' },
  { title: '已發佈', value: 'published' },
  { title: '未發佈', value: 'unpublished' }
]
const postHeaders = [
  {title: 'ID', key: 'id', sortable: true},
  {title: '標題', key: 'title', sortable: true},
  {title: '作者', key: 'author.username', sortable: true},
  {title: '狀態', key: 'published', sortable: true},
  {title: '創建時間', key: 'createdAt', sortable: true},
  {title: '操作', key: 'actions', sortable: false}
]

// 分類管理
const categories = ref([])
const categoryLoading = ref(false)
const categoryHeaders = [
  {title: 'ID', key: 'id', sortable: true},
  {title: '名稱', key: 'name', sortable: true},
  {title: '描述', key: 'description', sortable: false},
  {title: '操作', key: 'actions', sortable: false}
]

// 標籤管理 - 已移至進階標籤管理頁面

// 評論管理
const comments = ref([])
const commentLoading = ref(false)
const commentHeaders = [
  {title: 'ID', key: 'id', sortable: true},
  {title: '評論內容', key: 'content', sortable: false},
  {title: '所屬文章', key: 'post', sortable: false},
  {title: '評論者', key: 'user.username', sortable: true},
  {title: '評論時間', key: 'createdAt', sortable: true},
  {title: '操作', key: 'actions', sortable: false}
]

// 分類對話框
const categoryDialog = ref({
  show: false,
  isEdit: false,
  loading: false,
  form: {
    id: null,
    name: '',
    description: ''
  }
})



// 刪除確認對話框
const deleteDialog = ref({
  show: false,
  loading: false,
  type: '', // 'user', 'post', 'category', 'tag', 'comment'
  item: null,
  message: ''
})

const categoryForm = ref(null)


onMounted(() => {
  loadAllData()
})

function loadAllData() {
  loadUsers()
  loadPosts()
  loadCategories()
  loadComments()
}

// 用戶管理
async function loadUsers() {
  userLoading.value = true
  try {
    users.value = await UserService.getAllUsers()
  } catch (error) {
    console.error('載入用戶失敗:', error)
  } finally {
    userLoading.value = false
  }
}

function editUser(user) {
  // 在實際應用中，可能會開啟用戶編輯對話框
  console.log('編輯用戶:', user)
}

function confirmDeleteUser(user) {
  deleteDialog.value = {
    show: true,
    loading: false,
    type: 'user',
    item: user,
    message: `確定要刪除用戶 "${user.username}" 嗎？此操作無法撤銷。`
  }
}

// 文章管理
async function loadPosts() {
  postLoading.value = true
  try {
    const response = await PostService.getAllPostsForAdmin(0, 100)
    posts.value = response.content || []
    filterPosts() // 載入後立即套用篩選
  } catch (error) {
    console.error('載入文章失敗:', error)
  } finally {
    postLoading.value = false
  }
}

function filterPosts() {
  switch (postFilter.value) {
    case 'published':
      filteredPosts.value = posts.value.filter(post => post.published)
      break
    case 'unpublished':
      filteredPosts.value = posts.value.filter(post => !post.published)
      break
    default:
      filteredPosts.value = posts.value
  }
}

function confirmDeletePost(post) {
  deleteDialog.value = {
    show: true,
    loading: false,
    type: 'post',
    item: post,
    message: `確定要刪除文章 "${post.title}" 嗎？此操作無法撤銷。`
  }
}

// 分類管理
async function loadCategories() {
  categoryLoading.value = true
  try {
    categories.value = await CategoryService.getAllCategories()
  } catch (error) {
    console.error('載入分類失敗:', error)
  } finally {
    categoryLoading.value = false
  }
}

function openCategoryDialog(category = null) {
  if (category) {
    // 編輯
    categoryDialog.value = {
      show: true,
      isEdit: true,
      loading: false,
      form: {
        id: category.id,
        name: category.name,
        description: category.description || ''
      }
    }
  } else {
    // 新增
    categoryDialog.value = {
      show: true,
      isEdit: false,
      loading: false,
      form: {
        id: null,
        name: '',
        description: ''
      }
    }
  }
}

async function saveCategory() {
  const valid = await categoryForm.value.validate()
  if (!valid.valid) return

  categoryDialog.value.loading = true

  try {
    if (categoryDialog.value.isEdit) {
      // 更新
      await CategoryService.updateCategory(
          categoryDialog.value.form.id,
          categoryDialog.value.form
      )
    } else {
      // 新增
      await CategoryService.createCategory(categoryDialog.value.form)
    }

    // 重新載入分類列表
    await loadCategories()

    // 關閉對話框
    categoryDialog.value.show = false
  } catch (error) {
    console.error('保存分類失敗:', error)
    alert('保存分類失敗，請稍後重試')
  } finally {
    categoryDialog.value.loading = false
  }
}

function confirmDeleteCategory(category) {
  deleteDialog.value = {
    show: true,
    loading: false,
    type: 'category',
    item: category,
    message: `確定要刪除分類 "${category.name}" 嗎？此操作可能會影響該分類下的所有文章，無法撤銷。`
  }
}



// 評論管理
async function loadComments() {
  commentLoading.value = true
  try {
    // 假設有一個 API 能夠獲取所有評論
    // 在實際應用中需要實現此服務
    comments.value = await CommentService.getAllComments()
  } catch (error) {
    console.error('載入評論失敗:', error)
  } finally {
    commentLoading.value = false
  }
}

function confirmDeleteComment(comment) {
  deleteDialog.value = {
    show: true,
    loading: false,
    type: 'comment',
    item: comment,
    message: `確定要刪除此評論嗎？此操作無法撤銷。`
  }
}

// 通用函數
async function confirmDelete() {
  deleteDialog.value.loading = true

  try {
    const {type, item} = deleteDialog.value

    switch (type) {
      case 'user':
        await UserService.deleteUser(item.id)
        await loadUsers()
        break
      case 'post':
        await PostService.deletePost(item.id)
        await loadPosts()
        break
      case 'category':
        await CategoryService.deleteCategory(item.id)
        await loadCategories()
        break
      case 'comment':
        await CommentService.deleteComment(item.id)
        await loadComments()
        break
    }

    deleteDialog.value.show = false
  } catch (error) {
    console.error('刪除失敗:', error)
    alert('刪除失敗，請稍後重試')
  } finally {
    deleteDialog.value.loading = false
  }
}

function formatRole(role) {
  if (role === 'ROLE_ADMIN') return '管理員'
  if (role === 'ROLE_USER') return '用戶'
  return role.name.replace('ROLE_', '')
}

function formatDate(dateString) {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-TW', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}
</script>

<style scoped>
/* 自定義樣式可以在這裡添加 */
</style>