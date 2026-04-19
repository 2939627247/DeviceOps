# DeviceOps

基于 Android 企业管理策略 **Device Owner** 的 **WearOS** 设备管理应用，不依赖 ADB 驻留进程

# 配置

DeviceOps 需要 Device Owner 权限，安装后须通过 ADB 手动授予。

**1. 退出设备上的所有账号**

操作前请退出设备上的所有账号（Google、三星、小米、华为等）。存在账号时系统不允许激活 Device Owner。

**2. 授予 Device Owner**

```bash
adb shell dpm set-device-owner "com.android.deviceops/.DeviceAdminReceiver"
```

输出包含 `Success` 的字符串即表示激活成功。

**3. 重新登录账号**

激活完成后可正常添加账号。

> [!NOTE]
> 授权在重启后依然有效。如需撤销 Device Owner，请使用应用内的卸载功能，无其他移除方式。

# 版本

### Native（普通版）
常规应用图标与名称，启动后直接进入管理界面。

### Entry（伪装版）
伪装为 **SmartThings**，使用三星官方 SmartThings 图标。

启动后显示「请连接手机」弹窗：
- **短按**「确定」— 退出应用
- **长按**「确定」**≥ 0.79 秒** — 进入管理界面

# 功能

## HTTP Proxy

搭载 **WearOS 5.1+** 的手表内核已移除 TUN 模块，无法使用VpnService。DeviceOps 提供 HTTP 代理作为网络调试替代方案，在无 TUN 支持的环境下实现流量转发。

## 应用可见性控制

通过 `DevicePolicyManager.setApplicationHidden` 隐藏或恢复应用，无需连接 ADB。

与常规停用不同，被隐藏的应用通过多用户隔离机制实现，将从系统全局应用列表中消失，无法通过**设置 → 应用**恢复，只能通过 DeviceOps 管理。

# 警告

> [!CAUTION]
> 系统应用支持 **6 分钟**的停用测试。
>
> 隐藏系统应用后用户空间可能维持正常，但**重启后设备可能无法开机**，唯一的恢复方式为恢复出厂设置。

# 致谢

## AI Agents
<img src="docs/icons/claude.png" height="16" align="center"> <img src="docs/icons/gemini.png" height="16" align="center"> Claude / Gemini 提供代码支持
