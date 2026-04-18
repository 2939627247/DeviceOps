# DeviceOps

A device management application based on the Android **Device Owner API**, providing HTTP proxy and application visibility control.

# Setup

DeviceOps requires Device Owner privileges, which must be granted manually via ADB after installation.

**1. Remove all accounts from the device**

Sign out of all accounts on the device (Google, Samsung, Xiaomi, Huawei, etc.) before proceeding. The system does not allow Device Owner activation while accounts are present.

**2. Grant Device Owner**

```bash
adb shell dpm set-device-owner "com.android.deviceops/.DeviceAdminReceiver"
```

A response containing `Success` confirms activation.

**3. Re-add accounts**

Accounts can be added back normally after activation.

> [!NOTE]
> Activation persists across reboots. To revoke Device Owner, use the uninstall option within the app — no other removal path is available.

# Variants

### Native
Standard app icon and name. Launches directly into the management interface.

### Entry
Disguised as **SmartThings** with the official Samsung SmartThings icon.

On launch, a dialog reading *"请连接手机"* is displayed:
- **Short press** "确定" — exits the app
- **Hold** "确定" for **≥ 0.79 seconds** — enters the management interface

# Features

## HTTP Proxy

OneUI 8.0+ removes the TUN module from the kernel. DeviceOps provides an HTTP proxy as a replacement for network debugging, enabling traffic forwarding without TUN support.

## Application Visibility Control

Uses `DevicePolicyManager.setApplicationHidden` to hide or restore applications without additional ADB interaction after initial setup.

This differs from conventional app disabling. Hidden applications are isolated via a multi-user mechanism and disappear from the system-wide app list. They cannot be restored through **Settings → Apps** — only through DeviceOps.

# Warning

> [!CAUTION]
> System applications support a maximum **6-minute** disable test window.
>
> Hiding a system application may leave the user space functional, but **the device may fail to boot after a restart**. The only recovery is a factory reset.
