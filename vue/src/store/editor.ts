import { defineStore } from 'pinia';
import { ref } from 'vue';

export type EditorType = 'quill' | 'markdown';

export const useEditorStore = defineStore('editor', () => {
  // 目前的編輯器類型 ('quill' 或 'markdown')
  const editorType = ref<EditorType>('markdown'); // 預設為markdown
  
  // 切換編輯器類型
  function setEditorType(type: EditorType): void {
    editorType.value = type;
  }

  return { 
    editorType,
    setEditorType
  };
});
