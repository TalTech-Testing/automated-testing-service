
# Currently supported endpoints

Schemas can be generated from arete/api/data ```/request``` and ```/response```
* put
    * /image/{image}
    * /tests
* post
    * /:testAsync
    * /:testSync
    * /tests:update
    * /image/{image_name}:update
        * java-tester
        * python-tester
* get
    * /submissions
    * /submissions/{amount}
    * /submission/{hash}
    * /submission/{hash}/logs
    * /submissions/active
    * /debug/{1 or 0}
    * /logs


Some useful curl commands
```shell script
Force an update on a test:
curl -d '{"project":{"url":"git@gitlab.cs.ttu.ee:iti0102-2019/ex.git","path_with_namespace":"iti0211-2019/ex"}}' -X POST -H "Content-Type: application/json" localhost:8098/tests:update

Force an update on a image
curl -X POST -H "Content-Type: application/json" localhost:8098/tests/python-tester:update

Test something sync
curl -d '{"testingPlatform":"prolog","gitStudentRepo":"https://gitlab.cs.ttu.ee/envomp/iti0211-2019.git","uniid":"envomp","hash":"Fill me if you want to test a specific hash"}' -X POST -H "Content-Type: application/json" localhost:8098/:testSync
```

Template to add to new test repositories as user manual:

````
Kui teie ülesandel on vaja mingeid lisaparameetreid testimiseks, siis lisage enda ülesande kausta fail nimega arete.json, mis näeb välja järgmiselt:

{"dockerTimeout":120,"dockerExtra":["stylecheck"],"systemExtra":["noMail","noTesterFiles","noStd","noFeedback","minimalFeedback"]}

dockerTimeout - Kui kogu ülesande testimine võtab kaueb, kui 120 sekundit, siis muutke väärtus sobivaks.

stylecheck - stiilikontroll

noMail - tudengile ei saadeta meili

noTesterFiles - charon'isse ei jõua testi failid

noStd - charonisse ei jõua stdout ja stderr

noFeedback - tudeng ei näe läbikukkunud testide põhjuseid

minimalFeedback - tudeng ei näe assertion errorite põhjuseid

Näited:

ilma noFeedback ja minimalFeedback parameetrita: - kasutada lihtsamate ülesannetega

![](https://lh3.googleusercontent.com/OODuTVyMFDX6Z2a9ydyt5I21V6e_cR1p-7MX77Z1CPT5k3F3r8NCoVvCvbz8nGTLb8D3g4JDVnoOFpnxHd3_M2tli3oysxevhkEPPZImV_cN2aCkBssQN_HQ53pUJXndRKXUkDTXyuKDHlflUYasxhqlY23WMx5D3AhFRx-pDBObG57U-f1PCSq-0qCBUZBxnelh2OSstBlgpyakGsc_2J8bmqNNMQ6Ct8cKDaDeqPsgVb8VEpezYPLIucLmYQycRbozSQSolXKvpYGq3cq4aU30-zcsaovaAkkayvpNJtrmeEX-RyxMIO05mJqXGFagT38q_bIAqtInBVwIQgG0J9XMJ5N8zQA2gIUgx7nbkzDOM2VwO2opHPx4nPQf3nPFFcBA6YAtFjE_7l5vFVsEXMx0k-R82yJH3UUMvnKObRdXn34SEUHk315se8goWUE892VYolQDULXCiCyhk5ixAS8FlSgAVOc1JPD3Lx-Jop2vI9lzBiHUFQ1SB3Bj830jUEacndIQZHD5vnwfXj4IN234I-1PT03oS9V_54CpLjZrVXgRG8WXprIP-NdSfY93xPoLuTBRQudjI80qRBcYmo0FpaTlyxJ3gZ-avSTCS9xWn98v-9B4XmpEk4Ty2cwvykt5WuPRbAQXZb4CezwXNDYlzzrtZDZ3Wln-v19hA8aIq6MEfRe403w=w566-h101-no)

minimalFeedback: - kasutada ex ja pr ülesannetega

![](https://lh3.googleusercontent.com/7Lrcwo7h6pRfKv5ReAzSaszxrIQhl0degQ55qnGy6uCbdAvbunXDR8k6R727nxnLmDBHA-2GTObE-QwFmecO90AQsgtj65KUrfpR0WxWby7-mZIuQG8lHAFSC-i2BIP5BXZpMySQF8CGJrdNxdzwNgJkURdasD68HswXIuvyHRkWjsuSyEYFywM6lk91qvex5fNF8J-fp6bL0B_o2rWLIDNYb4EVqnW0FDG3KGBz5s6_1J_0EoZ7vioJ8lko_3a96XwKsS4khcRdDukEXPIpcVoX1I4zW9hA7ziUYmHvReSM54XobOBhlDT0mgZP_4eNdn4bXY-rGgsyUO5IbGAaw6xJ1iwHOEFz4JZYQim_bDweSCeAFfC7Fg3EXy8AGkeTnbCLPO93RslxwW4lkLl6vA82490O1oj8NuMCacsfbX6b4pq5qaZ-FGop6F1oNZJy9wU7AxeYsp_0Z3KwnQccqTZKwdkWWTBMo9XGnU7a0TxLpBGnLU7csrKrZThSAwaKATKZmwdz3tEnoVVCwQUmmS7VHeHGz3rIuqcS1WtwHMdOFtoIpF7kQIGdyCp9mrPvmoaZ8zX9kXPosC7M3hFWnE-UWz0BIlD3QwkbcZF6721IxwRWps0Bn02ssDO1NalkDapd0qajo6zJFYHSzGe2_CFEktov27SV8h7nmwGa9yKSBMJ1O0qnAao=w347-h89-no)

noFeedback: - kasutada xp ja eksami ülesannetega

![](https://lh3.googleusercontent.com/CVfR2iOcA4yfu-y-3KVmW22MDsBpGQ3xpNvpNN52lSkKW7Q-OCKbd-pLa3Jt9tJ2nXM6-McNAdb57QoSOTNfcOLpSZBUosoGi9tCMWa2cSzCh5ngDjiwPSJpyZ1_VKhRy21TTRHTA54arVvybytobQ-atHVq9PPRIeBsO2M00a2PMWXBGTt-Vp6EVS9uz0moKp55zR2Z3xZTJz64qT_P_bS2kJ5TMbdY046_FREJARNNaiKLX-k28kOskb_PPLATsSFwqjHt1osWU_Le9WaMWAcFn8kD_0karidfGsLqfNX9wrvXYBBEzdUJ3yQv28zhopFuSSM0q9M8Y-DRuRsG571mtFadjmEA_pkrF7PVAoMh3YFn8C1T8v_HtgIqwFGPNkOvqtnIBL8njVTBE0xK3QqjpOkJOkjmNgrfRVgX8Fcutf7f2xpczzMH1M-agTRmWvM8s4kJvzY5iXFYEYi2hbqEeEpFBIugQ_PFORcRYruPNYPLyT6IAGNLLNbR9P-5zLwQAX44saWMnTSDmXxH-GzyNuQxP46wDSUCoa2JgOsgMxGpXL9PiMSSKN3BSPISorux2XEQzW1YTSf-L0kGya75TWwqRbHmwcuugOKbc-KdLiDvtC-AVvr6hw1pMMeVfhk1302Y-NtSSWMx4wl7SaA_kNsXtLwSOF7vqTPbtxHW0rZqTjf1vC0=w351-h53-no)


stylecheck las olla sees iga ülesandega, kus pole kasutatud mingit raamistikku. Kui on django ülesanne, siis stylecheck las olla väljas.
````

Getting Started
----

### Manual setup

##### Local docker with postgres ####

You need latest of: Docker

```shell script
git clone https://github.com/envomp/arete
docker login -u automatedtestingservice -p $DOCKER_PASSWORD
docker-compose up -d
```

##### Local host machine with H2 ####
You need latest of: Java11, Docker, System Environments for ARETE_HOME, GITLAB_PASSWORD
___
##### for Linux
```shell script
git clone https://gitlab.cs.ttu.ee/envomp/automated-testing-service
./mvnw clean install
java -jar -Dspring.config.location=src/main/resources/application-dev.properties target/arete-0.0.1-SNAPSHOT.jar
```

##### for Windows

```shell script
git clone https://gitlab.cs.ttu.ee/envomp/automated-testing-service
mvnw.cmd clean install
java -jar -Dspring.config.location=src/main/resources/application-dev.properties target/arete-0.0.1-SNAPSHOT.jar
```

#### Server
___
##### Shell config for dind

___
/etc/gitlab-runner/config.toml
```shell script
concurrent = 1
check_interval = 0

[session_server]
  session_timeout = 1800

[[runners]]
  name = "Shell"
  url = "https://gitlab.cs.ttu.ee/"
  token = "XXX"
  executor = "shell"
  [runners.custom_build_dir]
  [runners.cache]
    [runners.cache.s3]
    [runners.cache.gcs]
```
___
##### Virtualbox config

___
C:/gitlab-runner/config.toml
```shell script
concurrent = 1
check_interval = 0

[session_server]
  session_timeout = 1800

[[runners]]
  name = "VM-runner"
  url = "https://gitlab.cs.ttu.ee/"
  token = "XXX"
  executor = "virtualbox"
  [runners.custom_build_dir]
  [runners.ssh]
    host = "192.168.56.101"
    user = "envomp"
    password = "XXX"
    port = "22"
  [runners.virtualbox]
    base_name = "Server"
    disable_snapshots = false
  [runners.cache]
    [runners.cache.s3]
    [runners.cache.gcs]
```

run in terminal:
```shell script
./arete_local_install.sh
```

cleanup:
```shell script
./docker_cleanup.sh
```

Scripts have been tested on Ubuntu 18.04 and 19.04
