<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { categoryApi, tagApi } from '@/api'
import { Add, Trash, Create } from '@vicons/ionicons5'

const { t } = useI18n()
const message = useMessage()

// Categories
const categories = ref<any[]>([])
const categoryLoading = ref(false)
const showCategoryDialog = ref(false)
const editingCategory = ref<any>(null)
const categoryForm = ref({ name: '', color: '#18a058' })

// Tags
const tags = ref<any[]>([])
const tagLoading = ref(false)
const showTagDialog = ref(false)
const editingTag = ref<any>(null)
const tagForm = ref({ name: '', color: '#18a058' })

const activeTab = ref('categories')

// Category functions
async function fetchCategories() {
  categoryLoading.value = true
  try {
    const response = await categoryApi.getAll()
    categories.value = response.data
  } catch (e) {
    message.error(t('common.failed'))
  } finally {
    categoryLoading.value = false
  }
}

function openCategoryDialog(category?: any) {
  if (category) {
    editingCategory.value = category
    categoryForm.value = { name: category.name, color: category.color }
  } else {
    editingCategory.value = null
    categoryForm.value = { name: '', color: '#18a058' }
  }
  showCategoryDialog.value = true
}

async function saveCategory() {
  try {
    if (editingCategory.value) {
      await categoryApi.update(editingCategory.value.id, categoryForm.value)
    } else {
      await categoryApi.create(categoryForm.value)
    }
    message.success(t('common.success'))
    showCategoryDialog.value = false
    fetchCategories()
  } catch (e) {
    message.error(t('common.failed'))
  }
}

async function deleteCategory(id: number) {
  try {
    await categoryApi.delete(id)
    message.success(t('common.success'))
    fetchCategories()
  } catch (e) {
    message.error(t('common.failed'))
  }
}

// Tag functions
async function fetchTags() {
  tagLoading.value = true
  try {
    const response = await tagApi.getAll()
    tags.value = response.data
  } catch (e) {
    message.error(t('common.failed'))
  } finally {
    tagLoading.value = false
  }
}

function openTagDialog(tag?: any) {
  if (tag) {
    editingTag.value = tag
    tagForm.value = { name: tag.name, color: tag.color }
  } else {
    editingTag.value = null
    tagForm.value = { name: '', color: '#18a058' }
  }
  showTagDialog.value = true
}

async function saveTag() {
  try {
    if (editingTag.value) {
      await tagApi.update(editingTag.value.id, tagForm.value)
    } else {
      await tagApi.create(tagForm.value)
    }
    message.success(t('common.success'))
    showTagDialog.value = false
    fetchTags()
  } catch (e) {
    message.error(t('common.failed'))
  }
}

async function deleteTag(id: number) {
  try {
    await tagApi.delete(id)
    message.success(t('common.success'))
    fetchTags()
  } catch (e) {
    message.error(t('common.failed'))
  }
}

onMounted(() => {
  fetchCategories()
  fetchTags()
})
</script>

<template>
  <div class="tags-manager">
    <n-card>
      <n-tabs v-model:value="activeTab" type="line" animated>
        <n-tab-pane name="categories" :tab="t('tags.categories')">
          <div class="toolbar">
            <n-button type="primary" @click="openCategoryDialog()">
              <template #icon>
                <n-icon :component="Add" />
              </template>
              {{ t('tags.addCategory') }}
            </n-button>
          </div>

          <n-grid v-if="categories.length > 0" :cols="3" :x-gap="12" :y-gap="12" class="items-grid">
            <n-gi v-for="category in categories" :key="category.id">
              <n-card hoverable class="item-card">
                <div class="item-content">
                  <div class="item-color" :style="{ background: category.color }"></div>
                  <span class="item-name">{{ category.name }}</span>
                </div>
                <div class="item-actions">
                  <n-button quaternary size="small" @click="openCategoryDialog(category)">
                    <n-icon :component="Create" />
                  </n-button>
                  <n-button quaternary size="small" type="error" @click="deleteCategory(category.id)">
                    <n-icon :component="Trash" />
                  </n-button>
                </div>
              </n-card>
            </n-gi>
          </n-grid>
          <n-empty v-else :description="t('common.noData')" />
        </n-tab-pane>

        <n-tab-pane name="tags" :tab="t('tags.tags')">
          <div class="toolbar">
            <n-button type="primary" @click="openTagDialog()">
              <template #icon>
                <n-icon :component="Add" />
              </template>
              {{ t('tags.addTag') }}
            </n-button>
          </div>

          <n-space v-if="tags.length > 0" wrap>
            <n-tag
              v-for="tag in tags"
              :key="tag.id"
              :bordered="false"
              :style="{ background: tag.color + '20', color: tag.color }"
            >
              {{ tag.name }}
              <template #icon>
                <n-button quaternary size="tiny" @click="deleteTag(tag.id)">
                  <n-icon :component="Trash" />
                </n-button>
              </template>
            </n-tag>
          </n-space>
          <n-empty v-else :description="t('common.noData')" />
        </n-tab-pane>
      </n-tabs>
    </n-card>

    <!-- Category Dialog -->
    <n-modal v-model:show="showCategoryDialog" preset="card" :title="editingCategory ? t('common.edit') : t('common.create')" style="width: 400px">
      <n-form>
        <n-form-item :label="t('tags.name')">
          <n-input v-model:value="categoryForm.name" :placeholder="t('tags.name')" />
        </n-form-item>
        <n-form-item :label="t('tags.color')">
          <n-color-picker v-model:value="categoryForm.color" :modes="['hex']" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-button @click="showCategoryDialog = false">{{ t('common.cancel') }}</n-button>
        <n-button type="primary" @click="saveCategory">{{ t('common.save') }}</n-button>
      </template>
    </n-modal>

    <!-- Tag Dialog -->
    <n-modal v-model:show="showTagDialog" preset="card" :title="editingTag ? t('common.edit') : t('common.create')" style="width: 400px">
      <n-form>
        <n-form-item :label="t('tags.name')">
          <n-input v-model:value="tagForm.name" :placeholder="t('tags.name')" />
        </n-form-item>
        <n-form-item :label="t('tags.color')">
          <n-color-picker v-model:value="tagForm.color" :modes="['hex']" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-button @click="showTagDialog = false">{{ t('common.cancel') }}</n-button>
        <n-button type="primary" @click="saveTag">{{ t('common.save') }}</n-button>
      </template>
    </n-modal>
  </div>
</template>

<style scoped lang="scss">
.tags-manager {
  .toolbar {
    margin-bottom: 16px;
  }

  .items-grid {
    margin-top: 16px;
  }

  .item-card {
    .item-content {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .item-color {
      width: 16px;
      height: 16px;
      border-radius: 4px;
    }

    .item-name {
      font-weight: 500;
    }

    .item-actions {
      display: flex;
      gap: 4px;
      margin-top: 8px;
      justify-content: flex-end;
    }
  }
}
</style>
