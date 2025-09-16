<template>
  <div class="markdown-editor-container">
    <div class="editor-wrapper">
      <md-editor
        v-model="innerContent"
        :height="editorHeight"
        :style="{ height: height }"
        @blur="onBlur"
        @focus="onFocus"
        @change="onContentChange"
        @onLoad="onEditorReady"
        @onUploadImg="handleImageUpload"
        :theme="theme === 'dark' ? 'dark' : 'light'"
        :placeholder="placeholder"
        :readOnly="readonly"
        :previewOnly="previewOnly"
        :preview-theme="previewTheme"
        :code-theme="codeTheme"
        ref="mdEditorRef"
        language="zh-TW"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, PropType } from 'vue'
import { MdEditor, config } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import logger from '@/utils/logger'
import ZH_TW from '@vavt/cm-extension/dist/locale/zh-TW';
import axios from '@/services/axios.config';

config({
  editorConfig: {
    languageUserDefined: {
      'zh-TW': ZH_TW
    }
  }
});

/**
 * Markdown Editor Component
 *
 * A reusable rich text editor component based on md-editor-v3
 */

const props = defineProps({
  /**
   * Editor content (v-model)
   */
  modelValue: {
    type: String as PropType<string>,
    default: ''
  },
  /**
   * Editor height
   */
  height: {
    type: String as PropType<string>,
    default: '300px'
  },
  /**
   * Editor theme (light or dark)
   */
  theme: {
    type: String as PropType<string>,
    default: 'light'
  },
  /**
   * Custom placeholder text
   */
  placeholder: {
    type: String as PropType<string>,
    default: '開始編寫內容...'
  },
  /**
   * Custom toolbar configuration
   */
  toolbars: {
    type: Array as PropType<string[]>,
    default: null
  },
  /**
   * Read-only mode
   */
  readonly: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  /**
   * Display preview only mode
   */
  previewOnly: {
    type: Boolean as PropType<boolean>,
    default: false
  },
  /**
   * Language for the editor
   */
  language: {
    type: String as PropType<string>,
    default: 'zh-TW'
  },
  /**
   * Theme for preview panel
   */
  previewTheme: {
    type: String as PropType<string>,
    default: 'default'
  },
  /**
   * Theme for code blocks
   */
  codeTheme: {
    type: String as PropType<string>,
    default: 'github'
  },
  /**
   * Additional editor options
   */
  options: {
    type: Object as PropType<Record<string, any>>,
    default: () => ({})
  },
  /**
   * Post ID for image upload association
   */
  postId: {
    type: [String, Number] as PropType<string | number | null>,
    default: null
  }
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'blur', editor: any): void;
  (e: 'focus', editor: any): void;
  (e: 'ready', editor: any): void;
  (e: 'change', content: string): void;
  (e: 'image-uploaded', url: string): void;
  (e: 'image-upload-error', error: any): void;
}>();

// 內部編輯器內容
const innerContent = ref<string>(props.modelValue || '');

// 編輯器引用
const mdEditorRef = ref<any | null>(null);

// 監聽外部 modelValue 變化
watch(() => props.modelValue, (newValue) => {
  logger.debug('外部 modelValue 變化', newValue?.substring(0, 50));
  if (newValue !== innerContent.value) {
    innerContent.value = newValue;
  }
});

// 監聽內部內容變化
watch(innerContent, (newContent) => {
  logger.debug('內部內容變化', newContent?.substring(0, 50));
  emit('update:modelValue', newContent);
  emit('change', newContent);
});

// 設置編輯器高度
const editorHeight = computed(() => {
  // 移除 'px' 後轉換為數字
  const heightStr = props.height.endsWith('px')
    ? props.height.slice(0, -2)
    : props.height;
  
  return parseInt(heightStr, 10) || 300;
});

// 編輯器事件處理函數
function onBlur(editor: any): void {
  emit('blur', editor);
}

function onFocus(editor: any): void {
  emit('focus', editor);
}

function onEditorReady(editor: any): void {
  logger.info('Markdown 編輯器就緒');
  emit('ready', editor);
}

function onContentChange(text: string): void {
  // 內容變化時的處理
  logger.debug('編輯器內容變化', text?.substring(0, 50));
}

// 處理圖片上傳
async function handleImageUpload(files: File[], callback: (urls: string[]) => void): Promise<void> {
  try {
    logger.info('開始上傳圖片', { count: files.length });
    
    const uploadPromises = files.map(async (file) => {
      const formData = new FormData();
      formData.append('file', file);
      formData.append('id', props.postId?.toString() || 'new')
      
      // 呼叫後端 API 上傳圖片
      const response = await axios.post('/files/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      
      // 確保後端回傳了預期的資料結構
      if (response.data && response.data.fileDownloadUri) {
        logger.info('圖片上傳成功', { url: response.data.fileDownloadUri });
        emit('image-uploaded', response.data.fileDownloadUri);
        return response.data.fileDownloadUri;
      } else {
        throw new Error('無效的 API 回應格式');
      }
    });
    
    // 等待所有圖片上傳完成
    const imageUrls = await Promise.all(uploadPromises);
    
    // 通過回調函數將圖片 URL 數組傳回編輯器
    callback(imageUrls);
    
    logger.info('所有圖片上傳完成', { count: imageUrls.length });
  } catch (error) {
    logger.error('圖片上傳失敗', { error });
    emit('image-upload-error', error);
    callback([]); // 失敗時傳入空數組避免編輯器報錯
  }
}

// 獲取編輯器實例的方法（供父組件調用）
const getEditor = (): any | undefined => {
  return mdEditorRef.value;
}

// 公開的方法
defineExpose({
  getEditor,
  mdEditorRef
})

onMounted(() => {
  // 初始化日誌
  logger.info('MarkdownEditor 掛載完成', {
    initialContent: props.modelValue?.substring(0, 50),
    theme: props.theme,
    readonly: props.readonly
  });
})
</script>

<style scoped>
.markdown-editor-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  border-radius: 4px;
  overflow: hidden;
}

.editor-wrapper {
  flex: 1;
  border-radius: 4px;
}
</style>
