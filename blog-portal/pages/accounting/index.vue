<script setup lang="ts">
import { ref, computed } from 'vue';

// 1. 定义响应式数据列表
const accountingList = ref([
  { id: 1, type: '支出', category: '餐饮', amount: 35, date: new Date().toISOString().split('T')[0], description: '午餐' },
  { id: 2, type: '支出', category: '交通', amount: 8, date: new Date().toISOString().split('T')[0], description: '地铁费' }
])

// 2. 表单数据模型
const form = ref({
  type: '支出', // 默认选中支出
  category: '',
  amount: '',
  description: ''
})

// 3. 动态计算统计信息
const totalIncome = computed(() => {
  return accountingList.value
      .filter(item => item.type === '收入')
      .reduce((sum, item) => sum + item.amount, 0)
})

const totalExpense = computed(() => {
  return accountingList.value
      .filter(item => item.type === '支出')
      .reduce((sum, item) => sum + item.amount, 0)
})

const balance = computed(() => totalIncome.value - totalExpense.value)

// 4. 提交记账方法
const handleSubmit = () => {
  // 简单校验
  if (!form.value.amount || !form.value.category) {
    uni.showToast({ title: '请填写金额和分类', icon: 'none' })
    return
  }

  // 添加新数据到列表头部
  accountingList.value.unshift({
    id: Date.now(), // 使用时间戳作为简单ID
    type: form.value.type,
    category: form.value.category,
    amount: Number(form.value.amount),
    date: new Date().toISOString().split('T')[0], // 自动填入今天日期
    description: form.value.description
  })

  // 重置表单
  form.value = {
    type: '支出',
    category: '',
    amount: '',
    description: ''
  }

  uni.showToast({ title: '记账成功', icon: 'success' })
}

// 5. 清空列表（可选功能）
const clearList = () => {
  uni.showModal({
    title: '提示',
    content: '确定要清空所有记录吗？',
    success: (res) => {
      if (res.confirm) {
        accountingList.value = []
      }
    }
  })
}
</script>

<template>
  <view class="accounting-container">
    <!-- 头部统计 -->
    <view class="stats-card">
      <text class="title">今天天气真不错啊</text>
      <view class="stat-item">
        <text class="stat-label">总收入</text>
        <text class="stat-value income">¥{{ totalIncome }}</text>
      </view>
      <view class="stat-item">
        <text class="stat-label">总支出</text>
        <text class="stat-value expense">¥{{ totalExpense }}</text>
      </view>
      <view class="stat-item">
        <text class="stat-label">结余</text>
        <text class="stat-value" :class="balance >= 0 ? 'income' : 'expense'">¥{{ balance }}</text>
      </view>
    </view>

    <!-- 记账输入表单 -->
    <view class="form-card">
      <view class="form-header">
        <text>记一笔</text>
      </view>
      <view class="form-body">
        <!-- 类型切换 -->
        <view class="form-row type-switcher">
          <text
              class="type-btn"
              :class="{ active: form.type === '支出' }"
              @click="form.type = '支出'"
          >支出</text>
          <text
              class="type-btn"
              :class="{ active: form.type === '收入' }"
              @click="form.type = '收入'"
          >收入</text>
        </view>

        <!-- 金额输入 -->
        <view class="form-row">
          <input
              type="number"
              placeholder="输入金额"
              class="amount-input"
              v-model="form.amount"
          />
        </view>

        <!-- 分类和备注 -->
        <view class="form-row input-group">
          <input
              type="text"
              placeholder="分类 (如: 餐饮)"
              v-model="form.category"
              class="input-field"
          />
          <input
              type="text"
              placeholder="备注 (可选)"
              v-model="form.description"
              class="input-field"
          />
        </view>

        <!-- 提交按钮 -->
        <button class="submit-btn" @click="handleSubmit">确认记账</button>
      </view>
    </view>

    <!-- 记账列表 -->
    <view class="list-container">
      <view class="list-header">
        <text>近期流水 ({{ accountingList.length }})</text>
        <text class="clear-btn" @click="clearList">清空</text>
      </view>

      <view v-if="accountingList.length === 0" class="empty-state">
        <text>暂无记录，快去记一笔吧</text>
      </view>

      <view
          v-for="item in accountingList"
          :key="item.id"
          class="accounting-item"
          :class="item.type === '收入' ? 'income-item' : 'expense-item'"
      >
        <view class="item-left">
          <text class="category">{{ item.category }}</text>
          <text class="description">{{ item.description }}</text>
          <text class="date">{{ item.date }}</text>
        </view>
        <view class="item-right">
          <text class="amount" :class="item.type === '收入' ? 'income' : 'expense'">
            {{ item.type === '收入' ? '+' : '-' }}¥{{ item.amount }}
          </text>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.accounting-container {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 40px;
}

/* 统计卡片样式 */
.stats-card {
  display: flex;
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #666;
  display: block;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
}

.income { color: #52c41a; }
.expense { color: #f5222d; }

/* 表单卡片样式 */
.form-card {
  background: white;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  overflow: hidden;
}

.form-header {
  padding: 12px 20px;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

.form-body {
  padding: 20px;
}

.type-switcher {
  display: flex;
  margin-bottom: 15px;
  background: #f5f5f5;
  border-radius: 8px;
  padding: 4px;
}

.type-btn {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  border-radius: 6px;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}

.type-btn.active {
  background: white;
  color: #333;
  font-weight: bold;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.amount-input {
  width: 100%;
  height: 50px;
  font-size: 24px;
  font-weight: bold;
  color: #333;
  border-bottom: 1px solid #eee;
  margin-bottom: 15px;
  box-sizing: border-box;
}

.input-group {
  display: flex;
  gap: 10px;
}

.input-field {
  flex: 1;
  height: 40px;
  background: #f9f9f9;
  border-radius: 6px;
  padding: 0 10px;
  font-size: 14px;
}

.submit-btn {
  margin-top: 20px;
  background: #1890ff;
  color: white;
  border-radius: 8px;
  font-size: 16px;
  height: 44px;
  line-height: 44px;
  border: none;
}

.submit-btn:active {
  opacity: 0.9;
}

/* 列表样式 */
.list-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.list-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-header text:first-child {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.clear-btn {
  font-size: 12px;
  color: #999;
}

.empty-state {
  padding: 40px 20px;
  text-align: center;
  color: #ccc;
  font-size: 14px;
}

.accounting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.accounting-item:last-child {
  border-bottom: none;
}

.item-left { flex: 1; }

.category {
  font-size: 16px;
  color: #333;
  font-weight: 500;
  display: block;
  margin-bottom: 4px;
}

.description {
  font-size: 14px;
  color: #999;
  display: block;
  margin-bottom: 4px;
}

.date {
  font-size: 12px;
  color: #ccc;
}

.item-right { text-align: right; }

.amount {
  font-size: 18px;
  font-weight: 500;
}

/* 简单的背景色区分 */
.income-item { background: #f6ffed; }
.expense-item { background: #fff2f0; }
</style>