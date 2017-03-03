# ModularizationArchitecture

ModularizationArchitecture is a routing-based multi-process, component-based architecture on the Android platform: it communicates with different modules and processes by sharing routes without referring to other modules. It is suitable for medium-sized App architecture team collaboration, parallel development, business line decoupling, white-box testing and other scenes.

## Getting Start

[开始使用](http://blog.spinytech.com/2017/02/01/ma_get_start_cn/)

[Getting Start](http://blog.spinytech.com/2017/02/03/ma_get_start_en/)

## Download

Maven:

```xml
<dependency>
  <groupId>com.spinytech.ma</groupId>
  <artifactId>macore</artifactId>
  <version>0.2.1</version>
  <type>pom</type>
</dependency>
```

Gradle:

```groovy
compile 'com.spinytech.ma:macore:0.2.1'
```

## ProGuard

If you are using ProGuard you might need to add the following option:
```
-dontwarn com.spinytech.**
```

## Other

[Android架构思考](http://blog.spinytech.com/2016/12/28/android_modularization/)

## License


    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

