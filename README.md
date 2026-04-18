# DeviceOps
A device management tool based on Device Owner API

<p style="font-size: 14pt; color: #666666;">
[!] 提示: 遇到任何技术疑问或操作障碍，请先问AI
</p>

## 权限授予
<p style="font-size: 11pt; color: #444444;">
由于应用依赖 Device Owner API，安装后需通过 ADB 授予权限：
</p>

1.  **退出设备上所有账号** (如Google/三星/小米账号)
2.  **命令行输入**:  
    `adb shell dpm set-device-owner "com.android.deviceops/.DeviceAdminReceiver"`  
    *(激活成功会输出带有Success的字符)*
3.  **你可以登录你的账号了**

<p style="font-size: 10pt; color: #888888;">
仅需一次授权，重启依然有效，只能通过应用卸载自身关闭授权
</p>

---

## UI 规范
<p style="font-size: 11pt; color: #555555;">
One UI / Material 3 设计语言
</p>

## 版本说明
<p style="font-size: 11pt; color: #555555;">
两个版本：
</p>

* **Entry**: 对应伪装版
* **Native**: 对应普通版

### 伪装版本特殊性：
1. 应用名称是 **SmartThings**
2. 应用图标是三星的 **SmartThings**
3. 启动应用将显示伪装，提示“请连接手机 确定”，短按“确定”将退出应用，长按“确定”满 **0.79秒** 后，进入真界面

---

## 功能

### 1. HTTP Proxy
<p style="font-size: 10.5pt; color: #444444;">
升级到OneUI8.0+后，内核TUN模块被移除，提供HTTP Proxy作为网络调试的功能替代，确保在无TUN支持的环境下依然能够实现高效的流量代理与分发。
</p>

### 2. 管理停用应用
<p style="font-size: 10.5pt; color: #444444;">
无需adb授权，停用/启用应用
</p>
<p style="font-size: 10pt; color: #777777;">
特殊性：由于调用的是Device Owner API(setApplicationHidden)实现的停用，停用实现方式是多用户隔离机制，这不同于常规冻结式停用，你无法在系统设置-应用列表里找到app并重新启用，只能通过本应用管理。
</p>

---

## 免责声明
<p style="font-size: 12pt; color: #333333; font-weight: bold;">
系统应用可进行6分钟停用测试
</p>
<p style="font-size: 12pt; color: #cc0000; font-weight: bold;">
但注意！停用系统应用后，用户空间可能正常，但重启可能无法开机
解决办法只有格式化
</p>

---
<p style="font-size: 9pt; color: #999999; text-align: right;">
Powered by Claude
</p>
