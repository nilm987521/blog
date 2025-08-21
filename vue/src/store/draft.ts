import { defineStore } from 'pinia';
import { ref } from 'vue';

const STORAGE_KEY = 'post_drafts';

interface DraftData {
  [key: string]: any;
  lastUpdated: string;
}

interface Drafts {
  [key: string]: DraftData;
}

export const useDraftStore = defineStore('draft', () => {
  const drafts = ref<Drafts>(loadDrafts());
  
  // 載入草稿
  function loadDrafts(): Drafts {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      return stored ? JSON.parse(stored) : {};
    } catch (e) {
      console.error('Failed to load drafts:', e);
      return {};
    }
  }
  
  // 保存草稿數據到本地存儲
  function saveDraftsToStorage(): void {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(drafts.value));
    } catch (e) {
      console.error('Failed to save drafts:', e);
    }
  }
  
  // 保存單個草稿
  function saveDraft(key: string, data: any): void {
    drafts.value[key] = {
      ...data,
      lastUpdated: new Date().toISOString()
    };
    saveDraftsToStorage();
  }
  
  // 獲取草稿
  function getDraft(key: string): DraftData | null {
    return drafts.value[key] || null;
  }
  
  // 刪除草稿
  function deleteDraft(key: string): boolean {
    if (drafts.value[key]) {
      delete drafts.value[key];
      saveDraftsToStorage();
      return true;
    }
    return false;
  }
  
  // 清理所有草稿
  function clearAllDrafts(): void {
    drafts.value = {};
    localStorage.removeItem(STORAGE_KEY);
  }
  
  return {
    drafts,
    saveDraft,
    getDraft,
    deleteDraft,
    clearAllDrafts
  };
});
