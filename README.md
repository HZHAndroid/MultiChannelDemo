# 介绍
[简书]()<br/>
参考：<br/>
[多渠道打包之动态修改App名称，图标，applicationId，版本号，添加资源](https://blog.csdn.net/abc6368765/article/details/52786509/)<br/>
[AndroidStudio3.0 gradle多渠道打包之 动态设置app名称、图标、包名等](https://blog.csdn.net/duxingerlai/article/details/85069216)<br/>
[Android多渠道打包且根据不同产品打包不同的assets资源目录](https://blog.csdn.net/xuwei7746521/article/details/83148894)<br/>

## 前提
```
我这里的环境：
gradle 插件版本： classpath "com.android.tools.build:gradle:7.0.0-beta05"
gradle 版本：https\://services.gradle.org/distributions/gradle-7.0.2-bin.zip
```

## 一、为什么需要多渠道打包
```
    假如我们没使用多渠道打包，假设切换 api 环境的情况下，我们一般会手动的去更改之后再打包，有些时候我们忘了改回去，发布的时候，
  可能连接的是测试的环境。这样的话，影响就非常巨大了。当然，多渠道打包，给我们的带来的方便还有很多，例如：同一份代码，打包出不同的
  app、已经app不同版本、不同名称、不同环境。
```

## 二、多渠道打包，在Android上，可以解决我们的一些什么问题。
```
    可以修改App名称、图标，applicationId，版本号；
    添加资源，设置不同的请求环境；
    添加某些定制化包，需要触发的标志等等
```

## 三、了解一些模块话，gradle 配置常用的东西
```
1、在 android{} 标签下的 sourceSets{} 标签：可以来设置一些渠道的资源目录，设置后，同名资源会以渠道内的为主；
2、 移除lint检测的error，也是放在 android{} 标签下
    lintOptions {
        abortOnError false
    }
3、在 android{} 下，添加  flavorDimensions 去定义一个纬度，例如： flavorDimensions "main"，main 是纬度名称；
4、在android{}中使用 productFlavors{} 去定义渠道；
5、在 android{} 中使用 buildTypes{} 去定义打包方式；
6、使用 buildConfigField 去定义一个变量到我们的 BuildConfig 类中；
7、applicationIdSuffix 是让我们在原来的包名基础上，加上一个后缀，例如：applicationIdSuffix ".debug"；
8、versionNameSuffix 为版本号添加后缀，例如：versionNameSuffix "-debug"；
```

## 四、开始我们的Demo
### 1、实际开发中，我们签名文件的那些密码等信息，是需要做一层安全的，不然别人知道后，可以进行对你应用破解，并且重签名，而且签名使用的就是你原来的签名
### 所以，我们把签名信息等放到 local.properties，或者自己新建一个这种文件，都是可以的，不过该文件不上传到 git
```
1、我在 local.properties 中添加了 
# 签名文件信息
keystroe_storeFile=../key/multi_channel.jks
keystroe_storePassword=123456
keystroe_keyAlias=multi_channel
keystroe_keyPassword=123456

2、在我的 app 的 build.gradle 添加代码，去加载 local.properties 文件
//获取local.properties的内容
Properties properties = new Properties()
properties.load(project.rootProject.file('local.properties').newDataInputStream())

```

### 2、实际开发中，我们会把渠道信息，版本号等信息抽离到一个配置gradle文件中，方便统一管理
```
1、我创建了 app_config.gradle 文件，配置如下：

ext {
    // 多渠道产品信息
    envInfo = [
            prod: [
                    // 应用 id
                    applicationId: "com.young.multichanneldemo",
                    // 构建版本号
                    versionCode  : 20210905,
                    // 版本名称
                    versionName  : "1.0",
                    // 请求的域名
                    host         : "https://www.young.com/prod",
            ],
            uat : [
                    // 应用 id
                    applicationId: "com.young.multichanneldemo1",
                    // 构建版本号
                    versionCode  : 1,
                    // 版本名称
                    versionName  : "2.0",
                    host         : "https://www.young.com/uat",
            ],
    ]
}

2、记得在模块，中引入当前配置，之后才可以调用当前配置的信息：
apply from: "../app_config.gradle"

```

## 3、定义一个签名对象，设置我们的签名配置：
```
//获取local.properties的内容
Properties properties = new Properties()
properties.load(project.rootProject.file('local.properties').newDataInputStream())

在 android{} 中加入下面配置
    signingConfigs {
        config {
            storeFile file(properties.getProperty("keystroe_storeFile"))
            storePassword properties.getProperty("keystroe_storePassword")
            keyAlias properties.getProperty("keystroe_keyAlias")
            keyPassword properties.getProperty("keystroe_keyPassword")
        }
    }
    
在 signingConfigs 可以配置多个签名配置，加入不同渠道签名不同
```
### 4、android{} 中的 defaultConfig{} 里的配置，例如：应用名称等，可以注释也不可以不注释，因为我们会在渠道里配置，我
### 这里注释了 applicationId

### 5、新增渠道特有的资源路径
```
 //移除lint检测的error
    lintOptions {
        abortOnError false
    }

    sourceSets {
//        main {
//            jniLibs.srcDirs = ['libs']
//        }
        // young 这里新增指定prd环境的资源文件，也就是这里的文件会覆盖 res 的同名文件
        // 这里的 prod.res.srcDirs 中的 prod 是渠道名称
        // 然后这里指向的资源路径，是渠道特有的资源，总而言之，就是这里的资源会覆盖，正常 res 目录下的资源
        prod.res.srcDirs = ['src/main/res-prod']
        uat.res.srcDirs = ['src/main/res-uat']
    }
注意：
1、'src/main/res-prod' 和 'src/main/res-uat' 需要自己手动创建；
2、prod.res.srcDirs 中的 prod 是渠道名称；uat.res.srcDirs 中的 uat 也是渠道名称，
其实调用方式就是：渠道名称.res.srcDirs
```

### 6、设置渠道
```
这里我设置了 prod、uat 两个渠道

    // 配置多渠道打包
    // 在 productFlavors 中配置多少个渠道，最后打包就有多少个渠道可以选择打包
    // defaultConfig{} 可以配置的，都可以在渠道里配置
    // 这里指定了渠道之后，原本的 debug 和 release 渠道就不存在了
    productFlavors {
        // 这个渠道叫 prod
        prod {
            // 每个环境包名可以指定不同
            applicationId envInfo.prod.applicationId
            versionCode envInfo.prod.versionCode
            versionName envInfo.prod.versionName
            flavorDimensions "main"

            // 修改 AndroidManifest.xml 里渠道变量
            manifestPlaceholders = [app_icon: "@mipmap/logo"]

            // 动态添加 string.xml 字段；
            // 注意，这里是添加，在 string.xml 不能有这个字段，会重名！！！
            // 这里不建议这样添加，因为国际化的时候这里没办法处理
            // 所以假如真的需要覆盖，则在 sourceSets {} 中指定的资源路径，去覆盖资源实现
//            resValue "string", "app_name", "百度"
//            resValue "bool", "auto_updates", 'false'
            // 动态修改 常量 字段
//            buildConfigField "String", "ENVIRONMENT", '"我是百度首页"'
            buildConfigField "String", "ENVIRONMENT", "\"${envInfo.prod.host}\""
        }
        // 这个渠道叫 uat
        uat {
            applicationId envInfo.uat.applicationId
            versionCode envInfo.uat.versionCode
            versionName envInfo.uat.versionName
            flavorDimensions "main"
            manifestPlaceholders = [app_icon: "@mipmap/logo"]
            buildConfigField "String", "ENVIRONMENT", "\"${envInfo.uat.host}\""
        }
    }
```

### 7、配置打包方式和自定义 apk 的包名
```
    // 这里是打包方式，指定不同的签名方式、混淆逻辑等
    // buildTypes 指定多少个打包方式（debug、release）
    // 那么 productFlavors {} 的每个管道，都会对应多少个结果包
    // 例如：
    // prod管道，就会有：prodDebug、prodRelease、 prodAlpha等结果包
    // uat管道，就会有：uatDebug、uatRelease、uatAlpha 等结果包
    buildTypes {
        debug {
            // 使用config签名(这个配置虽然可以放到 productFlavors{} 的管道中，但是debug包的话，签名配置不会被覆盖，
            // 也就是会使用系统默认的签名文件，但是配置在 buildTypes{} 这里，就不会使用系统默认的，而是使用我们指定的)
            signingConfig signingConfigs.config

            // debug模式下，显示log
            buildConfigField("boolean", "LOG_DEBUG", "true")

            //为已经存在的applicationId添加后缀(就说变成不同的包名了)
            applicationIdSuffix ".debug"
            // 为版本名添加后缀
            versionNameSuffix "-debug"
            // 不开启混淆
            minifyEnabled false
            // 不开启ZipAlign优化
            zipAlignEnabled false
            // 不移除无用的resource文件
            shrinkResources false
        }

        release {
            // 使用config签名
            signingConfig signingConfigs.config

            // release模式下，不显示log
            buildConfigField("boolean", "LOG_DEBUG", "false")
            // 为版本名添加后缀
            versionNameSuffix "-relase"
            // 不开启混淆
            minifyEnabled true
            // 开启ZipAlign优化
            zipAlignEnabled true
            // 移除无用的resource文件
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }

        alpha {
            // 使用config签名
            signingConfig signingConfigs.config

            // debug模式下，显示log
            buildConfigField("boolean", "LOG_DEBUG", "true")

            //为已经存在的applicationId添加后缀
            applicationIdSuffix ".alpha"
            // 为版本名添加后缀
            versionNameSuffix "-alpha"
            // 不开启混淆
            minifyEnabled false
            // 不开启ZipAlign优化
            zipAlignEnabled false
            // 不移除无用的resource文件
            shrinkResources false
        }

        // 这部分是 as  3.0 以下的
//        // 批量打包
//        applicationVariants.all { variant ->
//            variant.outputs.each { output ->
//                def outputFile = output.outputFile
//                println("outputFile = ${outputFile}")
//                if (outputFile != null && outputFile.name.endsWith('.apk')) {
//                    //输出apk名称为：渠道名_版本名_时间.apk
//                    def fileName = "${variant.productFlavors[0].name}_v${defaultConfig.versionName}_${releaseTime()}.apk"
//                    output.outputFile = new File(outputFile.parent, fileName)
//                }
//            }
//        }
        // as 3.0 以上的
        // https://blog.csdn.net/qq_36317441/article/details/81625936

        applicationVariants.all { variant ->
            variant.outputs.all { output ->
                def outputFile = output.outputFile
                if (outputFile != null && outputFile.name.endsWith('.apk')) {
                    // https://blog.csdn.net/h_bpdwn/article/details/108385118
                    // https://blog.csdn.net/u014780554/article/details/81284330
                    /*指定输出到 ${project}/outputs/apk/release文件夹下*/
                    // young 不建议更改，更改之后直接运行，会运行之后看不到 app
                    //variant.getPackageApplication().outputDirectory = new File(project.rootDir.absolutePath + "/outputs/apk/release")
//                    variant.getPackageApplication().outputDirectory = new File(project.rootDir.absolutePath + File.separator + "app" + File.separator + "outputs" +
//                            File.separator + variant.flavorName + File.separator + variant.buildType.name)
                    // 指定 apk 的输出路径
                    //输出apk名称为：渠道名_版本名_时间.apk
//                    def fileName = "${variant.productFlavors[0].name}_v${defaultConfig.versionName}_${releaseTime()}.apk"
//                    def fileName = "${variant.productFlavors[0].name}_v${variant.productFlavors[0].versionName}_${releaseTime()}.apk"
                    def fileName = "${variant.flavorName}-${variant.buildType.name}_v${variant.productFlavors[0].versionName}_b${variant.productFlavors[0].versionCode}_${releaseTime()}.apk"
                    outputFileName = fileName
                }
            }

//            // https://blog.csdn.net/smallbabylong/article/details/111276762
//            // 打包完成后做的一些事,复制apk到指定文件夹,复制mapping等
//            variant.assemble.doLast {
//                String oldApkOutDirPath = rootDir.absolutePath + File.separator + "app" + File.separator + variant.flavorName
//                deleteDir(new File(oldApkOutDirPath))
//            }
        }

    }
    
注意：gradle 3.0 之前 和 之后修改包名的方式有区别
不建议使用 variant.getPackageApplication().outputDirectory 去修改打包的输出路径，
否则可能会导致你直接运行，启动的时候， apk 并没有在手机看到，还有就是打包完成提示打开的弹出框，也无法定位到apk的位置；
```

### 7、完整的 build.gradle 的配置，请看[这里](https://github.com/HZHAndroid/MultiChannelDemo/blob/main/app/build.gradle)<br/>
```
plugins {
    id 'com.android.application'
    id 'kotlin-android'
}

apply from: "../app_config.gradle"

// 参考：https://blog.csdn.net/abc6368765/article/details/52786509/

//打包时间
def releaseTime() {
    return new Date().format("yyyy-MM-dd", TimeZone.getTimeZone("UTC"))
}

//获取local.properties的内容
Properties properties = new Properties()
properties.load(project.rootProject.file('local.properties').newDataInputStream())

android {
    compileSdk 30
    buildToolsVersion "30.0.3"

    // 使用签名文件进行签名的两种方式
//    //第一种：使用gradle直接签名打包
//    signingConfigs {
//        config {
//            storeFile file('keyTest.jks')
//            storePassword '123456'
//            keyAlias 'HomeKey'
//            keyPassword '123456'
//        }
//    }
    //第二种：为了保护签名文件，把它放在local.properties中并在版本库中排除
    // ，不把这些信息写入到版本库中（注意，此种方式签名文件中不能有中文）
    // 在 signingConfigs 可以配置多个签名配置，加入不同渠道签名不同
    signingConfigs {
        config {
            storeFile file(properties.getProperty("keystroe_storeFile"))
            storePassword properties.getProperty("keystroe_storePassword")
            keyAlias properties.getProperty("keystroe_keyAlias")
            keyPassword properties.getProperty("keystroe_keyPassword")
        }
    }

    // 默认配置
    defaultConfig {
        // 这里配置到下面的渠道里去，方便定制，加入属性相同，依旧放在这里也可以的
//        applicationId "com.young.multichanneldemo"
        minSdk 21
        targetSdk 30
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

//    buildTypes {
//        release {
//            minifyEnabled false
//            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
//        }
//    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = '1.8'
    }

    //移除lint检测的error
    lintOptions {
        abortOnError false
    }

    sourceSets {
//        main {
//            jniLibs.srcDirs = ['libs']
//        }
        // young 这里新增指定prd环境的资源文件，也就是这里的文件会覆盖 res 的同名文件
        // 这里的 prod.res.srcDirs 中的 prod 是渠道名称
        // 然后这里指向的资源路径，是渠道特有的资源，总而言之，就是这里的资源会覆盖，正常 res 目录下的资源
        prod.res.srcDirs = ['src/main/res-prod']
        uat.res.srcDirs = ['src/main/res-uat']
    }

    // 定义一个纬度
    // https://www.bbsmax.com/A/nAJvvg03Jr/
    // 解决 All flavors must now belong to a named flavor dimension
    // 其实定义多个纬度后，就说会用第一个纬度的，分别跟第二个纬度以及后面的纬度进行组合
    //例如：
    //flavorDimensions "api", "mode"
    // 那么组合就是: api + mode 的各种组合
    flavorDimensions "main"


    // 配置多渠道打包
    // 在 productFlavors 中配置多少个渠道，最后打包就有多少个渠道可以选择打包
    // defaultConfig{} 可以配置的，都可以在渠道里配置
    // 这里指定了渠道之后，原本的 debug 和 release 渠道就不存在了
    productFlavors {
        // 这个渠道叫 prod
        prod {
            // 每个环境包名可以指定不同
            applicationId envInfo.prod.applicationId
            versionCode envInfo.prod.versionCode
            versionName envInfo.prod.versionName
            flavorDimensions "main"

            // 修改 AndroidManifest.xml 里渠道变量
            manifestPlaceholders = [app_icon: "@mipmap/logo"]

            // 动态添加 string.xml 字段；
            // 注意，这里是添加，在 string.xml 不能有这个字段，会重名！！！
            // 这里不建议这样添加，因为国际化的时候这里没办法处理
            // 所以假如真的需要覆盖，则在 sourceSets {} 中指定的资源路径，去覆盖资源实现
//            resValue "string", "app_name", "百度"
//            resValue "bool", "auto_updates", 'false'
            // 动态修改 常量 字段
//            buildConfigField "String", "ENVIRONMENT", '"我是百度首页"'
            buildConfigField "String", "ENVIRONMENT", "\"${envInfo.prod.host}\""
        }
        // 这个渠道叫 uat
        uat {
            applicationId envInfo.uat.applicationId
            versionCode envInfo.uat.versionCode
            versionName envInfo.uat.versionName
            flavorDimensions "main"
            manifestPlaceholders = [app_icon: "@mipmap/logo"]
            buildConfigField "String", "ENVIRONMENT", "\"${envInfo.uat.host}\""
        }
    }

    // 这里是打包方式，指定不同的签名方式、混淆逻辑等
    // buildTypes 指定多少个打包方式（debug、release）
    // 那么 productFlavors {} 的每个管道，都会对应多少个结果包
    // 例如：
    // prod管道，就会有：prodDebug、prodRelease、 prodAlpha等结果包
    // uat管道，就会有：uatDebug、uatRelease、uatAlpha 等结果包
    buildTypes {
        debug {
            // 使用config签名(这个配置虽然可以放到 productFlavors{} 的管道中，但是debug包的话，签名配置不会被覆盖，
            // 也就是会使用系统默认的签名文件，但是配置在 buildTypes{} 这里，就不会使用系统默认的，而是使用我们指定的)
            signingConfig signingConfigs.config

            // debug模式下，显示log
            buildConfigField("boolean", "LOG_DEBUG", "true")

            //为已经存在的applicationId添加后缀(就说变成不同的包名了)
            applicationIdSuffix ".debug"
            // 为版本名添加后缀
            versionNameSuffix "-debug"
            // 不开启混淆
            minifyEnabled false
            // 不开启ZipAlign优化
            zipAlignEnabled false
            // 不移除无用的resource文件
            shrinkResources false
        }

        release {
            // 使用config签名
            signingConfig signingConfigs.config

            // release模式下，不显示log
            buildConfigField("boolean", "LOG_DEBUG", "false")
            // 为版本名添加后缀
            versionNameSuffix "-relase"
            // 不开启混淆
            minifyEnabled true
            // 开启ZipAlign优化
            zipAlignEnabled true
            // 移除无用的resource文件
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }

        alpha {
            // 使用config签名
            signingConfig signingConfigs.config

            // debug模式下，显示log
            buildConfigField("boolean", "LOG_DEBUG", "true")

            //为已经存在的applicationId添加后缀
            applicationIdSuffix ".alpha"
            // 为版本名添加后缀
            versionNameSuffix "-alpha"
            // 不开启混淆
            minifyEnabled false
            // 不开启ZipAlign优化
            zipAlignEnabled false
            // 不移除无用的resource文件
            shrinkResources false
        }

        // 这部分是 as  3.0 以下的
//        // 批量打包
//        applicationVariants.all { variant ->
//            variant.outputs.each { output ->
//                def outputFile = output.outputFile
//                println("outputFile = ${outputFile}")
//                if (outputFile != null && outputFile.name.endsWith('.apk')) {
//                    //输出apk名称为：渠道名_版本名_时间.apk
//                    def fileName = "${variant.productFlavors[0].name}_v${defaultConfig.versionName}_${releaseTime()}.apk"
//                    output.outputFile = new File(outputFile.parent, fileName)
//                }
//            }
//        }
        // as 3.0 以上的
        // https://blog.csdn.net/qq_36317441/article/details/81625936

        applicationVariants.all { variant ->
            variant.outputs.all { output ->
                def outputFile = output.outputFile
                if (outputFile != null && outputFile.name.endsWith('.apk')) {
                    // https://blog.csdn.net/h_bpdwn/article/details/108385118
                    // https://blog.csdn.net/u014780554/article/details/81284330
                    /*指定输出到 ${project}/outputs/apk/release文件夹下*/
                    // young 不建议更改，更改之后直接运行，会运行之后看不到 app
                    //variant.getPackageApplication().outputDirectory = new File(project.rootDir.absolutePath + "/outputs/apk/release")
//                    variant.getPackageApplication().outputDirectory = new File(project.rootDir.absolutePath + File.separator + "app" + File.separator + "outputs" +
//                            File.separator + variant.flavorName + File.separator + variant.buildType.name)
                    // 指定 apk 的输出路径
                    //输出apk名称为：渠道名_版本名_时间.apk
//                    def fileName = "${variant.productFlavors[0].name}_v${defaultConfig.versionName}_${releaseTime()}.apk"
//                    def fileName = "${variant.productFlavors[0].name}_v${variant.productFlavors[0].versionName}_${releaseTime()}.apk"
                    def fileName = "${variant.flavorName}-${variant.buildType.name}_v${variant.productFlavors[0].versionName}_b${variant.productFlavors[0].versionCode}_${releaseTime()}.apk"
                    outputFileName = fileName
                }
            }

//            // https://blog.csdn.net/smallbabylong/article/details/111276762
//            // 打包完成后做的一些事,复制apk到指定文件夹,复制mapping等
//            variant.assemble.doLast {
//                String oldApkOutDirPath = rootDir.absolutePath + File.separator + "app" + File.separator + variant.flavorName
//                deleteDir(new File(oldApkOutDirPath))
//            }
        }

    }


}

/**
 * 删除目录
 * @param file 需要删除的目录
 * @return
 */
def deleteDir(File file) {
    if (!file.exists()) {
        return
    }
    if (file.isFile()) {
        file.delete()
        return
    }
    if (file.isDirectory()) {
        File[] fileList = file.listFiles()
        if (fileList == null || fileList.length == 0) {
            file.delete()
            return
        }
        for (File f : fileList) {
            deleteDir(f)
        }
        file.delete()
    }
}

dependencies {

    implementation 'androidx.core:core-ktx:1.6.0'
    implementation 'androidx.appcompat:appcompat:1.3.1'
    implementation 'com.google.android.material:material:1.4.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.0'
    testImplementation 'junit:junit:4.+'
    androidTestImplementation 'androidx.test.ext:junit:1.1.3'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'
}
```
