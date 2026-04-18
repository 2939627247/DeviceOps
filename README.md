# DeviceOps
A device management tool based on Device Owner API

<div style="color: #333; font-family: sans-serif; line-height: 1.6;">

<h1 style="color: #222; border-bottom: 1px solid #ddd; padding-bottom: 0.3em;">DeviceOps</h1>

<div style="background-color: #f6f8fa; border-left: 5px solid #666; padding: 10px; color: #555; margin-bottom: 20px;">
    <strong>[!] 提示:</strong> 遇到任何技术疑问或操作障碍，请先问 AI
</div>

<h2 style="color: #444; font-size: 1.4em;">权限授予</h2>
<p style="color: #666;">由于应用依赖 <strong>Device Owner API</strong>，安装后需通过 ADB 授予权限：</p>
<ol style="color: #666;">
    <li>退出设备上所有账号 (如 Google/三星/小米账号)</li>
    <li>命令行输入: <br>
        <code>adb shell dpm set-device-owner "com.android.deviceops/.DeviceAdminReceiver"</code><br>
        <span style="font-size: 0.9em; color: #888;">(激活成功会输出带有 Success 的字符)</span>
    </li>
    <li>你可以登录你的账号了</li>
</ol>
<p style="color: #888; font-size: 0.95em;">* 仅需一次授权，重启依然有效，只能通过应用卸载自身关闭授权。</p>

<h2 style="color: #444; font-size: 1.4em;">UI 规范</h2>
<p style="color: #666;">One UI / Material 3 设计语言</p>

<h2 style="color: #444; font-size: 1.4em;">版本说明</h2>
<ul style="color: #666;">
    <li><strong>Entry:</strong> 对应伪装版</li>
    <li><strong>Native:</strong> 对应普通版</li>
</ul>

<div style="border: 1px solid #eee; padding: 15px; border-radius: 8px; background-color: #fafafa;">
    <h3 style="margin-top: 0; color: #555;">伪装版本特殊性：</h3>
    <ol style="color: #777; font-size: 0.95em;">
        <li>应用名称是 <strong>SmartThings</strong></li>
        <li>应用图标是三星的 <strong>SmartThings</strong></li>
        <li>启动应用将显示伪装，提示“请连接手机 确定”：
            <ul style="margin-top: 5px;">
                <li>短按“确定”将退出应用</li>
                <li>长按“确定”满 <strong>0.79秒</strong> 后，进入真界面</li>
            </ul>
        </li>
    </ol>
</div>

<h2 style="color: #444; font-size: 1.4em;">核心功能</h2>

<h3 style="color: #555;">1. HTTP Proxy</h3>
<p style="color: #666;">升级到 OneUI 8.0+ 后，内核 TUN 模块被移除。本应用提供 HTTP Proxy 作为网络调试的功能替代，确保在无 TUN 支持的环境下依然能够实现高效的流量代理与分发。</p>

<h3 style="color: #555;">2. 管理停用应用</h3>
<p style="color: #666;">无需 ADB 授权，即可直接停用/启用应用。</p>
<p style="color: #777; font-size: 0.95em; font-style: italic;">
    <strong>特殊性：</strong> 由于调用的是 Device Owner API (setApplicationHidden) 实现的停用，其底层是多用户隔离机制。这不同于常规冻结式停用，你无法在系统设置-应用列表里找到 App 并重新启用，只能通过本应用管理。
</p>

<h2 style="color: #d32f2f; font-size: 1.4em; border-bottom: 1px solid #ffcdd2;">免责声明</h2>
<p style="color: #d32f2f; font-weight: bold;">系统应用可进行 6 分钟停用测试。</p>
<p style="color: #d32f2f;">
    <strong>注意！</strong> 停用系统应用后，用户空间可能正常，但重启可能无法开机。<br>
    解决办法只有<strong>格式化</strong>。
</p>

<hr style="border: 0; border-top: 1px solid #eee; margin: 40px 0 20px 0;">

<p style="color: #aaa; font-size: 0.85em; text-align: center;">Powered by Claude</p>

</div>
