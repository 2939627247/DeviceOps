# DeviceOps
A device management tool based on Device Owner API

<p style="font-size: 14px; color: #666666;">
[!] 提示：遇技术疑问或操作障碍，请优先咨询 AI
</p>

### 权限授予
本应用依赖 **Device Owner API**。安装后须通过 ADB 授予权限：
1. **账号退出**：移除设备上所有账号（如 Google、三星、小米账号等）。
2. **指令执行**：在命令行输入：
```bash
adb shell dpm set-device-owner "com.android.deviceops/.DeviceAdminReceiver"
```
   *激活成功将输出包含 "Success" 的字符。*
3. **账号恢复**：此时可重新登录您的账号。

<p style="font-size: 13px; color: #888888;">
注：授权仅需一次，重启依然有效。如需撤销授权，只能通过应用内置的卸载功能实现。
</p>

---

### 设计规范
* **UI 标准**：遵循 One UI / Material 3 设计语言。

---

### 版本说明
* **Entry (伪装版)**
* **Native (普通版)**

**伪装版特殊机制：**
1. **名称**：显示为 `SmartThings`。
2. **图标**：采用三星 `SmartThings` 官方图标。
3. **交互**：启动后进入伪装界面，提示“请连接手机 [确定]”。
    * **短按**“确定”：退出应用。
    * **长按**“确定”：持续 0.79 秒后进入真实界面。

---

### 核心功能
1. **HTTP Proxy**
   针对 One UI 8.0+ 移除内核 TUN 模块的情况，提供 HTTP Proxy 作为网络调试替代方案，确保在无 TUN 支持环境下实现流量代理与分发。

2. **应用管理 (停用/启用)**
   基于 Device Owner API (`setApplicationHidden`) 实现，无需额外 ADB 授权。
   * **技术特性**：利用多用户隔离机制实现。与常规冻结不同，被停用的应用在系统设置的应用列表中不可见，仅可通过本应用恢复。

---

### 免责声明
<p style="font-size: 13px; color: #888888;">
系统应用支持 6 分钟停用测试。
<b>警告：</b>停用系统应用可能导致设备重启后无法进入系统（卡屏/黑屏）。若发生此类故障，唯有通过格式化数据解决。
</p>

---
<p style="font-size: 12px; color: #999999;">
Powered by Claude
</p>
