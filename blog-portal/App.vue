<script setup lang="ts">
// @ts-nocheck
import request from "./utils/request";
import { onMounted, onUnmounted } from "vue";

let notificationSocket: UniApp.SocketTask | null = null;

onMounted(async () => {
  await getUniAppConfig();
  // 连接 WebSocket 用于接收通知
  connectNotificationWebSocket();
});

onUnmounted(() => {
  // 关闭 WebSocket 连接
  if (notificationSocket) {
    notificationSocket.close({});
    notificationSocket = null;
  }
});

/**
 * 连接通知专用的 WebSocket
 */
function connectNotificationWebSocket() {
  const user = uni.getStorageSync('user') || {};
  if (!user.id) {
    console.log('用户未登录，不连接通知 WebSocket');
    return;
  }

  // 如果已经有连接，先关闭
  if (notificationSocket) {
    notificationSocket.close({});
  }

  const WS_URL = 'ws://http://1.14.69.51/:8080/api/ws/chat';
  
  notificationSocket = uni.connectSocket({
    url: WS_URL,
  });

  notificationSocket.onOpen(() => {
    console.log('通知 WebSocket 连接成功');
    
    // 注册用户信息
    const registerMsg = {
      type: 'REGISTER',
      userId: user.id,
      nickname: user.nickname || user.username,
      avatar: user.avatar || '',
    };
    notificationSocket!.send({
      data: JSON.stringify(registerMsg),
    });
  });

  notificationSocket.onMessage((res) => {
    try {
      const data = JSON.parse(res.data as string);
      
      // 处理风月币通知
      if (data.type === 'COIN_NOTIFICATION') {
        console.log('收到风月币通知:', data);
        
        uni.showModal({
          title: data.title || '风月币变动通知',
          content: data.content || '',
          showCancel: false,
          confirmText: '知道了',
          success() {
            // 可以在这里刷新钱包数据
            uni.$emit('wallet-updated');
          }
        });
        
        // 震动提醒
        uni.vibrateShort({
          type: 'medium'
        });
        
        return;
      }
      
      // 忽略其他类型的消息（在线用户列表、聊天消息等）
      // 这些消息由聊天室页面处理
    } catch (e) {
      // 非 JSON 消息，忽略
    }
  });

  notificationSocket.onClose(() => {
    console.log('通知 WebSocket 连接关闭');
    notificationSocket = null;
    
    // 3秒后尝试重连
    setTimeout(() => {
      const user = uni.getStorageSync('user') || {};
      if (user.id) {
        connectNotificationWebSocket();
      }
    }, 3000);
  });

  notificationSocket.onError((err) => {
    console.error('通知 WebSocket 错误:', err);
    notificationSocket = null;
  });
}

const getUniAppConfig = () => {
  request({
    url: '/wx/queryForConfig',
    method: 'GET',
    success(res) {
      const apiResponse = res.data;

      if (
    
  apiResponse.code === 200 &&
  
  apiResponse.data.winterfly === 'yzfy' &&
  apiResponse.data.config!==""
      ) {
     
        uni.redirectTo({
          url: '/pages/index/index',
        });
      } else {
       
        uni.redirectTo({
          url: '/pages/accounting/index',
        });
      }
    },
    fail(err) {
      console.error('请求失败:', err);
      
      uni.redirectTo({
        url: '/pages/accounting/index',
      });
    },
  });
};
</script>

<template>
  <!-- ❌ 删除 router-view -->
  <!-- <template v-if="showApp">
    <router-view />
  </template> -->

  <!-- 可以保留一个加载中提示 -->
  <view style="padding: 20px; text-align: center;">
    <text>正在加载...</text>
  </view>
</template>

<style scoped>
/* 这里可以保留一些全局样式，但不需要 accounting-container */
</style>