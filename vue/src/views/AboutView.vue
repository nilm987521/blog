<template>
  <div class="page-about v-full-height" style="padding-top: 65px">
    <!-- 左側個人資訊區域 -->
    <aside class="p-6" style="padding-top: 30px">
      <ProfileSection />
    </aside>

    <!-- 右側 GitLab 作品集區域 -->
    <section class="p-6 overflow-y-auto overflow-x-hidden">
      <RepoList />
    </section>
  </div>
</template>

<script setup>
import RepoList from '@/components/about/RepoList.vue'
import ProfileSection from '@/components/about/ProfileSection.vue'
</script>

<style>
/* GlobalStyles */
:root {
  --color-background-soft: #f8f8f8;
  --color-border: #e0e0e0;
  --color-heading: #2c3e50;
  --color-text: #333;
  --color-background-mute: #f2f2f2;
}

.v-full-height {
  min-height: calc(100vh - 64px) !important; /* 減去頭部高度 */
}

.page-about {
  background-color: #fff;
  width: 100%;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: row !important;
}

@media (max-width: 768px) {
  .page-about {
    flex-direction: column !important;
  }
}

.page-about aside {
  flex: 0 0 400px;
  max-width: 400px;
  width: 400px;
  background-color: var(--color-background-soft);
  position: sticky;
  top: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.page-about section {
  flex: 1;
  overflow-y: auto;
}

.page-about .repo-card {
  width: 350px;
}
</style>
