# MiPushEnhancement

一个欺骗应用让他们误以为设备是小米 MIUI 的 Xposed 模块。

## 使用

1. 在 [Releases](https://github.com/MiPushFramework/MiPushEnhancement/releases/latest) 或 [Xposed Module Repository](https://repo.xposed.info/module/org.meowcat.xposed.mipush) 中下载最新 APK。

2. 安装并在 Xposed, EdXposed 或其他没有恶意行为的 Xposed 实现 中激活模块。

3. 重启设备

### 迁移

这个模块是由原推送服务中的 Xposed 模块部分迁移来的。迁移过程中保留了所有核心功能，并增加了一些功能。然而，您需要重新配置白名单（如果之前在用的话）。请参考 [配置](#配置) 部分。

## 配置

本模块的配置文件位于 `/data/user_de/<用户 ID>/etc/module.conf`（很可能不同，具体查看 UI > 菜单 > 查看配置地址）。可以通过两种方式编辑：

1. UI：打开模块设置界面，它会显示部分常见配置，并在修改的时候保存到文件。如果你不会用编辑器，建议使用这种方式。

2. 人工编辑：使用文件编辑器以 Root 方式打开配置文件，阅读注释进行修改。配置格式为 INI。这种方式可以修改更多配置。

请注意，两种编辑方式冲突，请不要同时编辑。
对于新安装，配置文件不会自动创建，请打开 UI 并修改一次，模块会自动创建配置文件。
对于升级，在修改配置的时候会自动将新的配置加入文件。不会自动删除旧配置项，也不会自动合并。如有需要，将会在更新日志上显示。
对于多用户，请查看 [多用户](#多用户) 部分。

### 多用户

如果您在使用多用户（或者工作资料等），需要在每个用户都安装模块并创建配置。模块会自动读取目标应用所在用户的配置。如果找不到，会自动使用缺省配置。

## 原理

部分应用带有多个推送服务，它们有时会在小米设备上启用小米推送。这个模块有助于欺骗它们，来使它们在非小米设备上启用小米推送。

## 内置黑名单

为了防止出现意外情况，模块内置了一组黑名单。包名匹配黑名单中的软件将不被欺骗。没有选项可以关闭或修改黑名单。这些软件通常包括系统关键组件和依赖于机型判断的软件（如相机）。如果您对黑名单有建议，可以通过 [Issues](https://github.com/MiPushFramework/MiPushEnhancement/issues)、[Pull Request](https://github.com/MiPushFramework/MiPushEnhancement/pulls) 等方式贡献。

# License

[AMTPL v1 + GPL v3](LICENSE)

# Credits

[MlgmXyysd](https://github.com/MlgmXyysd/) 改进，重写，优化，修复bug

[YuutaW](https://github.com/Trumeet) 原作，迁移，配置，mipush相关组件
