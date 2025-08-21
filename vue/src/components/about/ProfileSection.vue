<template>
  <div class="profile-section flex flex-col items-center w-full">
    <!-- 個人照片 -->
    <div class="profile-image mb-6">
      <div class="profile-image-container">
        <img
          src="@/assets/avatar.jpeg"
          alt="Daniel 的照片"
        />
      </div>
    </div>

    <!-- 個人資訊 -->
    <div class="profile-info text-center w-full px-2">
      <h1 class="text-xl font-bold mb-1" style="color: var(--color-heading)">Daniel</h1>

      <div class="profile-bio mb-4 text-left">
        <p class="mb-2 text-sm" style="color: var(--color-text)">
          喜歡Terminal大於GUI
        </p>
      </div>

      <!-- 技能標籤 -->
      <div class="skills mt-4">
        <h2 class="text-base font-medium mb-2" style="color: var(--color-heading)">專精技能</h2>
        <div class="flex flex-wrap justify-center gap-1">
          <span class="px-2 py-1 rounded-full text-xs"
                style="background-color: var(--color-background-mute); color: var(--color-text)">
            Container /
          </span>
          <span class="px-2 py-1 rounded-full text-xs"
                style="background-color: var(--color-background-mute); color: var(--color-text)">
            RedHat Linux /
          </span>
          <span class="px-2 py-1 rounded-full text-xs"
                style="background-color: var(--color-background-mute); color: var(--color-text)">
            Java
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.profile-image-container {
  width: 300px;
  height: 300px;
  border-radius: 50%;
  overflow: hidden;
  border: 4px solid;
  border-color: var(--color-border);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin: 0 auto;
}

.profile-image-container img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
}

@media (min-width: 768px) {
  .profile-image-container {
    width: 360px;
    height: 360px;
  }
}
</style>
