<template>
  <section class="profile-page">
    <PageHeader title="个人中心" description="账号信息与安全设置" />
    <div class="profile-page__panel">
      <el-empty
        :image-size="96"
        description="个人中心正在建设中，后续将提供修改密码等账号功能"
      />
    </div>
  </section>
</template>

<script setup>
// 个人中心当前只做占位：不请求账号接口，后续在此扩展修改密码等账号能力。
import PageHeader from '@/components/PageHeader.vue';
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.profile-page__panel {
  display: grid;
  place-items: center;
  min-height: 320px;
  padding: 24px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--surface);
  box-shadow: var(--shadow-soft);
}
</style>
