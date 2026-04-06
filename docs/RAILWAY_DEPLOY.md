# Deploy Railway cho Shop Gia Thinh

## 1. Tinh trang repo

- Repo da co file `railway.json` de Railway build va start app tu dong.
- `server.port` da doc tu bien `PORT`, phu hop voi Railway.
- App dang dong goi thanh file WAR executable va duoc start bang `java -jar target/*.war`.

## 2. Tao service tren Railway

1. Dang nhap Railway.
2. Chon `New Project`.
3. Chon `Deploy from GitHub repo`.
4. Chon repo `thinhweb3/ShopDoChoiGiaThinh`.
5. Chon branch `main`.
6. Doi Railway build xong deployment dau tien.

Neu Railway hoi build/start command, giu nguyen theo file `railway.json` trong repo.

## 3. Tao domain public

1. Vao service vua tao.
2. Mo tab `Settings`.
3. Mo muc `Networking`.
4. Bam `Generate Domain`.
5. Luu lai domain dang `https://...railway.app`.

## 4. Khai bao bien moi truong

Vao tab `Variables` cua service va them toi thieu:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `STORE_CONTACT_NAME`
- `STORE_CONTACT_PHONE`
- `STORE_CONTACT_ZALO`

Nen them them:

- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `PAYMENT_QR_BANK_ID`
- `PAYMENT_QR_ACCOUNT`
- `PAYMENT_QR_ACQ_NAME`
- `PAYMENT_QR_TEMPLATE`

- `JAVA_TOOL_OPTIONS`
- `SERVER_TOMCAT_THREADS_MAX`
- `SERVER_TOMCAT_THREADS_MIN_SPARE`
- `SERVER_TOMCAT_MAX_CONNECTIONS`
- `SERVER_TOMCAT_ACCEPT_COUNT`
- `SERVER_TOMCAT_PROCESSOR_CACHE`

## 5. Cau hinh luu anh de khong bi mat sau moi lan redeploy

Neu ban co upload anh tu trang admin, can gan `Volume`.

1. Tao `Volume` trong Railway va gan vao service.
2. Dat `Mount Path` la `/app/data`.
3. Dat bien moi truong `APP_STORAGE_UPLOAD_ROOT=/app/data/uploads`.

Neu khong gan volume, anh upload local se bi mat sau khi redeploy.

## 6. Kiem tra sau deploy

Sau khi deploy xanh, mo cac URL sau:

- `/`
- `/home/index`
- `/product/list`
- `/auth/login`
- `/admin/login`

Neu dang nhap admin va upload anh duoc, service da o muc co the dung that.

## 7. Cau hinh toi uu RAM cho goi 1 GB

Repo da ep gioi han JVM trong `railway.json`. Neu muon dat bang Railway Variables cho moi truong khac, dung bo bien sau:

- `JAVA_TOOL_OPTIONS=-Xms64m -Xmx512m -Xss512k -XX:MaxMetaspaceSize=192m -XX:ReservedCodeCacheSize=64m -XX:MaxDirectMemorySize=64m -XX:+UseSerialGC -XX:ActiveProcessorCount=1 -XX:TieredStopAtLevel=1 -XX:+ExitOnOutOfMemoryError`
- `DB_MAX_POOL_SIZE=2`
- `DB_MIN_IDLE=0`
- `SERVER_TOMCAT_THREADS_MAX=20`
- `SERVER_TOMCAT_THREADS_MIN_SPARE=2`
- `SERVER_TOMCAT_MAX_CONNECTIONS=100`
- `SERVER_TOMCAT_ACCEPT_COUNT=20`
- `SERVER_TOMCAT_PROCESSOR_CACHE=10`

Ly do:

- Heap Java khong duoc an het 1 GB RAM, tranh bi Railway kill vi vuot memory.
- Tomcat mac dinh cho toi 200 worker threads, qua du voi shop nho va ton RAM.
- Pool ket noi database 2 connection la du cho luong truy cap nho.

## 8. Loi hay gap

- Build loi dependency Spring milestone:
  - Repo da duoc bo sung `spring-milestones` trong `pom.xml`. Neu van loi, thu `Redeploy` lai.
- Anh bi mat sau redeploy:
  - Kiem tra volume co mount vao `/app/data` va `APP_STORAGE_UPLOAD_ROOT` da la `/app/data/uploads`.
