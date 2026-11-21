<template>
  <section class="role-home">
    <PageHeader
      title="学生工作台"
      description="以下为后续将在此页展示的数据，当前仅作占位说明。"
    />
    <div class="role-home__grid">
      <article v-for="item in sections" :key="item.title" class="role-home__card">
        <div class="role-home__head">
          <h2>{{ item.title }}</h2>
          <span class="role-home__tag">待接入</span>
        </div>
        <p class="role-home__desc">{{ item.description }}</p>
        <ul class="role-home__list">
          <li v-for="line in item.items" :key="line">{{ line }}</li>
        </ul>
      </article>
    </div>
  </section>
</template>

<script setup>
import PageHeader from '@/components/PageHeader.vue';

const sections = [
  {
    title: '今日课程',
    description: '按节次展示今天要上的课，方便课前快速确认。',
    items: ['节次与上课时间', '课程与授课教师', '上课教室']
  },
  {
    title: '本周课表',
    description: '本周课程分布与调课标记。',
    items: ['周一至周五课程', '调课与停课标记', '连续课时提示']
  },
  {
    title: '作业与通知',
    description: '待完成作业与班级通知。',
    items: ['作业截止时间', '提交状态', '班级通知列表']
  },
  {
    title: '考试安排',
    description: '近期考试时间与地点。',
    items: ['考试科目', '考试时间', '考场']
  },
  {
    title: '班级信息',
    description: '所属班级与班主任信息。',
    items: ['班级名称', '班主任', '固定教室']
  }
];
</script>

<style scoped>
.role-home {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.role-home__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.role-home__card {
  padding: 18px 20px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: var(--surface);
  box-shadow: var(--shadow-soft);
}

.role-home__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.role-home__head h2 {
  margin: 0;
  color: var(--ink);
  font-size: 15px;
  font-weight: 600;
}

.role-home__tag {
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--brand-soft);
  color: var(--brand);
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}

.role-home__desc {
  margin: 8px 0 0;
  color: var(--muted);
  font-size: 12.5px;
  line-height: 1.7;
}

.role-home__list {
  margin: 10px 0 0;
  padding-left: 18px;
  color: var(--ink-2);
  font-size: 12.5px;
  line-height: 1.9;
}

@media (max-width: 900px) {
  .role-home__grid {
    grid-template-columns: 1fr;
  }
}
</style>
