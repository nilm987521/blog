import './assets/tailwind.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'
import '@mdi/font/css/materialdesignicons.css'
// 導入 md-editor-v3 (使用具名導入)
import { config } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
// 導入 Axios 配置
import './services/axios.config'

const vuetify = createVuetify({
  components,
  directives,
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        colors: {
          primary: '#1976D2',    // 藍色
          secondary: '#424242',  // 深灰色
          accent: '#82B1FF',     // 淺藍色
          error: '#FF5252',      // 紅色
          info: '#2196F3',       // 藍色
          success: '#4CAF50',    // 綠色
          warning: '#FFC107',    // 黃色
          background: '#f4f4f4'
        }
      }
    }
  }
})

const app = createApp(App)

// 配置 md-editor-v3 (使用正確的 config 函數)
config({
  editorConfig: {
    languageUserDefined: {
      'zh-TW': {
        toolbarTips: {
          bold: '粗體',
          italic: '斜體',
          strikethrough: '刪除線',
          title: '標題',
          quote: '引用',
          unorderedList: '無序列表',
          orderedList: '有序列表',
          codeRow: '行內代碼',
          code: '代碼塊',
          link: '連結',
          image: '圖片',
          table: '表格',
          revoke: '撤銷',
          next: '重做',
          save: '保存',
          prettier: '美化',
          pageFullscreen: '瀏覽器全屏',
          fullscreen: '全屏',
          preview: '預覽',
          htmlPreview: 'HTML 預覽',
          catalog: '目錄',
          github: 'GitHub',
          help: '幫助'
        },
        titleItem: {
          h1: '一級標題',
          h2: '二級標題',
          h3: '三級標題',
          h4: '四級標題',
          h5: '五級標題',
          h6: '六級標題'
        },
        imgTitleItem: {
          link: '添加連結',
          upload: '上傳圖片',
          clip2upload: '剪貼板上傳'
        },
        linkModalTips: {
          title: '添加連結',
          descLable: '連結描述：',
          descLablePlaceHolder: '請輸入描述',
          urlLable: '連結地址：',
          urlLablePlaceHolder: '請輸入 URL',
          buttonOK: '確定'
        },
        clipModalTips: {
          title: '添加剪貼板圖片',
          buttonUpload: '上傳'
        },
        copyCode: {
          text: '複製代碼',
          successTips: '已複製！',
          failTips: '複製失敗！'
        },
        mermaid: {
          flow: '流程圖',
          sequence: '時序圖',
          gantt: '甘特圖',
          class: '類圖',
          state: '狀態圖',
          pie: '餅圖',
          relationship: '關係圖',
          journey: '旅程圖'
        },
        katex: {
          inline: '行內公式',
          block: '區塊公式'
        },
        footer: {
          markdownTotal: '字數',
          scrollAuto: '同步滾動'
        }
      }
    }
  }
})

app.use(createPinia())
app.use(router)
app.use(vuetify)

app.mount('#app')
