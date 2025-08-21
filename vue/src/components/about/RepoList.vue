<template>
  <div class="repo-list-container" style="color: var(--color-text)">
    <div class="title-row">
      <h2 class="repo-title">ALL MY PROJECTS</h2>
      <div class="search-wrapper">
        <input
          v-model="filter"
          type="text"
          placeholder="🔍 搜尋"
          class="search-input"
        />
      </div>
    </div>

    <div v-if="loading" class="text-center text-gray-500 mt-4">載入中...</div>
    <div v-else-if="error" class="text-center text-red-500 mt-4">載入失敗：{{ error }}</div>

    <div v-else class="repo-cards-container">
      <RepoCard
        v-for="repo in filteredRepos"
        :key="repo.id"
        :repo="repo"
        :repoLanguages="repoLanguages"
      />
    </div> <!-- close grid -->
  </div> <!-- close repo-list-container -->
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import RepoCard from './repo/RepoCard.vue'

// 定義類型
interface Repo {
  id: number;
  name: string;
  description: string | null;
  web_url: string;
  last_activity_at: string;
  path_with_namespace: string;
  [key: string]: any;
}

interface RepoLanguages {
  [repoId: number]: {
    [language: string]: number;
  };
}

const repos = ref<Repo[]>([]);
const repoLanguages = ref<RepoLanguages>({});
const filter = ref('');
const loading = ref(true);
const error = ref<string | null>(null);

const filteredRepos = computed(() =>
  repos.value
    .filter(repo =>
      repo.name.toLowerCase().includes(filter.value.toLowerCase())
    )
    .sort((a, b) => new Date(b.last_activity_at).getTime() - new Date(a.last_activity_at).getTime())
);

onMounted(async () => {
  try {
    // 讀取環境變數中的 GitLab token
    const gitlabToken = import.meta.env.VITE_GITLAB_TOKEN;
    console.log(gitlabToken);
    // 獲取專案列表
    const requestConfig = {
      params: {
        visibility: 'public', // 只要求公開項目
        per_page: 100 // 增加每頁項目數
      }
    };
    
    // 嘗試使用環境變數中的 token，但如果失敗也不影響整體功能
    try {
      if (!gitlabToken) {
        throw new Error('GitLab token 環境變數未設置');
      }
      
      const { data } = await axios.get<Repo[]>('https://gitlab.nilm.cc/api/v4/projects', {
        ...requestConfig,
        headers: {
          Authorization: `Bearer ${gitlabToken}`
        }
      });
      repos.value = data;
    } catch (tokenError) {
      console.error('Error with token, trying without token:', tokenError);
      // 如果使用 token 失敗，嘗試不使用 token 的請求
      const { data } = await axios.get<Repo[]>('https://gitlab.nilm.cc/api/v4/projects', requestConfig);
      repos.value = data;
    }

    // 對每個專案獲取語言信息
    for (const repo of repos.value) {
      try {
        // 嘗試使用環境變數中的 token 獲取語言信息
        try {
        if (!gitlabToken) {
          throw new Error('GitLab token 環境變數未設置');
        }
        
        const languageResponse = await axios.get<{[language: string]: number}>(`https://gitlab.nilm.cc/api/v4/projects/${repo.id}/languages`, {
          headers: {
            Authorization: `Bearer ${gitlabToken}`
          }
        });
        repoLanguages.value[repo.id] = languageResponse.data;
        } catch (langTokenError) {
          console.error(`Error fetching languages with token for project ${repo.id}, trying without token:`, langTokenError);
          // 如果使用 token 失敗，嘗試不使用 token
          const languageResponse = await axios.get<{[language: string]: number}>(`https://gitlab.nilm.cc/api/v4/projects/${repo.id}/languages`);
          repoLanguages.value[repo.id] = languageResponse.data;
        }
      } catch (langError) {
        console.error(`Error fetching languages for project ${repo.id}:`, langError);
        repoLanguages.value[repo.id] = {};
      }
    }
  } catch (e: any) {
  console.error('Error fetching projects:', e);
    error.value = e.message || '無法獲取專案列表，請稍後再試或直接訪問 https://gitlab.nilm.cc';
      // 即使失敗，仍然保持頁面能夠顯示
      repos.value = [];
    } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.repo-list-container {
  width: 100%;
  padding: 20px;
  max-width: 100%;
  overflow-x: hidden;
}

/* 標題列 */
.title-row {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  white-space: nowrap;
}

.repo-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--color-heading);
  margin: 0;
  margin-right: 15px;
}

/* 搜尋框 */
.search-wrapper {
  width: 200px;
  display: inline-block;
}

.search-input {
  width: 100%;
  padding: 6px 10px;
  border: none;
  border-bottom: 1px solid var(--color-border);
  background-color: transparent;
  color: var(--color-text);
  outline: none;
  text-align: left;
  font-size: 0.9rem;
}

.search-input:focus {
  border-bottom-color: #1976D2;
}

/* 卡片結构 */
.repo-cards-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  gap: 16px; /* 卡片之間的間距 */
  width: 100%;
  margin: 0 auto;
  margin-top: 20px;
}

/* 確保在不同寬度下的排版 */
@media (max-width: 768px) {
  .repo-list-container {
    padding: 10px;
  }
  
  .title-row {
    flex-wrap: wrap;
  }
  
  .repo-title {
    margin-bottom: 10px;
    margin-right: 0;
  }
  
  .search-wrapper {
    width: 100%;
  }
  
  .repo-cards-container {
    justify-content: center;
  }
}
</style>