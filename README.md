# DeviceOps
A device management tool based on Device Owner API

# <font size="6" color="#333333">DeviceOps</font>

<font size="3" color="#666666">[!] 提示: 遇到任何技术疑问或操作障碍，请先问AI</font>

<font size="5" color="#444444">权限授予：</font>
<font size="3" color="#666666">由于应用依赖 Device Owner API，安装后需通过 ADB 授予权限：</font>
<font size="3" color="#666666">1.退出设备上所有账号(如Google/三星/小米账号)</font>
<font size="3" color="#666666">2.命令行输入:adb shell dpm set-device-owner "com.android.deviceops/.DeviceAdminReceiver"</font>
<font size="3" color="#666666">(激活成功会输出带有Success的字符)</font>
<font size="3" color="#666666">3.你可以登录你的账号了</font>

<font size="3" color="#666666">仅需一次授权，重启依然有效，只能通过应用卸载自身关闭授权</font>

<font size="3" color="#666666">UI 规范：One UI / Material 3 设计语言</font>

<font size="5" color="#444444">两个版本：</font>
<font size="3" color="#666666">Entry 对应伪装版</font>
<font size="3" color="#666666">Native 对应普通版</font>

<font size="3" color="#666666">唯一区别，伪装版本特殊在：</font>
<font size="3" color="#666666">1. 应用名称是SmartThings</font>
<font size="3" color="#666666">2. 应用图标是三星的SmartThings</font>
<font size="3" color="#666666">3. 启动应用将显示伪装，提示“请连接手机 确定”，短按“确定”将退出应用，长按“确定”满0.79秒后，进入真界面</font>

<font size="5" color="#444444">功能：</font>
<font size="4" color="#555555">1. HTTP Proxy</font>
<font size="3" color="#666666">升级到OneUI8.0+后，内核TUN模块被移除，提供HTTP Proxy作为网络调试的功能替代，确保在无TUN支持的环境下依然能够实现高效的流量代理与分发。</font>
<font size="4" color="#555555">2. 管理停用应用</font>
<font size="3" color="#666666">无需adb授权，停用/启用应用</font>
<font size="3" color="#666666">特殊性：由于调用的是Device Owner API(setApplicationHidden)实现的停用，停用实现方式是多用户隔离机制，这不同于常规冻结式停用，你无法在系统设置-应用列表里找到app并重新启用，只能通过本应用管理。</font>

<font size="5" color="#444444">免责声明：</font>
<font size="3" color="#666666">系统应用可进行6分钟停用测试</font>
<font size="3" color="#666666">但注意！停用系统应用后，用户空间可能正常，但重启可能无法开机</font>
<font size="3" color="#666666">解决办法只有格式化</font>

<font size="2" color="#999999">Powered by Claude</font>
