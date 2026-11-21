<template>
  <header class="page-head">
    <div class="page-head__main">
      <h1 class="page-head__title">{{ title }}</h1>
      <span v-if="hasCount" class="page-head__count">{{ count }}</span>
      <span v-if="description" class="page-head__desc">{{ description }}</span>
    </div>
    <div v-if="$slots.actions" class="page-head__actions">
      <slot name="actions" />
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  },
  count: {
    type: [Number, String],
    default: null
  }
});

const hasCount = computed(
  () => props.count !== null && props.count !== undefined && props.count !== ''
);
</script>

<style scoped>
.page-head {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 34px;
}

.page-head__main {
  display: flex;
  align-items: baseline;
  gap: 10px;
  min-width: 0;
}

.page-head__title {
  margin: 0;
  color: var(--ink);
  font-family: var(--app-font-family);
  font-size: 19px;
  font-weight: 600;
  letter-spacing: 0;
  line-height: 1.2;
  white-space: nowrap;
}

.page-head__count {
  flex: 0 0 auto;
  padding: 1px 7px;
  border-radius: 6px;
  background: var(--brand-soft);
  color: var(--brand);
  font-size: 11.5px;
  font-weight: 600;
  line-height: 1.6;
}

.page-head__desc {
  overflow: hidden;
  color: var(--muted-2);
  font-size: 12.5px;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.page-head__actions {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .page-head {
    flex-wrap: wrap;
    gap: 8px;
  }

  .page-head__desc {
    display: none;
  }

  .page-head__actions {
    width: 100%;
  }
}
</style>
