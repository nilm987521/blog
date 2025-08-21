<template>
  <div
    @click="openRepo(repo.web_url)"
    class="repo-card rounded-xl p-[8px] border cursor-pointer transition-all duration-300 h-full"
    style="background-color: var(--color-background-soft); border-color: var(--color-border); width: 350px;"
  >
    <!-- 縮圖區域 - 預設隱藏，只有當圖片載入成功時才顯示 -->
    <div class="repo-thumbnail p-[10px]" ref="thumbnailDiv" style="display: none;">
    <img 
      :src="thumbnailUrl" 
      :alt="repo.name + ' 縮圖'" 
      class="w-full h-auto rounded-lg" 
      @load="handleImageLoad" 
      @error="handleImageError" 
      crossorigin="anonymous"
    />
    </div>

    <div class="repo-content">
      <h3 class="project-title text-xl font-bold mb-2 px-3 py-1 rounded-md flex items-center" style="background: linear-gradient(135deg, #e6f7ff, #ccecff); color: #004080; border-left: 3px solid #0066cc; font-family: '楷體', KaiTi, serif; letter-spacing: 0.05em; padding-left: 10px;">
        {{ repo.name }}
      </h3>
      <p v-if="repo.description" class="text-sm mb-2 line-clamp-2" style="color: var(--color-text); opacity: 0.9">{{ repo.description }}</p>

      <div class="repo-info text-sm space-y-2">
        <div class="language-line">
          <span class="language-label">💻</span>
          <strong class="label-text">主要語言：</strong>
          <span v-if="typeof languages === 'string'" class="language-text">
            {{ languages }}
          </span>
          <span v-else class="language-icons">
            <template v-for="(lang, index) in languages.slice(0, 3)" :key="index">
              <span class="language-item" :title="lang.name + ' ' + lang.percentage + '%'">
                <span v-html="lang.icon"></span> <span class="percent">{{ lang.percentage }}%</span>
              </span>
            </template>
            <span v-if="languages.length > 3" class="language-item" title="更多語言">
              <span>...</span>
            </span>
          </span>
        </div>
        <div class="update-line">
          <span class="language-label">📅</span>
          <strong class="label-text">更新：</strong>
          <span class="update-text">{{ formattedDate }}</span>
        </div>
      </div> <!-- close repo-info -->
    </div> <!-- close repo-content -->
  </div> <!-- close repo-card -->
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

interface LanguageItem {
  name: string;
  icon: string;
  percentage: number;
}

// 定義組件的prop
const props = defineProps<{
  repo: {
    id: number;
    name: string;
    description: string | null;
    web_url: string;
    last_activity_at: string;
    path_with_namespace: string;
    [key: string]: any;
  };
  repoLanguages: {
    [repoId: number]: {
      [language: string]: number;
    };
  };
}>();

const thumbnailDiv = ref<HTMLElement | null>(null);

// 計算縮圖URL
const thumbnailUrl = computed(() => {
  const projectPath = props.repo.path_with_namespace || '';
  // 在控制台輸出路徑以便調試
  console.log(`Loading thumbnail for ${props.repo.name}: https://gitlab.nilm.cc/${projectPath}/-/raw/main/img/thumbnail.jpg`);
  return `https://gitlab.nilm.cc/${projectPath}/-/raw/main/img/thumbnail.jpg`;
});

// 計算格式化的日期
const formattedDate = computed(() => {
  return new Date(props.repo.last_activity_at).toLocaleDateString();
});

// 計算語言數據
const languages = computed(() => {
  const langData = props.repoLanguages[props.repo.id];
  if (!langData || Object.keys(langData).length === 0) {
    return '未知';
  }

  // 取得最大比例的前三種語言
  const topLanguages = Object.entries(langData)
    .sort((a, b) => b[1] - a[1]) // 按百分比降序排列
    .slice(0, 3); // 取前三個

  return topLanguages.map(([lang, percentage]) => {
    return {
      name: lang,
      icon: getLanguageIcon(lang),
      percentage: Math.round(percentage)
    };
  });
});

// 開啟repo的方法
const openRepo = (url: string) => window.open(url, '_blank');

// 處理圖片成功加載
const handleImageLoad = (event: Event) => {
  console.log(`圖片加載成功: ${props.repo.name}`);
  // 圖片載入成功才顯示縮圖區域
  try {
    const target = event.target as HTMLElement;
    const thumbnailDiv = target.parentNode as HTMLElement;
    if (thumbnailDiv) {
      thumbnailDiv.style.display = 'block';
      thumbnailDiv.classList.add('mb-3'); // 加入下邊距
    }
  } catch (error) {
    console.error('處理圖片加載成功時發生錯誤:', error);
  }
};

// 處理圖片加載失敗
const handleImageError = (event: Event) => {
  console.log(`圖片加載失敗: ${props.repo.name}`);
  // 圖片加載失敗時完全移除容器
  try {
    const target = event.target as HTMLElement;
    const thumbnailDiv = target.parentNode as HTMLElement;
    if (thumbnailDiv) {
      thumbnailDiv.style.display = 'none';
      thumbnailDiv.classList.remove('mb-3', 'p-[10px]');
    }
  } catch (error) {
    console.error('處理圖片加載失敗時發生錯誤:', error);
  }
};

// 語言圖標函數
const getLanguageIcon = (language: string): string => {
  // 轉換為小寫來比對
  const lowerLang = language.toLowerCase();

  // 先對常見的特殊情況進行判斷
  if (lowerLang === 'dockerfile' || lowerLang.includes('docker')) {
    return '<i class="fa-brands fa-docker"></i>';
  }
  if (lowerLang.includes('shell') || lowerLang.includes('bash') || lowerLang.includes('sh')) {
    return '<i class="fa-solid fa-terminal"></i>';
  }

  const icons: { [key: string]: string } = {
    // 程式語言
    'javascript': '<i class="fa-brands fa-js"></i>',
    'typescript': '<i class="fa-brands fa-js"></i>',
    'python': '<i class="fa-brands fa-python"></i>',
    'java': '<i class="fa-brands fa-java"></i>',
    'c#': '<i class="fa-brands fa-microsoft"></i>',
    'c++': '<i class="fa-solid fa-code"></i>',
    'c': '<i class="fa-solid fa-copyright"></i>',
    'php': '<i class="fa-brands fa-php"></i>',
    'ruby': '<i class="fa-solid fa-gem"></i>',
    'go': '<i class="fa-solid fa-external-link-alt"></i>',
    'rust': '<i class="fa-brands fa-rust"></i>',
    'kotlin': '<i class="fa-brands fa-android"></i>',
    'swift': '<i class="fa-brands fa-swift"></i>',
    'r': '<i class="fa-solid fa-registered"></i>',
    'perl': '<i class="fa-solid fa-journal-whills"></i>',
    'scala': '<i class="fa-solid fa-sort-amount-up"></i>',

    // 前端框架與技術
    'html': '<i class="fa-brands fa-html5"></i>',
    'css': '<i class="fa-brands fa-css3-alt"></i>',
    'sass': '<i class="fa-brands fa-sass"></i>',
    'vue': '<i class="fa-brands fa-vuejs"></i>',
    'react': '<i class="fa-brands fa-react"></i>',
    'angular': '<i class="fa-brands fa-angular"></i>',
    'node': '<i class="fa-brands fa-node-js"></i>',
    'npm': '<i class="fa-brands fa-npm"></i>',
    'yarn': '<i class="fa-brands fa-yarn"></i>',

    // 其他技術
    'docker': '<i class="fa-brands fa-docker"></i>',
    'kubernetes': '<i class="fa-solid fa-dharmachakra"></i>',
    'aws': '<i class="fa-brands fa-aws"></i>',
    'linux': '<i class="fa-brands fa-linux"></i>',
    'ubuntu': '<i class="fa-brands fa-ubuntu"></i>',
    'debian': '<i class="fa-brands fa-debian"></i>',
    'centos': '<i class="fa-brands fa-centos"></i>',
    'fedora': '<i class="fa-brands fa-fedora"></i>',
    'redhat': '<i class="fa-brands fa-redhat"></i>',
    'gitlab': '<i class="fa-brands fa-gitlab"></i>',
    'github': '<i class="fa-brands fa-github"></i>',
    'git': '<i class="fa-brands fa-git-alt"></i>',
    'markdown': '<i class="fa-brands fa-markdown"></i>',
    'shell': '<i class="fa-solid fa-terminal"></i>',
    'bash': '<i class="fa-solid fa-terminal"></i>',
    'powershell': '<i class="fa-solid fa-terminal"></i>',

    // 資料相關
    'json': '<i class="fa-solid fa-file-code"></i>',
    'yaml': '<i class="fa-solid fa-file-code"></i>',
    'xml': '<i class="fa-solid fa-code"></i>',
    'sql': '<i class="fa-solid fa-database"></i>',
    'graphql': '<i class="fa-solid fa-project-diagram"></i>',
  };

  // 匹配語言或預設圖標
  for (const key in icons) {
    if (lowerLang.includes(key)) {
      return icons[key];
    }
  }

  // 預設圖標
  return '<i class="fa-solid fa-file-code"></i>';
};
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 卡片樣式 */
.repo-card {
  box-shadow: 3px 3px 10px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
  /* 移除margin，使用父容器的gap來設置間距 */
  width: 350px;
  min-height: 115px;
  display: flex;
  flex-direction: column;
}

/* 縮圖樣式 */
.repo-thumbnail {
  width: 100%;
  max-height: 180px;
  overflow: hidden;
  border-radius: 8px;
  background-color: transparent;
  border: none;
  margin: 0;
  transition: margin 0.2s ease;
}

.repo-thumbnail img {
  width: 100%;
  height: auto;
  object-fit: cover;
  border-radius: 6px;
  transition: transform 0.3s ease;
  background-color: transparent;
  border: none;
  padding: 10px;
}

.repo-card:hover .repo-thumbnail img {
  transform: scale(1.03);
}

.repo-card h3 {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.repo-info {
  margin-top: auto;
  padding-top: 8px;
  border-top: 1px solid rgba(200, 200, 200, 0.2);
}

.repo-card:hover {
  box-shadow: 5px 5px 15px rgba(0, 0, 0, 0.2);
  transform: translateY(-3px) translateX(-3px);
  border-color: rgba(59, 130, 246, 0.4);
}

/* 圖標樣式 */
.repo-icon {
  display: inline-block;
  min-width: 1.5rem;
  text-align: center;
}

/* 語言行樣式 */
.language-line, .update-line {
  display: flex;
  align-items: center;
  white-space: nowrap;
  width: 100%;
  overflow: hidden;
}

.language-label {
  flex-shrink: 0;
  width: 24px;
  margin-right: 2px;
  text-align: center;
}

.label-text {
  flex-shrink: 0;
  margin-right: 4px;
}

.language-icons {
  display: inline-flex;
  align-items: center;
  flex-wrap: nowrap;
  overflow: hidden;
}

.language-item {
  display: inline-flex;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.05);
  padding: 1px 4px;
  border-radius: 8px;
  margin-right: 4px;
  font-size: 0.8rem;
}

.language-item .percent {
  font-size: 0.65rem;
  margin-left: 1px;
  opacity: 0.8;
}

.language-text, .update-text {
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 專案標題樣式 */
.project-title {
  transition: all 0.25s ease;
  transform-origin: left center;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
  position: relative;
  overflow: hidden;
}

.project-title::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.3) 50%, rgba(255,255,255,0) 100%);
  transition: all 0.4s ease;
}

.repo-card:hover .project-title {
  transform: scale(1.02);
  background: linear-gradient(135deg, #d9f0ff, #b3e0ff);
  box-shadow: 0 2px 4px rgba(0,0,0,0.15);
}

.repo-card:hover .project-title::after {
  left: 100%;
}
</style>
