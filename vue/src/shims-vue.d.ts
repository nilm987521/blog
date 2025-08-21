/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// 聲明全局 Window 接口擴展
interface Window {
  // 如果有全局自定義變量，可以在這裡聲明
}

// 聲明 Quill 相關模塊
declare module '@vueup/vue-quill' {
  import { DefineComponent } from 'vue'
  
  export const QuillEditor: DefineComponent<{
    content?: string;
    theme?: string;
    toolbar?: any;
    options?: any;
  }, {}, any>
}
